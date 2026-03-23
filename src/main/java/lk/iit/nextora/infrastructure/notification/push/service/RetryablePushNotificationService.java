package lk.iit.nextora.infrastructure.notification.push.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Retry-enabled Push Notification Service Interface.
 *
 * Provides push notification methods with automatic retry logic
 * for production resilience. Wraps the base PushNotificationService
 * with retry capabilities using Spring Retry.
 */
public interface RetryablePushNotificationService {

    /**
     * Send notification to a single user with retry logic.
     * Retries on FirebaseMessagingException or RuntimeException.
     *
     * @param userId Target user ID
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return CompletableFuture with notification response
     */
    CompletableFuture<NotificationResponse> sendToUserWithRetry(
            Long userId, String title, String body, Map<String, String> data);

    /**
     * Send notification to multiple users with retry logic.
     *
     * @param userIds List of target user IDs
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return CompletableFuture with notification response
     */
    CompletableFuture<NotificationResponse> sendToUsersWithRetry(
            List<Long> userIds, String title, String body, Map<String, String> data);

    /**
     * Send notification to all users with a specific role with retry logic.
     *
     * @param role Target user role
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return CompletableFuture with notification response
     */
    CompletableFuture<NotificationResponse> sendToRoleWithRetry(
            UserRole role, String title, String body, Map<String, String> data);

    /**
     * Send broadcast notification to all users with retry logic.
     *
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return CompletableFuture with notification response
     */
    CompletableFuture<NotificationResponse> sendBroadcastWithRetry(
            String title, String body, Map<String, String> data);
}
