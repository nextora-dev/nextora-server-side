package lk.iit.nextora.module.kuppi.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiAnalyticsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Service interface for Kuppi session operations
 * Users click on session liveLink to join the session on external platform
 */
public interface KuppiSessionService {

    // ==================== Normal Student Operations ====================

    /**
     * View all approved/public Kuppi sessions
     */
    PagedResponse<KuppiSessionResponse> getPublicSessions(Pageable pageable);


    /**
     * View a specific Kuppi session (increments view count)
     */
    KuppiSessionResponse getSessionById(Long sessionId);

    /**
     * Search sessions by keyword (title/subject)
     */
    PagedResponse<KuppiSessionResponse> searchSessions(String keyword, Pageable pageable);

    /**
     * Search sessions by subject
     */
    PagedResponse<KuppiSessionResponse> searchBySubject(String subject, Pageable pageable);

    /**
     * Search sessions by host name (lecturer)
     */
    PagedResponse<KuppiSessionResponse> searchByHostName(String hostName, Pageable pageable);

    /**
     * Search sessions by date range
     */
    PagedResponse<KuppiSessionResponse> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Get upcoming sessions
     */
    PagedResponse<KuppiSessionResponse> getUpcomingSessions(Pageable pageable);


    // ==================== Kuppi Student Operations ====================

    /**
     * Create a new Kuppi session with optional uploaded files (supports single or multiple files)
     */
    KuppiSessionResponse createSession(CreateKuppiSessionRequest request, MultipartFile[] files);

    /**
     * Update own Kuppi session including optional uploaded files (supports single or multiple files)
     * Accepts an optional list of note IDs to remove before uploading new files. If null, existing notes will be removed when new files are provided.
     */
    KuppiSessionResponse updateSession(Long sessionId, UpdateKuppiSessionRequest request, MultipartFile[] files, java.util.List<Long> removeNoteIds);

    /**
     * Cancel own Kuppi session
     */
    void cancelSession(Long sessionId, String reason);

    /**
     * Reschedule own Kuppi session
     */
    KuppiSessionResponse rescheduleSession(Long sessionId, LocalDateTime newStartTime, LocalDateTime newEndTime);

    /**
     * Soft-delete a session and remove associated note files from storage (best-effort).
     * Only the owner may soft-delete their session.
     */
    void softDeleteSession(Long sessionId);

    /**
     * Get own sessions
     */
    PagedResponse<KuppiSessionResponse> getMySessions(Pageable pageable);

    /**
     * Get analytics for own sessions
     */
    KuppiAnalyticsResponse getMyAnalytics();

    // ==================== Admin Operations ====================

    /**
     * Get platform statistics
     */
    KuppiPlatformStatsResponse getPlatformStats();

    // ==================== Super Admin Operations ====================

    /**
     * Permanently delete a session and all associated files/notes from storage and database.
     * Intended for super-admin or privileged operations.
     */
    void permanentlyDeleteSession(Long sessionId);
}
