package lk.iit.nextora.infrastructure.notification.service;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class LostAndFoundNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyUser(Long studentId, String message) {

        String notificationTitle = "Lost & Found Update";

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOST_AND_FOUND_UPDATE");
        data.put("clickAction", "/lost-and-found");

        return sendAndStoreForUser(
                studentId, notificationTitle, message, data,
                NotificationType.LOST_AND_FOUND, "/lost-and-found",
                "lost and found update"
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMatchFound(
            Long studentId, Long lostItemId, Long foundItemId) {

        String notificationTitle = "Potential Match Found!";
        String notificationBody = "A potential match has been found for your lost item. Check it out!";

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOST_AND_FOUND_MATCH");
        data.put("lostItemId", String.valueOf(lostItemId));
        data.put("foundItemId", String.valueOf(foundItemId));
        data.put("clickAction", "/lost-and-found/items/" + lostItemId);

        return sendAndStoreForUser(
                studentId, notificationTitle, notificationBody, data,
                NotificationType.LOST_AND_FOUND, "/lost-and-found/items/" + lostItemId,
                "match found"
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyClaimApproved(Long studentId, Long claimId) {

        String notificationTitle = "Claim Approved!";
        String notificationBody = "Your claim has been approved. Please collect your item.";

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOST_AND_FOUND_CLAIM_APPROVED");
        data.put("claimId", String.valueOf(claimId));
        data.put("clickAction", "/lost-and-found/claims/" + claimId);

        return sendAndStoreForUser(
                studentId, notificationTitle, notificationBody, data,
                NotificationType.LOST_AND_FOUND, "/lost-and-found/claims/" + claimId,
                "claim approved"
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyClaimRejected(
            Long studentId, Long claimId, String reason) {

        String notificationTitle = "Claim Rejected";
        String notificationBody = String.format(
                "Your claim has been rejected.%s",
                reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "LOST_AND_FOUND_CLAIM_REJECTED");
        data.put("claimId", String.valueOf(claimId));
        data.put("clickAction", "/lost-and-found/claims/" + claimId);

        return sendAndStoreForUser(
                studentId, notificationTitle, notificationBody, data,
                NotificationType.LOST_AND_FOUND, "/lost-and-found/claims/" + claimId,
                "claim rejected"
        );
    }

    // ==================== PRIVATE HELPER ====================

    private CompletableFuture<NotificationResponse> sendAndStoreForUser(
            Long userId, String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel) {

        try {
            notificationHistoryService.storeNotificationForUsers(
                    List.of(userId), title, body, type, clickAction, null, data
            );
        } catch (Exception e) {
            log.warn("Failed to store {} notification history: {}", logLabel, e.getMessage());
        }

        if (!pushNotificationService.isEnabled()) {
            log.debug("Push disabled - skipping {} push (history stored)", logLabel);
            return CompletableFuture.completedFuture(NotificationResponse.disabled());
        }

        log.info("Sending {} push notification to user {}", logLabel, userId);

        try {
            NotificationResponse response = pushNotificationService.sendToUser(
                    userId, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push to user {}: {}",
                    logLabel, userId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0).failureCount(0).totalAttempted(0)
                            .build()
            );
        }
    }
}
