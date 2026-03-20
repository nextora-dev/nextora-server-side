package lk.iit.nextora.infrastructure.notification.push.service.impl;

import com.google.firebase.messaging.*;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.request.RegisterTokenRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.request.SendNotificationRequest;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.dto.response.TokenRegistrationResponse;
import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import lk.iit.nextora.infrastructure.notification.push.repository.FcmTokenRepository;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of Push Notification Service using Firebase Cloud Messaging.
 *
 * Design Decisions:
 * - Uses FCM's batch sending for efficiency (up to 500 messages per batch)
 * - Invalid tokens are automatically cleaned up on send failure
 * - All async methods use Spring's @Async for non-blocking execution
 * - Graceful degradation: if Firebase is not configured, operations log warnings and return safely
 *
 * Error Handling:
 * - MessagingErrorCode.UNREGISTERED: Token is invalid, remove it
 * - MessagingErrorCode.INVALID_ARGUMENT: Token format is wrong, remove it
 * - Other errors: Log and continue, don't remove token
 */
@Service
@Slf4j
@Transactional
public class PushNotificationServiceImpl implements PushNotificationService {

    private final FcmTokenRepository fcmTokenRepository;
    private final Optional<FirebaseMessaging> firebaseMessaging;

    // Maximum tokens per batch (FCM limit is 500)
    private static final int BATCH_SIZE = 500;

    public PushNotificationServiceImpl(
            FcmTokenRepository fcmTokenRepository,
            Optional<FirebaseMessaging> optionalFirebaseMessaging) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.firebaseMessaging = optionalFirebaseMessaging;

        if (firebaseMessaging.isEmpty()) {
            log.warn("PushNotificationService initialized without Firebase - push notifications will be disabled");
        } else {
            log.info("PushNotificationService initialized with Firebase Cloud Messaging");
        }
    }

    /**
     * Check if Firebase is available for sending notifications.
     */
    private boolean isFirebaseAvailable() {
        return firebaseMessaging.isPresent();
    }

    // ==================== TOKEN MANAGEMENT ====================

    @Override
    public TokenRegistrationResponse registerToken(Long userId, UserRole role, RegisterTokenRequest request) {
        String tokenValue = request.getToken();

        // Check if token already exists
        Optional<FcmToken> existingToken = fcmTokenRepository.findByToken(tokenValue);

        if (existingToken.isPresent()) {
            FcmToken token = existingToken.get();

            // If token belongs to a different user, reassign it
            // (This handles the case where a user logs out and another logs in on the same device)
            if (!token.getUserId().equals(userId)) {
                log.info("Reassigning FCM token from user {} to user {}", token.getUserId(), userId);
            }

            // Update token metadata
            token.setUserId(userId);
            token.setRole(role);
            token.setIsActive(true);
            token.setDeviceInfo(request.getDeviceInfo());

            FcmToken saved = fcmTokenRepository.save(token);
            log.debug("Updated FCM token for user {}", userId);

            return TokenRegistrationResponse.updated(saved.getId(), saved.getUpdatedAt());
        }

        // Create new token
        FcmToken newToken = FcmToken.builder()
                .token(tokenValue)
                .userId(userId)
                .role(role)
                .isActive(true)
                .deviceInfo(request.getDeviceInfo())
                .build();

        FcmToken saved = fcmTokenRepository.save(newToken);
        log.info("Registered new FCM token for user {}", userId);

        return TokenRegistrationResponse.created(saved.getId(), saved.getCreatedAt());
    }

    @Override
    public void removeToken(String token) {
        int updated = fcmTokenRepository.deactivateToken(token, ZonedDateTime.now());
        if (updated > 0) {
            log.debug("Deactivated FCM token");
        }
    }

    @Override
    public void removeAllTokensForUser(Long userId) {
        int updated = fcmTokenRepository.deactivateAllTokensForUser(userId, ZonedDateTime.now());
        log.info("Deactivated {} FCM tokens for user {}", updated, userId);
    }

    // ==================== NOTIFICATION SENDING - SYNC ====================

    @Override
    public NotificationResponse sendToUser(Long userId, String title, String body, Map<String, String> data) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);
        return sendToTokens(tokens, title, body, null, data, null, null);
    }

    @Override
    public NotificationResponse sendToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) {
            return NotificationResponse.noTargets();
        }
        List<FcmToken> tokens = fcmTokenRepository.findActiveTokensByUserIds(userIds);
        return sendToTokens(tokens, title, body, null, data, null, null);
    }

    @Override
    public NotificationResponse sendToRole(UserRole role, String title, String body, Map<String, String> data) {
        List<FcmToken> tokens = fcmTokenRepository.findByRoleAndIsActiveTrue(role);
        return sendToTokens(tokens, title, body, null, data, null, null);
    }

    @Override
    public NotificationResponse sendToAll(String title, String body, Map<String, String> data) {
        List<FcmToken> tokens = fcmTokenRepository.findByIsActiveTrue();
        return sendToTokens(tokens, title, body, null, data, null, null);
    }

    @Override
    public NotificationResponse send(SendNotificationRequest request) {
        List<FcmToken> tokens;

        // Priority: userIds > targetRole > broadcast
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            tokens = fcmTokenRepository.findActiveTokensByUserIds(request.getUserIds());
        } else if (request.getTargetRole() != null) {
            tokens = fcmTokenRepository.findByRoleAndIsActiveTrue(request.getTargetRole());
        } else {
            // No specific target - this could be a broadcast or topic-based
            // For now, we don't support broadcast without explicit targeting for safety
            log.warn("Send notification called without targets - ignoring");
            return NotificationResponse.noTargets();
        }

        return sendToTokens(
                tokens,
                request.getTitle(),
                request.getBody(),
                request.getImageUrl(),
                request.getData(),
                request.getClickAction(),
                request.getTtlSeconds()
        );
    }

    // ==================== NOTIFICATION SENDING - ASYNC ====================

    @Override
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> sendToUserAsync(Long userId, String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToUser(userId, title, body, data));
    }

    @Override
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> sendToUsersAsync(List<Long> userIds, String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToUsers(userIds, title, body, data));
    }

    @Override
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> sendToRoleAsync(UserRole role, String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToRole(role, title, body, data));
    }

    @Override
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> sendToAllAsync(String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToAll(title, body, data));
    }

    @Override
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> sendAsync(SendNotificationRequest request) {
        return CompletableFuture.completedFuture(send(request));
    }

    // ==================== UTILITY ====================

    @Override
    public boolean isEnabled() {
        return isFirebaseAvailable();
    }

    @Override
    public int cleanupStaleTokens(int daysOld) {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(daysOld);
        int deleted = fcmTokenRepository.deleteStaleTokens(cutoffDate);
        log.info("Cleaned up {} stale FCM tokens older than {} days", deleted, daysOld);
        return deleted;
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Core method that sends notifications to a list of FCM tokens.
     * Handles batching, error handling, and invalid token cleanup.
     */
    private NotificationResponse sendToTokens(
            List<FcmToken> tokens,
            String title,
            String body,
            String imageUrl,
            Map<String, String> data,
            String clickAction,
            Integer ttlSeconds
    ) {
        if (!isEnabled()) {
            log.warn("Push notifications are disabled. Firebase not configured.");
            return NotificationResponse.builder()
                    .totalAttempted(0)
                    .successCount(0)
                    .failureCount(0)
                    .message("Push notifications are disabled")
                    .build();
        }

        if (tokens == null || tokens.isEmpty()) {
            return NotificationResponse.noTargets();
        }

        List<String> tokenStrings = tokens.stream()
                .map(FcmToken::getToken)
                .collect(Collectors.toList());

        int totalAttempted = tokenStrings.size();
        int successCount = 0;
        int failureCount = 0;
        List<String> invalidTokens = new ArrayList<>();
        List<String> messageIds = new ArrayList<>();

        // Process in batches
        for (int i = 0; i < tokenStrings.size(); i += BATCH_SIZE) {
            List<String> batch = tokenStrings.subList(i, Math.min(i + BATCH_SIZE, tokenStrings.size()));

            try {
                MulticastMessage message = buildMulticastMessage(batch, title, body, imageUrl, data, clickAction, ttlSeconds);
                BatchResponse response = firebaseMessaging.get().sendEachForMulticast(message);

                successCount += response.getSuccessCount();
                failureCount += response.getFailureCount();

                // Process individual responses to identify invalid tokens
                List<SendResponse> responses = response.getResponses();
                for (int j = 0; j < responses.size(); j++) {
                    SendResponse sendResponse = responses.get(j);
                    if (sendResponse.isSuccessful()) {
                        messageIds.add(sendResponse.getMessageId());
                    } else {
                        FirebaseMessagingException exception = sendResponse.getException();
                        String failedToken = batch.get(j);

                        if (isTokenInvalid(exception)) {
                            invalidTokens.add(failedToken);
                            log.debug("Invalid FCM token detected, will be removed: {}",
                                    failedToken.substring(0, Math.min(20, failedToken.length())) + "...");
                        } else {
                            log.warn("Failed to send notification: {}",
                                    Optional.ofNullable(exception != null ? exception.getMessage() : "Unknown error"));
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("Batch send failed: {}", e.getMessage());
                failureCount += batch.size();
            }
        }

        // Clean up invalid tokens
        if (!invalidTokens.isEmpty()) {
            removeInvalidTokens(invalidTokens);
        }

        // Update last used timestamp for valid tokens
        List<String> validTokens = tokenStrings.stream()
                .filter(t -> !invalidTokens.contains(t))
                .collect(Collectors.toList());
        if (!validTokens.isEmpty()) {
            fcmTokenRepository.updateLastUsedAt(validTokens, ZonedDateTime.now());
        }

        if (failureCount == 0) {
            return NotificationResponse.success(successCount, messageIds);
        } else {
            return NotificationResponse.partial(totalAttempted, successCount, failureCount, invalidTokens, messageIds);
        }
    }

    /**
     * Build a multicast FCM message with the specified parameters.
     */
    private MulticastMessage buildMulticastMessage(
            List<String> tokens,
            String title,
            String body,
            String imageUrl,
            Map<String, String> data,
            String clickAction,
            Integer ttlSeconds
    ) {
        // Build notification part
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(title)
                .setBody(body);

        if (imageUrl != null && !imageUrl.isBlank()) {
            notificationBuilder.setImage(imageUrl);
        }

        // Build message
        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notificationBuilder.build());

        // Add custom data
        if (data != null && !data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        // Add click action URL to data (for frontend handling)
        if (clickAction != null && !clickAction.isBlank()) {
            messageBuilder.putData("click_action", clickAction);
        }

        // Configure Android-specific options
        AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH);

        if (ttlSeconds != null && ttlSeconds > 0) {
            androidConfigBuilder.setTtl(Duration.ofSeconds(ttlSeconds).toMillis());
        }

        messageBuilder.setAndroidConfig(androidConfigBuilder.build());

        // Configure web push options
        // NOTE: We intentionally omit WebpushFcmOptions.setLink() because:
        // 1. It requires an absolute URL (https://) and we use relative paths
        // 2. It conflicts with the service worker's notificationclick handler
        // Instead, the click_action is passed via data payload and handled by the service worker
        WebpushConfig.Builder webpushBuilder = WebpushConfig.builder()
                .setNotification(WebpushNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setIcon("/icons/icon-192x192.png")
                        .build());

        messageBuilder.setWebpushConfig(webpushBuilder.build());

        // Configure APNs (iOS) options
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setSound("default")
                        .build())
                .build();

        messageBuilder.setApnsConfig(apnsConfig);

        return messageBuilder.build();
    }

    /**
     * Check if the exception indicates an invalid/expired token that should be removed.
     */
    private boolean isTokenInvalid(FirebaseMessagingException exception) {
        if (exception == null) {
            return false;
        }

        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED ||
               errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    /**
     * Remove invalid tokens from the database.
     * Note: Runs within the class-level @Transactional context.
     * The repository's deleteByToken uses REQUIRES_NEW propagation where needed.
     */
    private void removeInvalidTokens(List<String> invalidTokens) {
        for (String token : invalidTokens) {
            fcmTokenRepository.deleteByToken(token);
        }
        log.info("Removed {} invalid FCM tokens", invalidTokens.size());
    }
}
