package lk.iit.nextora.module.kuppi.repository;

import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KuppiSessionRepository extends JpaRepository<KuppiSession, Long> {

    // Find by host
    Page<KuppiSession> findByHostIdAndIsDeletedFalse(Long hostId, Pageable pageable);

    List<KuppiSession> findByHostIdAndIsDeletedFalse(Long hostId);

    // Find by status
    Page<KuppiSession> findByStatusAndIsDeletedFalse(KuppiSessionStatus status, Pageable pageable);

    // Find approved/public sessions
    @Query("SELECT k FROM KuppiSession k WHERE k.status IN :statuses AND k.isDeleted = false")
    Page<KuppiSession> findByStatusInAndIsDeletedFalse(@Param("statuses") List<KuppiSessionStatus> statuses, Pageable pageable);

    // Search by subject
    @Query("SELECT k FROM KuppiSession k WHERE LOWER(k.subject) LIKE LOWER(CONCAT('%', :subject, '%')) " +
           "AND k.status IN :statuses AND k.isDeleted = false")
    Page<KuppiSession> searchBySubject(@Param("subject") String subject,
                                        @Param("statuses") List<KuppiSessionStatus> statuses,
                                        Pageable pageable);

    // Search by title or subject
    @Query("SELECT k FROM KuppiSession k WHERE " +
           "(LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(k.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND k.status IN :statuses AND k.isDeleted = false")
    Page<KuppiSession> searchByKeyword(@Param("keyword") String keyword,
                                        @Param("statuses") List<KuppiSessionStatus> statuses,
                                        Pageable pageable);

    // Search by host name (lecturer)
    @Query("SELECT k FROM KuppiSession k JOIN k.host h WHERE " +
           "(LOWER(h.firstName) LIKE LOWER(CONCAT('%', :hostName, '%')) OR " +
           "LOWER(h.lastName) LIKE LOWER(CONCAT('%', :hostName, '%'))) " +
           "AND k.status IN :statuses AND k.isDeleted = false")
    Page<KuppiSession> searchByHostName(@Param("hostName") String hostName,
                                         @Param("statuses") List<KuppiSessionStatus> statuses,
                                         Pageable pageable);

    // Find by date range
    @Query("SELECT k FROM KuppiSession k WHERE k.scheduledStartTime BETWEEN :startDate AND :endDate " +
           "AND k.status IN :statuses AND k.isDeleted = false")
    Page<KuppiSession> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("statuses") List<KuppiSessionStatus> statuses,
                                        Pageable pageable);

    // Find upcoming sessions
    @Query("SELECT k FROM KuppiSession k WHERE k.scheduledStartTime > :now " +
           "AND k.status IN :statuses AND k.isDeleted = false ORDER BY k.scheduledStartTime ASC")
    Page<KuppiSession> findUpcomingSessions(@Param("now") LocalDateTime now,
                                             @Param("statuses") List<KuppiSessionStatus> statuses,
                                             Pageable pageable);

    // Find by ID with eager loading
    @Query("SELECT k FROM KuppiSession k LEFT JOIN FETCH k.host WHERE k.id = :id AND k.isDeleted = false")
    Optional<KuppiSession> findByIdWithHost(@Param("id") Long id);

    // Count by host
    long countByHostIdAndIsDeletedFalse(Long hostId);

    // Count by status
    long countByStatusAndIsDeletedFalse(KuppiSessionStatus status);

    // Count by host and status
    long countByHostIdAndStatusAndIsDeletedFalse(Long hostId, KuppiSessionStatus status);

    // Analytics - total views for a host
    @Query("SELECT SUM(k.viewCount) FROM KuppiSession k WHERE k.host.id = :hostId AND k.isDeleted = false")
    Long getTotalViewsByHost(@Param("hostId") Long hostId);

    // Find recent sessions by host (limit by pageable)
    @Query("SELECT k FROM KuppiSession k WHERE k.host.id = :hostId AND k.isDeleted = false " +
           "ORDER BY k.scheduledStartTime DESC")
    List<KuppiSession> findRecentSessionsByHost(@Param("hostId") Long hostId, Pageable pageable);

    // Find upcoming sessions by host
    @Query("SELECT k FROM KuppiSession k WHERE k.host.id = :hostId " +
           "AND k.scheduledStartTime > :now AND k.status = :status AND k.isDeleted = false " +
           "ORDER BY k.scheduledStartTime ASC")
    List<KuppiSession> findUpcomingSessionsByHost(@Param("hostId") Long hostId,
                                                   @Param("now") LocalDateTime now,
                                                   @Param("status") KuppiSessionStatus status,
                                                   Pageable pageable);

    // Find pending approval sessions (for admin)
    Page<KuppiSession> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(KuppiSessionStatus status, Pageable pageable);

    // ==================== Status Transition Queries ====================

    /**
     * Find SCHEDULED sessions that should be LIVE (current time is between start and end time)
     */
    @Query("SELECT k FROM KuppiSession k WHERE k.status = :scheduledStatus " +
           "AND k.scheduledStartTime <= :now AND k.scheduledEndTime > :now " +
           "AND k.isDeleted = false")
    List<KuppiSession> findSessionsToGoLive(@Param("scheduledStatus") KuppiSessionStatus scheduledStatus,
                                             @Param("now") LocalDateTime now);

    /**
     * Find LIVE sessions that should be COMPLETED (current time is past end time)
     */
    @Query("SELECT k FROM KuppiSession k WHERE k.status = :liveStatus " +
           "AND k.scheduledEndTime <= :now " +
           "AND k.isDeleted = false")
    List<KuppiSession> findSessionsToComplete(@Param("liveStatus") KuppiSessionStatus liveStatus,
                                               @Param("now") LocalDateTime now);

    /**
     * Find SCHEDULED sessions that were missed (start and end time both passed, but never went LIVE)
     */
    @Query("SELECT k FROM KuppiSession k WHERE k.status = :scheduledStatus " +
           "AND k.scheduledEndTime <= :now " +
           "AND k.isDeleted = false")
    List<KuppiSession> findMissedScheduledSessions(@Param("scheduledStatus") KuppiSessionStatus scheduledStatus,
                                                    @Param("now") LocalDateTime now);

    /**
     * Find SCHEDULED sessions starting between two times (for reminder notifications).
     * Used to find sessions starting within the next N minutes.
     */
    @Query("SELECT k FROM KuppiSession k WHERE k.status = :status " +
           "AND k.scheduledStartTime BETWEEN :from AND :to " +
           "AND k.isDeleted = false")
    List<KuppiSession> findSessionsStartingBetween(@Param("status") KuppiSessionStatus status,
                                                    @Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);
}
