package lk.iit.nextora.infrastructure.notification.push.repository;

import lk.iit.nextora.infrastructure.notification.push.entity.Notification;
import lk.iit.nextora.common.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Notification Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications for a user, ordered by sent time descending.
     */
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);

    /**
     * Find notifications for a user with pagination.
     */
    Page<Notification> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user.
     */
    List<Notification> findByUserIdAndReadFalseOrderBySentAtDesc(Long userId);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Find notifications by type for a user.
     */
    List<Notification> findByUserIdAndTypeOrderBySentAtDesc(Long userId, NotificationType type);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.user.id = :userId AND n.read = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("now") ZonedDateTime now);

    /**
     * Delete old notifications (for cleanup).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") ZonedDateTime cutoffDate);

    /**
     * Find recent notifications across all users (for admin dashboard).
     */
    List<Notification> findTop50ByOrderBySentAtDesc();
}
