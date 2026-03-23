package lk.iit.nextora.infrastructure.notification.push.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lk.iit.nextora.infrastructure.notification.push.service.RetryablePushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Retry-enabled Push Notification Service.
 *
 * Wraps PushNotificationService with retry logic for production resilience.
 * Uses Spring Retry with exponential backoff strategy.
 *
 * Retry Configuration:
 * - Max attempts: 3
 * - Initial delay: 1000ms
 * - Multiplier: 2 (exponential backoff)
 * - Retries on: FirebaseMessagingException, RuntimeException
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryablePushNotificationServiceImpl implements RetryablePushNotificationService {

    private final PushNotificationService pushNotificationService;

    @Value("${app.notification.retry-attempts:3}")
    private int maxRetries;

    @Override
    @Retryable(
        retryFor = {FirebaseMessagingException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<NotificationResponse> sendToUserWithRetry(
            Long userId, String title, String body, Map<String, String> data) {

        log.debug("Attempting to send notification to user: {} (with retry)", userId);
        return pushNotificationService.sendToUserAsync(userId, title, body, data);
    }

    @Override
    @Retryable(
        retryFor = {FirebaseMessagingException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<NotificationResponse> sendToUsersWithRetry(
            List<Long> userIds, String title, String body, Map<String, String> data) {

        log.debug("Attempting to send notification to {} users (with retry)", userIds.size());
        return pushNotificationService.sendToUsersAsync(userIds, title, body, data);
    }

    @Override
    @Retryable(
        retryFor = {FirebaseMessagingException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<NotificationResponse> sendToRoleWithRetry(
            UserRole role, String title, String body, Map<String, String> data) {

        log.debug("Attempting to send notification to role: {} (with retry)", role);
        return pushNotificationService.sendToRoleAsync(role, title, body, data);
    }

    @Override
    @Retryable(
        retryFor = {FirebaseMessagingException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<NotificationResponse> sendBroadcastWithRetry(
            String title, String body, Map<String, String> data) {

        log.debug("Attempting to send broadcast notification (with retry)");
        return pushNotificationService.sendToAllAsync(title, body, data);
    }

    /**
     * Recovery method when all retries fail for sendToUserWithRetry.
     */
    @Recover
    public CompletableFuture<NotificationResponse> recoverSendToUser(
            Exception ex, Long userId, String title, String body, Map<String, String> data) {
        log.error("All retry attempts failed for push notification to user {}: {}", userId, ex.getMessage());
        return CompletableFuture.completedFuture(buildFailureResponse(ex));
    }

    /**
     * Recovery method when all retries fail for sendToUsersWithRetry.
     */
    @Recover
    public CompletableFuture<NotificationResponse> recoverSendToUsers(
            Exception ex, List<Long> userIds, String title, String body, Map<String, String> data) {
        log.error("All retry attempts failed for push notification to {} users: {}", userIds.size(), ex.getMessage());
        return CompletableFuture.completedFuture(buildFailureResponse(ex));
    }

    /**
     * Recovery method when all retries fail for sendToRoleWithRetry.
     */
    @Recover
    public CompletableFuture<NotificationResponse> recoverSendToRole(
            Exception ex, UserRole role, String title, String body, Map<String, String> data) {
        log.error("All retry attempts failed for push notification to role {}: {}", role, ex.getMessage());
        return CompletableFuture.completedFuture(buildFailureResponse(ex));
    }

    /**
     * Recovery method when all retries fail for sendBroadcastWithRetry.
     */
    @Recover
    public CompletableFuture<NotificationResponse> recoverSendBroadcast(
            Exception ex, String title, String body, Map<String, String> data) {
        log.error("All retry attempts failed for broadcast notification: {}", ex.getMessage());
        return CompletableFuture.completedFuture(buildFailureResponse(ex));
    }

    /**
     * Build a failure response for recovery methods.
     */
    private NotificationResponse buildFailureResponse(Exception ex) {
        return NotificationResponse.builder()
                .totalAttempted(0)
                .successCount(0)
                .failureCount(1)
                .message("Failed to send notification after retries: " + ex.getMessage())
                .invalidTokens(Collections.emptyList())
                .messageIds(Collections.emptyList())
                .build();
    }
}
