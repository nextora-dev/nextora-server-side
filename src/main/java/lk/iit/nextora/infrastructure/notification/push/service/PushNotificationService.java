package lk.iit.nextora.infrastructure.notification.push.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.request.RegisterTokenRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.request.SendNotificationRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.dto.response.TokenRegistrationResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Push Notification Service Interface.
 *
 * Design Principles:
 * - Single Responsibility: Each method handles one specific use case
 * - Async-first: All sending operations support async execution
 * - Graceful degradation: Handle failures without crashing the application
 * - Clean abstraction: Hides FCM implementation details from callers
 */
public interface PushNotificationService {

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Register or update an FCM token for the current authenticated user.
     * If the token already exists for a different user, it will be reassigned.
     *
     * @param userId The user's ID
     * @param role The user's current role
     * @param request Token registration request containing the FCM token
     * @return Registration response with token ID and status
     */
    TokenRegistrationResponse registerToken(Long userId, UserRole role, RegisterTokenRequest request);

    /**
     * Remove/deactivate a specific FCM token (e.g., on logout from a device).
     *
     * @param token The FCM token to remove
     */
    void removeToken(String token);

    /**
     * Deactivate all FCM tokens for a user (e.g., on logout from all devices or account deletion).
     *
     * @param userId The user's ID
     */
    void removeAllTokensForUser(Long userId);

    // ==================== NOTIFICATION SENDING - SYNC ====================

    /**
     * Send notification to a single user.
     *
     * @param userId Target user ID
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return Notification response with delivery status
     */
    NotificationResponse sendToUser(Long userId, String title, String body, Map<String, String> data);

    /**
     * Send notification to multiple users.
     *
     * @param userIds List of target user IDs
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return Notification response with delivery status
     */
    NotificationResponse sendToUsers(List<Long> userIds, String title, String body, Map<String, String> data);

    /**
     * Send notification to all users with a specific role.
     *
     * @param role Target role
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return Notification response with delivery status
     */
    NotificationResponse sendToRole(UserRole role, String title, String body, Map<String, String> data);

    /**
     * Send notification to all active users (broadcast).
     *
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data payload
     * @return Notification response with delivery status
     */
    NotificationResponse sendToAll(String title, String body, Map<String, String> data);

    /**
     * Generic send method that handles all targeting options from request.
     *
     * @param request Send notification request with targeting options
     * @return Notification response with delivery status
     */
    NotificationResponse send(SendNotificationRequest request);

    // ==================== NOTIFICATION SENDING - ASYNC ====================

    /**
     * Async version: Send notification to a single user.
     * Non-blocking, returns immediately with a Future.
     */
    CompletableFuture<NotificationResponse> sendToUserAsync(Long userId, String title, String body, Map<String, String> data);

    /**
     * Async version: Send notification to multiple users.
     * Non-blocking, returns immediately with a Future.
     */
    CompletableFuture<NotificationResponse> sendToUsersAsync(List<Long> userIds, String title, String body, Map<String, String> data);

    /**
     * Async version: Send notification to all users with a specific role.
     * Non-blocking, returns immediately with a Future.
     */
    CompletableFuture<NotificationResponse> sendToRoleAsync(UserRole role, String title, String body, Map<String, String> data);

    /**
     * Async version: Send notification to all active users (broadcast).
     * Non-blocking, returns immediately with a Future.
     */
    CompletableFuture<NotificationResponse> sendToAllAsync(String title, String body, Map<String, String> data);

    /**
     * Async version: Generic send method.
     * Non-blocking, returns immediately with a Future.
     */
    CompletableFuture<NotificationResponse> sendAsync(SendNotificationRequest request);

    // ==================== UTILITY ====================

    /**
     * Check if push notifications are enabled and configured.
     *
     * @return true if Firebase is initialized and ready
     */
    boolean isEnabled();

    /**
     * Clean up stale/unused tokens older than the specified days.
     * Should be called by a scheduled job.
     *
     * @param daysOld Number of days after which a token is considered stale
     * @return Number of tokens deleted
     */
    int cleanupStaleTokens(int daysOld);
}
