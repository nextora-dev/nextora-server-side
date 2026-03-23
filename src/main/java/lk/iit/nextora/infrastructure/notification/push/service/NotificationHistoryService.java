package lk.iit.nextora.infrastructure.notification.push.service;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.infrastructure.notification.push.entity.Notification;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Notification History Service Interface.
 *
 * Provides methods for storing and retrieving notification history
 * for users. Enables users to view past notifications.
 */
public interface NotificationHistoryService {

    /**
     * Store a notification in the user's history.
     *
     * @param userId The user's ID
     * @param title Notification title
     * @param body Notification body
     * @param type Notification type for categorization
     * @param clickAction Optional click action URL
     * @param imageUrl Optional image URL
     * @param data Additional custom data
     * @return The stored Notification entity, or null if user not found
     */
    Notification storeNotification(Long userId, String title, String body, NotificationType type,
                                   String clickAction, String imageUrl, Map<String, String> data);

    /**
     * Store notifications for multiple users.
     *
     * @param userIds List of user IDs
     * @param title Notification title
     * @param body Notification body
     * @param type Notification type
     * @param clickAction Optional click action URL
     * @param imageUrl Optional image URL
     * @param data Additional custom data
     */
    void storeNotificationForUsers(List<Long> userIds, String title, String body, NotificationType type,
                                   String clickAction, String imageUrl, Map<String, String> data);

    /**
     * Get all notifications for a user, ordered by sent time descending.
     *
     * @param userId The user's ID
     * @return List of notifications
     */
    List<Notification> getNotificationsForUser(Long userId);

    /**
     * Get notifications for a user with pagination.
     *
     * @param userId The user's ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of notifications
     */
    Page<Notification> getNotificationsForUser(Long userId, int page, int size);

    /**
     * Get unread notifications for a user.
     *
     * @param userId The user's ID
     * @return List of unread notifications
     */
    List<Notification> getUnreadNotificationsForUser(Long userId);

    /**
     * Get unread notification count for a user.
     *
     * @param userId The user's ID
     * @return Count of unread notifications
     */
    long getUnreadCount(Long userId);

    /**
     * Mark a specific notification as read.
     *
     * @param notificationId The notification ID
     */
    void markAsRead(Long notificationId);

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId The user's ID
     * @return Number of notifications marked as read
     */
    int markAllAsRead(Long userId);

    /**
     * Delete old notifications for cleanup.
     * Should be called by a scheduled job.
     *
     * @param daysOld Delete notifications older than this many days
     * @return Number of notifications deleted
     */
    int deleteOldNotifications(int daysOld);
}
