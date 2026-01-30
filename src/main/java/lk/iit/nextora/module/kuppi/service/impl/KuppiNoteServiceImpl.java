package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.NoteDownloadResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.mapper.KuppiMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiNoteRepository;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KuppiNoteServiceImpl implements KuppiNoteService {

    private static final String KUPPI_NOTES_FOLDER = "kuppi-notes";

    private final KuppiNoteRepository noteRepository;
    private final KuppiSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;
    private final KuppiMapper kuppiMapper;
    private final S3Service s3Service;

    // ==================== Normal Student Operations ====================

    @Override
    public PagedResponse<KuppiNoteResponse> getApprovedNotes(Pageable pageable) {
        Page<KuppiNote> notes = noteRepository.findByIsDeletedFalse(pageable);
        return toPagedResponse(notes);
    }

    @Override
    public List<KuppiNoteResponse> getNotesForSession(Long sessionId) {
        List<KuppiNote> notes = noteRepository.findApprovedNotesBySession(sessionId);
        return kuppiMapper.toNoteResponseList(notes);
    }

    @Override
    @Transactional
    public KuppiNoteResponse getNoteById(Long noteId) {
        KuppiNote note = findNoteById(noteId);
        note.incrementViewCount();
        noteRepository.save(note);
        return kuppiMapper.toResponse(note);
    }

    @Override
    @Transactional
    public KuppiNoteResponse downloadNote(Long noteId) {
        KuppiNote note = findNoteById(noteId);

        if (!note.isDownloadable()) {
            throw new BadRequestException("This note is not available for download");
        }

        note.incrementDownloadCount();
        noteRepository.save(note);

        log.info("Note {} downloaded", noteId);
        return kuppiMapper.toResponse(note);
    }

    @Override
    public PagedResponse<KuppiNoteResponse> searchNotes(String keyword, Pageable pageable) {
        Page<KuppiNote> notes = noteRepository.searchByTitle(keyword, pageable);
        return toPagedResponse(notes);
    }

    // ==================== Kuppi Student Operations ====================

    @Override
    @Transactional
    public KuppiNoteResponse uploadNoteWithFile(CreateKuppiNoteRequest request, MultipartFile file) {
        Long currentUserId = securityService.getCurrentUserId();
        Student uploader = findStudentById(currentUserId);

        // Validate that user is a Kuppi Student
        validateKuppiStudent(uploader);

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("application/pdf")
                && !contentType.startsWith("application/vnd.ms-powerpoint")
                && !contentType.startsWith("application/vnd.openxmlformats-officedocument")
                && !contentType.startsWith("image/"))) {
            throw new BadRequestException("Invalid file type. Only PDF, PowerPoint, and image files are allowed");
        }

        // Upload file to S3
        String s3Key = s3Service.uploadFile(file, KUPPI_NOTES_FOLDER);
        String fileUrl = s3Service.getPublicUrl(s3Key);

        // Create note entity
        KuppiNote note = kuppiMapper.toEntity(request);
        note.setUploadedBy(uploader);
        note.setFileUrl(fileUrl);
        note.setFileName(file.getOriginalFilename());
        note.setFileSize(file.getSize());

        // Determine file type from content type
        if (contentType.contains("pdf")) {
            note.setFileType("PDF");
        } else if (contentType.contains("powerpoint") || contentType.contains("presentation")) {
            note.setFileType("SLIDES");
        } else if (contentType.startsWith("image/")) {
            note.setFileType("IMAGE");
        } else {
            note.setFileType("DOCUMENT");
        }

        // Link to session if provided
        if (request.getSessionId() != null) {
            KuppiSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("KuppiSession", "id", request.getSessionId()));

            // Validate session ownership
            if (!session.getHost().getId().equals(currentUserId)) {
                throw new UnauthorizedException("You can only add notes to your own sessions");
            }
            note.setSession(session);
        }

        if (request.getAllowDownload() != null) {
            note.setAllowDownload(request.getAllowDownload());
        }

        note = noteRepository.save(note);
        log.info("Note with file uploaded by student {} to S3: {}", currentUserId, note.getId());

        return kuppiMapper.toResponse(note);
    }

    @Override
    @Transactional
    public NoteDownloadResponse downloadNoteFile(Long noteId) {
        KuppiNote note = findNoteById(noteId);

        if (!note.isDownloadable()) {
            throw new BadRequestException("This note is not available for download");
        }

        if (note.getFileUrl() == null || note.getFileUrl().isEmpty()) {
            throw new ResourceNotFoundException("File not found for note", "id", noteId);
        }

        // Extract S3 key from the file URL
        String s3Key = extractS3KeyFromUrl(note.getFileUrl());

        // Check if file exists in S3
        if (!s3Service.fileExists(s3Key)) {
            throw new ResourceNotFoundException("File not found in storage", "key", s3Key);
        }

        // Download file from S3
        byte[] fileContent = s3Service.downloadFile(s3Key);

        // Increment download count
        note.incrementDownloadCount();
        noteRepository.save(note);

        log.info("Note {} downloaded", noteId);

        return NoteDownloadResponse.builder()
                .noteId(noteId)
                .fileName(note.getFileName())
                .contentType(getContentTypeFromFileType(note.getFileType()))
                .fileSize(note.getFileSize())
                .fileContent(fileContent)
                .downloadUrl(note.getFileUrl())
                .build();
    }

    @Override
    @Transactional
    public KuppiNoteResponse updateNoteWithFile(Long noteId, UpdateKuppiNoteRequest request, MultipartFile file) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiNote note = findNoteById(noteId);

        validateNoteOwnership(note, currentUserId);

        // Update basic fields from request
        kuppiMapper.updateNoteFromRequest(request, note);

        // Handle file upload if provided
        if (file != null && !file.isEmpty()) {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("application/pdf")
                    && !contentType.startsWith("application/vnd.ms-powerpoint")
                    && !contentType.startsWith("application/vnd.openxmlformats-officedocument")
                    && !contentType.startsWith("image/"))) {
                throw new BadRequestException("Invalid file type. Only PDF, PowerPoint, and image files are allowed");
            }

            // Delete old file from S3 if exists
            if (note.getFileUrl() != null && !note.getFileUrl().isEmpty()) {
                try {
                    String oldS3Key = extractS3KeyFromUrl(note.getFileUrl());
                    if (s3Service.fileExists(oldS3Key)) {
                        s3Service.deleteFile(oldS3Key);
                        log.info("Deleted old file from S3: {}", oldS3Key);
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete old file from S3: {}", e.getMessage());
                }
            }

            // Upload new file to S3
            String s3Key = s3Service.uploadFile(file, KUPPI_NOTES_FOLDER);
            String fileUrl = s3Service.getPublicUrl(s3Key);

            note.setFileUrl(fileUrl);
            note.setFileName(file.getOriginalFilename());
            note.setFileSize(file.getSize());

            // Determine file type from content type
            if (contentType.contains("pdf")) {
                note.setFileType("PDF");
            } else if (contentType.contains("powerpoint") || contentType.contains("presentation")) {
                note.setFileType("SLIDES");
            } else if (contentType.startsWith("image/")) {
                note.setFileType("IMAGE");
            } else {
                note.setFileType("DOCUMENT");
            }
        }

        note = noteRepository.save(note);
        log.info("Note {} updated with file by student {}", noteId, currentUserId);

        return kuppiMapper.toResponse(note);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiNote note = findNoteById(noteId);

        validateNoteOwnership(note, currentUserId);

        note.softDelete();
        noteRepository.save(note);

        log.info("Note {} deleted by student {}", noteId, currentUserId);
    }

    @Override
    public PagedResponse<KuppiNoteResponse> getMyNotes(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<KuppiNote> notes = noteRepository.findByUploadedByIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(notes);
    }

    // ==================== Admin Operations ====================

    @Override
    @Transactional
    public KuppiNoteResponse adminUpdateNote(Long noteId, UpdateKuppiNoteRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiNote note = findNoteById(noteId);

        kuppiMapper.updateNoteFromRequest(request, note);
        note = noteRepository.save(note);

        log.info("Note {} updated by admin {}", noteId, currentUserId);
        return kuppiMapper.toResponse(note);
    }

    @Override
    @Transactional
    public void adminDeleteNote(Long noteId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiNote note = findNoteById(noteId);

        note.softDelete();
        noteRepository.save(note);

        log.info("Note {} soft deleted by admin {}", noteId, currentUserId);
    }

    // ==================== Super Admin Operations ====================

    @Override
    @Transactional
    public void permanentlyDeleteNote(Long noteId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiNote", "id", noteId));

        noteRepository.delete(note);
        log.info("Note {} permanently deleted by super admin {}", noteId, currentUserId);
    }

    // ==================== Helper Methods ====================

    private KuppiNote findNoteById(Long noteId) {
        return noteRepository.findByIdWithDetails(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiNote", "id", noteId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private void validateKuppiStudent(Student student) {
        // Check for both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
        if (!student.hasKuppiCapability()) {
            throw new UnauthorizedException("Only Kuppi Students can upload notes");
        }
    }

    private void validateNoteOwnership(KuppiNote note, Long userId) {
        if (!note.getUploadedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own notes");
        }
    }

    private PagedResponse<KuppiNoteResponse> toPagedResponse(Page<KuppiNote> notes) {
        List<KuppiNoteResponse> content = kuppiMapper.toNoteResponseList(notes.getContent());
        return PagedResponse.<KuppiNoteResponse>builder()
                .content(content)
                .pageNumber(notes.getNumber())
                .pageSize(notes.getSize())
                .totalElements(notes.getTotalElements())
                .totalPages(notes.getTotalPages())
                .first(notes.isFirst())
                .last(notes.isLast())
                .empty(notes.isEmpty())
                .build();
    }

    /**
     * Extract S3 key from the full S3 URL
     */
    private String extractS3KeyFromUrl(String url) {
        // URL format: https://bucket-name.s3.region.amazonaws.com/key
        if (url == null || url.isEmpty()) {
            throw new BadRequestException("Invalid file URL");
        }

        // Extract the key part after the bucket URL
        String bucketUrl = String.format("https://%s.s3.", s3Service.getBucketName());
        if (url.contains(bucketUrl)) {
            int startIndex = url.indexOf(".amazonaws.com/") + 15;
            if (startIndex > 14 && startIndex < url.length()) {
                return url.substring(startIndex);
            }
        }

        throw new BadRequestException("Invalid S3 URL format");
    }

    /**
     * Get content type from file type
     */
    private String getContentTypeFromFileType(String fileType) {
        if (fileType == null) {
            return "application/octet-stream";
        }

        return switch (fileType.toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "SLIDES" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "IMAGE" -> "image/png";
            case "DOCUMENT" -> "application/octet-stream";
            default -> "application/octet-stream";
        };
    }
}