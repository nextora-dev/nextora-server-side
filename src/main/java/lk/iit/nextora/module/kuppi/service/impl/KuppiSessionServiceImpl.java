package lk.iit.nextora.module.kuppi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.enums.KuppiSessionType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.infrastructure.notification.service.KuppiNotificationService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiAnalyticsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import lk.iit.nextora.module.kuppi.mapper.KuppiMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiNoteRepository;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KuppiSessionServiceImpl implements KuppiSessionService {

    private final KuppiSessionRepository sessionRepository;
    private final KuppiNoteRepository noteRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;
    private final KuppiMapper kuppiMapper;
    private final KuppiNotificationService kuppiNotificationService;
    private final S3Service s3Service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // helper for collecting uploaded file metadata
    private static class FileMeta {
        final String url;
        final String name;
        final Long size;
        final String fileType;

        FileMeta(String url, String name, Long size, String fileType) {
            this.url = url;
            this.name = name;
            this.size = size;
            this.fileType = fileType;
        }
    }

    private static final List<KuppiSessionStatus> PUBLIC_STATUSES = List.of(
            KuppiSessionStatus.SCHEDULED,
            KuppiSessionStatus.LIVE,
            KuppiSessionStatus.COMPLETED
    );

    private static final String KUPPI_SESSIONS_FOLDER = "kuppi-sessions";

    // ==================== Normal Student Operations ====================

    @Override
    public PagedResponse<KuppiSessionResponse> getPublicSessions(Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.findByStatusInAndIsDeletedFalse(PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    @Transactional
    public KuppiSessionResponse getSessionById(Long sessionId) {
        KuppiSession session = findSessionById(sessionId);
        session.incrementViewCount();
        sessionRepository.save(session);

        return kuppiMapper.toResponse(session);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> searchSessions(String keyword, Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.searchByKeyword(keyword, PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> searchBySubject(String subject, Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.searchBySubject(subject, PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> searchByHostName(String hostName, Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.searchByHostName(hostName, PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.findByDateRange(startDate, endDate, PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> getUpcomingSessions(Pageable pageable) {
        Page<KuppiSession> sessions = sessionRepository.findUpcomingSessions(LocalDateTime.now(), PUBLIC_STATUSES, pageable);
        return toPagedResponse(sessions);
    }

    // ==================== Kuppi Student Operations ====================

    @Override
    @Transactional
    public KuppiSessionResponse createSession(CreateKuppiSessionRequest request, MultipartFile[] files) {
        Long currentUserId = securityService.getCurrentUserId();
        Student host = findStudentById(currentUserId);

        validateKuppiStudent(host);
        validateSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());

        KuppiSession session = kuppiMapper.toEntity(request);
        session.setHost(host);
        session.setStatus(KuppiSessionStatus.SCHEDULED);

        // handle files (optional)
        List<FileMeta> collected = new java.util.ArrayList<>();
        if (files != null && files.length > 0) {
            List<String> uploadedUrls = new java.util.ArrayList<>();
            List<String> uploadedKeys = new java.util.ArrayList<>();
            try {
                for (MultipartFile f : files) {
                    if (f == null || f.isEmpty()) continue;
                    String contentType = f.getContentType();
                    if (contentType == null || (!contentType.startsWith("application/pdf")
                            && !contentType.startsWith("application/vnd.ms-powerpoint")
                            && !contentType.startsWith("application/vnd.openxmlformats-officedocument")
                            && !contentType.startsWith("image/"))) {
                        throw new BadRequestException("Invalid file type. Only PDF, PowerPoint, and image files are allowed");
                    }

                    String s3Key = s3Service.uploadFile(f, KUPPI_SESSIONS_FOLDER);
                    // record key so we can cleanup on failure
                    uploadedKeys.add(s3Key);
                    String url = s3Service.getPublicUrl(s3Key);
                    uploadedUrls.add(url);

                    // collect metadata
                    String derivedType;
                    if (contentType.contains("pdf")) {
                        derivedType = "PDF";
                    } else if (contentType.contains("powerpoint") || contentType.contains("presentation")) {
                        derivedType = "SLIDES";
                    } else if (contentType.startsWith("image/")) {
                        derivedType = "IMAGE";
                    } else {
                        derivedType = "DOCUMENT";
                    }
                    collected.add(new FileMeta(url, f.getOriginalFilename(), f.getSize(), derivedType));

                    // set primary file metadata from first valid file
                    if (session.getFileUrl() == null) {
                        session.setFileUrl(url);
                        session.setFileName(f.getOriginalFilename());
                        session.setFileSize(f.getSize());
                        session.setFileType(derivedType);
                    }
                }
            } catch (RuntimeException | Error ex) {
                // Cleanup any uploaded keys to avoid orphaned S3 objects
                for (String key : uploadedKeys) {
                    try {
                        s3Service.deleteFile(key);
                    } catch (Exception e) {
                        log.warn("Failed to cleanup uploaded S3 key {} after error: {}", key, e.getMessage());
                    }
                }
                throw ex;
            }

            // store all uploaded URLs as JSON array string in fileUrl (for compatibility also keep first URL in fileUrl field)
            try {
                String urlsJson = objectMapper.writeValueAsString(uploadedUrls);
                // store JSON in fileUrlJson field? We don't have field - reuse fileUrl to keep first URL, but create a companion column 'fileUrls' would be better.
                // For now, store JSON in fileUrl as the canonical list when multiple provided
                if (uploadedUrls.size() > 1) {
                    session.setFileUrl(urlsJson);
                }
            } catch (JsonProcessingException e) {
                // ignore serialization failure, fallback to single url already set
                log.warn("Failed to serialize uploaded URLs", e);
            }
        }

        // Set defaults
        if (session.getViewCount() == null) {
            session.setViewCount(0L);
        }
        if (session.getSessionType() == null) {
            session.setSessionType(KuppiSessionType.LIVE);
        }

        session = sessionRepository.save(session);
        log.info("Kuppi session with files uploaded by student {} to S3: {}", currentUserId, session.getId());

        // create KuppiNote entries for each uploaded file so they appear in session.notes
        if (!collected.isEmpty()) {
            for (FileMeta fm : collected) {
                KuppiNote note = KuppiNote.builder()
                        .title(fm.name != null ? fm.name : session.getTitle())
                        .description(null)
                        .fileType(fm.fileType)
                        .fileUrl(fm.url)
                        .fileName(fm.name)
                        .fileSize(fm.size)
                        .session(session)
                        .uploadedBy(host)
                        .allowDownload(true)
                        .build();

                note = noteRepository.save(note);
                // attach to session in-memory so mapper will include it
                session.getNotes().add(note);
            }
            // update session in DB to reflect notes relationship (optional)
            session = sessionRepository.save(session);
        }

        log.info("Kuppi session with files uploaded by student {} to S3: {}", currentUserId, session.getId());

        // Send push notification to all students (async - non-blocking)
        String hostFullName = host.getFirstName() + " " + host.getLastName();
        kuppiNotificationService.notifyNewKuppiSession(
                session.getId(),
                session.getTitle(),
                session.getSubject(),
                hostFullName,
                session.getScheduledStartTime()
        );

        // Build response and ensure notes are present
        KuppiSessionResponse response = kuppiMapper.toResponse(session);
        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            List<KuppiNote> noteList = new java.util.ArrayList<>(session.getNotes());
            response.setNotes(kuppiMapper.toNoteResponseList(noteList));
        }

        return response;
    }

    @Override
    @Transactional
    public void cancelSession(Long sessionId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        validateSessionOwnership(session, currentUserId);

        // Validate session can be cancelled
        if (KuppiSessionStatus.CANCELLED.equals(session.getStatus())) {
            throw new BadRequestException("Session is already cancelled");
        }
        if (KuppiSessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Cannot cancel a completed session");
        }

        // Set cancellation details
        session.setStatus(KuppiSessionStatus.CANCELLED);
        session.setCancellationReason(reason);
        session.setCancelledAt(LocalDateTime.now());
        sessionRepository.save(session);

        log.info("Kuppi session {} cancelled by student {}", sessionId, currentUserId);

        // Send push notification to all students (async - non-blocking)
        kuppiNotificationService.notifyKuppiSessionCancelled(
                session.getId(),
                session.getTitle(),
                session.getSubject(),
                reason
        );
    }

    @Override
    @Transactional
    public KuppiSessionResponse rescheduleSession(Long sessionId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        validateSessionOwnership(session, currentUserId);

        // Validate session can be rescheduled
        if (KuppiSessionStatus.CANCELLED.equals(session.getStatus())) {
            throw new BadRequestException("Cannot reschedule a cancelled session");
        }
        if (KuppiSessionStatus.COMPLETED.equals(session.getStatus())) {
            throw new BadRequestException("Cannot reschedule a completed session");
        }

        validateSchedule(newStartTime, newEndTime);

        session.setScheduledStartTime(newStartTime);
        session.setScheduledEndTime(newEndTime);
        session = sessionRepository.save(session);

        log.info("Kuppi session {} rescheduled by student {}", sessionId, currentUserId);

        // Send push notification to all students (async - non-blocking)
        kuppiNotificationService.notifyKuppiSessionRescheduled(
                session.getId(),
                session.getTitle(),
                session.getSubject(),
                newStartTime
        );

        return kuppiMapper.toResponse(session);
    }

    @Override
    public PagedResponse<KuppiSessionResponse> getMySessions(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<KuppiSession> sessions = sessionRepository.findByHostIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(sessions);
    }

    @Override
    public KuppiAnalyticsResponse getMyAnalytics() {
        Long currentUserId = securityService.getCurrentUserId();

        List<KuppiSession> mySessions = sessionRepository.findByHostIdAndIsDeletedFalse(currentUserId);

        long totalSessions = mySessions.size();
        long completedSessions = mySessions.stream()
                .filter(s -> KuppiSessionStatus.COMPLETED.equals(s.getStatus()))
                .count();
        long upcomingSessions = mySessions.stream()
                .filter(s -> s.getScheduledStartTime().isAfter(LocalDateTime.now()))
                .count();
        long totalViews = mySessions.stream()
                .mapToLong(KuppiSession::getViewCount)
                .sum();

        Long totalNoteViews = noteRepository.getTotalViewsByUploader(currentUserId);
        long totalNotes = noteRepository.countByUploadedByIdAndIsDeletedFalse(currentUserId);

        return KuppiAnalyticsResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .upcomingSessions(upcomingSessions)
                .totalSessionViews(totalViews)
                .totalNotes(totalNotes)
                .totalNoteViews(totalNoteViews != null ? totalNoteViews : 0L)
                .build();
    }

    // ==================== Admin Operations ====================

    @Override
    public KuppiPlatformStatsResponse getPlatformStats() {
        long totalSessions = sessionRepository.count();
        long completed = sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.COMPLETED);
        long cancelled = sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.CANCELLED);
        long totalNotes = noteRepository.count();

        return KuppiPlatformStatsResponse.builder()
                .totalSessions(totalSessions)
                .totalNotes(totalNotes)
                .completedSessions(completed)
                .cancelledSessions(cancelled)
                .totalParticipants(0L)
                .totalKuppiStudents(0L)
                .totalViews(0L)
                .totalDownloads(0L)
                .averagePlatformRating(0.0)
                .sessionsThisWeek(0L)
                .sessionsThisMonth(0L)
                .newKuppiStudentsThisMonth(0L)
                .build();
    }

    @Override
    @Transactional
    public void softDeleteSession(Long sessionId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        // Only owner may soft-delete their session
        validateSessionOwnership(session, currentUserId);

        // Prevent deleting live sessions
        if (KuppiSessionStatus.LIVE.equals(session.getStatus())) {
            throw new BadRequestException("Cannot soft-delete a session that is currently live");
        }

        // Best-effort delete S3 objects for all notes
        List<KuppiNote> notes = noteRepository.findBySessionIdAndIsDeletedFalse(sessionId);
        for (KuppiNote n : notes) {
            try {
                if (n.getFileUrl() != null && !n.getFileUrl().isEmpty()) {
                    tryDeleteS3Object(n.getFileUrl());
                }
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for note {} during soft-delete: {}", n.getId(), e.getMessage());
            }
            // soft-delete note
            n.softDelete();
            noteRepository.save(n);
        }

        // Soft-delete session
        session.softDelete();
        sessionRepository.save(session);

        log.info("Kuppi session {} soft-deleted with files removed by student {}", sessionId, currentUserId);
    }

    @Override
    @Transactional
    public void permanentlyDeleteSession(Long sessionId) {
        // Only privileged users should call this - permission checked by controller
        KuppiSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiSession", "id", sessionId));

        // Best-effort delete S3 objects for all notes (and any session file Urls)
        List<KuppiNote> notes = noteRepository.findBySessionIdAndIsDeletedFalse(sessionId);
        for (KuppiNote n : notes) {
            try {
                if (n.getFileUrl() != null && !n.getFileUrl().isEmpty()) {
                    tryDeleteS3Object(n.getFileUrl());
                }
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for note {} during permanent-delete: {}", n.getId(), e.getMessage());
            }
            // permanently delete note row
            noteRepository.delete(n);
        }

        // Also attempt to delete session-level fileUrl if it contains direct URLs
        try {
            if (session.getFileUrl() != null && !session.getFileUrl().isEmpty()) {
                String existing = session.getFileUrl();
                if (existing.trim().startsWith("[")) {
                    List<String> existingUrls = objectMapper.readValue(existing, new com.fasterxml.jackson.core.type.TypeReference<List<String>>(){});
                    for (String url : existingUrls) {
                        tryDeleteS3Object(url);
                    }
                } else {
                    tryDeleteS3Object(existing);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to delete session file urls from S3 during permanent-delete {}: {}", sessionId, e.getMessage());
        }

        // delete session row
        sessionRepository.delete(session);

        log.info("Kuppi session {} permanently deleted with files by admin", sessionId);
    }

    // ==================== Helper Methods ====================

    private KuppiSession findSessionById(Long sessionId) {
        return sessionRepository.findByIdWithHost(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiSession", "id", sessionId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private void validateKuppiStudent(Student student) {
        // Check for both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
        if (!student.hasKuppiCapability()) {
            throw new UnauthorizedException("Only Kuppi Students can create sessions");
        }
    }

    private void validateSessionOwnership(KuppiSession session, Long userId) {
        if (!session.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own sessions");
        }
    }

    private void validateSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
    }

    private PagedResponse<KuppiSessionResponse> toPagedResponse(Page<KuppiSession> sessions) {
        List<KuppiSessionResponse> content = kuppiMapper.toResponseList(sessions.getContent());
        return PagedResponse.<KuppiSessionResponse>builder()
                .content(content)
                .pageNumber(sessions.getNumber())
                .pageSize(sessions.getSize())
                .totalElements(sessions.getTotalElements())
                .totalPages(sessions.getTotalPages())
                .first(sessions.isFirst())
                .last(sessions.isLast())
                .empty(sessions.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public KuppiSessionResponse updateSession(Long sessionId, UpdateKuppiSessionRequest request, MultipartFile[] files, java.util.List<Long> removeNoteIds) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        // validate ownership
        validateSessionOwnership(session, currentUserId);

        // Update basic fields
        kuppiMapper.updateSessionFromRequest(request, session);

        // If files provided, replace existing files and notes
        if (files != null && files.length > 0) {
            // 1) Remove existing notes according to removeNoteIds (if provided) or remove all
            if (removeNoteIds != null && !removeNoteIds.isEmpty()) {
                for (Long nid : removeNoteIds) {
                    java.util.Optional<KuppiNote> opt = noteRepository.findByIdWithDetails(nid);
                    if (opt.isPresent()) {
                        KuppiNote n = opt.get();
                        try {
                            if (n.getFileUrl() != null && !n.getFileUrl().isEmpty()) {
                                tryDeleteS3Object(n.getFileUrl());
                            }
                        } catch (Exception e) {
                            log.warn("Failed to delete S3 object for existing note {}: {}", n.getId(), e.getMessage());
                        }
                        n.softDelete();
                        noteRepository.save(n);
                        // remove from session notes by id
                        session.getNotes().removeIf(nn -> nn.getId().equals(n.getId()));
                    }
                }
            } else {
                List<lk.iit.nextora.module.kuppi.entity.KuppiNote> existingNotes = noteRepository.findBySessionIdAndIsDeletedFalse(sessionId);
                if (existingNotes != null && !existingNotes.isEmpty()) {
                    for (lk.iit.nextora.module.kuppi.entity.KuppiNote n : existingNotes) {
                        try {
                            if (n.getFileUrl() != null && !n.getFileUrl().isEmpty()) {
                                tryDeleteS3Object(n.getFileUrl());
                            }
                        } catch (Exception e) {
                            log.warn("Failed to delete S3 object for existing note {}: {}", n.getId(), e.getMessage());
                        }
                        n.softDelete();
                        noteRepository.save(n);
                    }
                    // clear session notes collection in-memory
                    session.getNotes().clear();
                }
            }

            // reset primary file metadata to allow new primary file to be set
            session.setFileUrl(null);
            session.setFileName(null);
            session.setFileSize(null);
            session.setFileType(null);

             // 2) Upload new files and collect metadata
             List<FileMeta> collected = new java.util.ArrayList<>();
             List<String> uploadedUrls = new java.util.ArrayList<>();
             List<String> uploadedKeys = new java.util.ArrayList<>();
             try {
             for (MultipartFile f : files) {
                 if (f == null || f.isEmpty()) continue;
                 String contentType = f.getContentType();
                 if (contentType == null || (!contentType.startsWith("application/pdf")
                         && !contentType.startsWith("application/vnd.ms-powerpoint")
                         && !contentType.startsWith("application/vnd.openxmlformats-officedocument")
                         && !contentType.startsWith("image/"))) {
                     throw new BadRequestException("Invalid file type. Only PDF, PowerPoint, and image files are allowed");
                 }

                String s3Key = s3Service.uploadFile(f, KUPPI_SESSIONS_FOLDER);
                // record key so we can cleanup on failure
                uploadedKeys.add(s3Key);
                String url = s3Service.getPublicUrl(s3Key);
                uploadedUrls.add(url);

                 String derivedType;
                 if (contentType.contains("pdf")) {
                     derivedType = "PDF";
                 } else if (contentType.contains("powerpoint") || contentType.contains("presentation")) {
                     derivedType = "SLIDES";
                 } else if (contentType.startsWith("image/")) {
                     derivedType = "IMAGE";
                 } else {
                     derivedType = "DOCUMENT";
                 }

                 collected.add(new FileMeta(url, f.getOriginalFilename(), f.getSize(), derivedType));

                 // set primary file metadata from first valid file
                if (session.getFileUrl() == null) {
                     session.setFileUrl(url);
                     session.setFileName(f.getOriginalFilename());
                     session.setFileSize(f.getSize());
                     session.setFileType(derivedType);
                 }
             }
            } catch (RuntimeException | Error ex) {
                // Cleanup any uploaded keys to avoid orphaned S3 objects
                for (String key : uploadedKeys) {
                    try {
                        s3Service.deleteFile(key);
                    } catch (Exception e) {
                        log.warn("Failed to cleanup uploaded S3 key {} after error: {}", key, e.getMessage());
                    }
                }
                throw ex;
            }

             try {
                 if (uploadedUrls.size() > 1) {
                     session.setFileUrl(objectMapper.writeValueAsString(uploadedUrls));
                 }
             } catch (JsonProcessingException e) {
                 log.warn("Failed to serialize uploaded URLs: {}", e.getMessage());
             }

             // 3) persist session first so FK relationship is valid
             session = sessionRepository.save(session);

             // 4) create KuppiNote entries for each uploaded file and attach them to session
             if (!collected.isEmpty()) {
                 for (FileMeta fm : collected) {
                     KuppiNote note = KuppiNote.builder()
                             .title(fm.name != null ? fm.name : session.getTitle())
                             .description(null)
                             .fileType(fm.fileType)
                             .fileUrl(fm.url)
                             .fileName(fm.name)
                             .fileSize(fm.size)
                             .session(session)
                             .uploadedBy(findStudentById(currentUserId))
                             .allowDownload(true)
                             .build();

                     note = noteRepository.save(note);
                     session.getNotes().add(note);
                 }
                 // Save session to persist relationship
                 session = sessionRepository.save(session);
             }
         }

         // Save any other changes and return response with notes populated
         session = sessionRepository.save(session);
         KuppiSessionResponse response = kuppiMapper.toResponse(session);
         if (session.getNotes() != null && !session.getNotes().isEmpty()) {
             List<KuppiNote> noteList = new java.util.ArrayList<>(session.getNotes());
             response.setNotes(kuppiMapper.toNoteResponseList(noteList));
         }

         log.info("Kuppi session {} updated with files by student {}", sessionId, currentUserId);
         return response;
     }

    private void tryDeleteS3Object(String url) {
        try {
            // attempt to extract key as in note service
            String bucketUrl = String.format("https://%s.s3.", s3Service.getBucketName());
            if (url.contains(bucketUrl)) {
                int startIndex = url.indexOf(".amazonaws.com/") + 15;
                if (startIndex > 14 && startIndex < url.length()) {
                    String key = url.substring(startIndex);
                    s3Service.deleteFile(key);
                    log.info("Deleted S3 object: {}", url);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to delete S3 object {}: {}", url, e.getMessage());
        }
    }
}

