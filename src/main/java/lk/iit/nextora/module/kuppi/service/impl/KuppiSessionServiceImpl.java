package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.enums.KuppiSessionType;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiAnalyticsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
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

    private static final List<KuppiSessionStatus> PUBLIC_STATUSES = List.of(
            KuppiSessionStatus.SCHEDULED,
            KuppiSessionStatus.LIVE,
            KuppiSessionStatus.COMPLETED
    );

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
    public KuppiSessionResponse createSession(CreateKuppiSessionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        Student host = findStudentById(currentUserId);

        // Validate that user is a Kuppi Student
        validateKuppiStudent(host);

        // Validate schedule
        validateSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());

        KuppiSession session = kuppiMapper.toEntity(request);
        session.setHost(host);
        session.setStatus(KuppiSessionStatus.SCHEDULED);

        // Set default values
        if (session.getViewCount() == null) {
            session.setViewCount(0L);
        }
        if (session.getSessionType() == null) {
            session.setSessionType(KuppiSessionType.LIVE);
        }

        session = sessionRepository.save(session);
        log.info("Kuppi session created by student {}: {}", currentUserId, session.getId());

        return kuppiMapper.toResponse(session);
    }

    @Override
    @Transactional
    public KuppiSessionResponse updateSession(Long sessionId, UpdateKuppiSessionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        // Validate ownership
        validateSessionOwnership(session, currentUserId);

        // Validate schedule if changed
        if (request.getScheduledStartTime() != null && request.getScheduledEndTime() != null) {
            validateSchedule(request.getScheduledStartTime(), request.getScheduledEndTime());
        }

        kuppiMapper.updateSessionFromRequest(request, session);
        session = sessionRepository.save(session);

        log.info("Kuppi session {} updated by student {}", sessionId, currentUserId);
        return kuppiMapper.toResponse(session);
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
        return kuppiMapper.toResponse(session);
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        validateSessionOwnership(session, currentUserId);

        // Validate session can be deleted
        if (KuppiSessionStatus.LIVE.equals(session.getStatus())) {
            throw new BadRequestException("Cannot delete a session that is currently live");
        }

        session.softDelete();
        sessionRepository.save(session);

        log.info("Kuppi session {} deleted by student {}", sessionId, currentUserId);
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
    @Transactional
    public KuppiSessionResponse adminUpdateSession(Long sessionId, UpdateKuppiSessionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        kuppiMapper.updateSessionFromRequest(request, session);
        session = sessionRepository.save(session);

        log.info("Kuppi session {} updated by admin {}", sessionId, currentUserId);
        return kuppiMapper.toResponse(session);
    }

    @Override
    @Transactional
    public void adminDeleteSession(Long sessionId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = findSessionById(sessionId);

        session.softDelete();
        sessionRepository.save(session);

        log.info("Kuppi session {} soft deleted by admin {}", sessionId, currentUserId);
    }

    @Override
    public KuppiPlatformStatsResponse getPlatformStats() {
        long totalSessions = sessionRepository.count();
        long completed = sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.COMPLETED);
        long cancelled = sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.CANCELLED);

        long totalNotes = noteRepository.count();

        return KuppiPlatformStatsResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(completed)
                .cancelledSessions(cancelled)
                .totalNotes(totalNotes)
                .build();
    }

    // ==================== Super Admin Operations ====================

    @Override
    @Transactional
    public void permanentlyDeleteSession(Long sessionId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiSession", "id", sessionId));

        sessionRepository.delete(session);
        log.info("Kuppi session {} permanently deleted by super admin {}", sessionId, currentUserId);
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
}

