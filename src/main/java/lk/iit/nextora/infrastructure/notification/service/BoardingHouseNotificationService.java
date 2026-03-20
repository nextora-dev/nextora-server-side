package lk.iit.nextora.infrastructure.notification.service;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardingHouseNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;
    private final EntityManager entityManager;

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyNewListing(
            Long houseId, String title, String city, String district, BigDecimal price) {

        String notificationTitle = "New Boarding House Listed";
        String notificationBody = String.format(
                "%s\nLocation: %s, %s\nPrice: Rs. %,.0f/month",
                title, city, district, price != null ? price.doubleValue() : 0
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "BOARDING_HOUSE_NEW_LISTING");
        data.put("houseId", String.valueOf(houseId));
        data.put("clickAction", "/boarding-houses/" + houseId);

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.BOARDING_HOUSE, "/boarding-houses/" + houseId,
                "new boarding house listing", houseId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyListingRemovedByAdmin(
            Long houseId, Long ownerUserId, String title) {

        String notificationTitle = "Listing Removed by Admin";
        String notificationBody = String.format(
                "Your boarding house listing \"%s\" has been removed by an administrator.",
                title
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "BOARDING_HOUSE_ADMIN_REMOVED");
        data.put("houseId", String.valueOf(houseId));
        data.put("clickAction", "/boarding-houses/my-listings");

        return sendAndStoreForUser(
                ownerUserId, notificationTitle, notificationBody, data,
                NotificationType.BOARDING_HOUSE, "/boarding-houses/my-listings",
                "listing removed by admin", houseId
        );
    }

    // ==================== PRIVATE HELPERS ====================

    private CompletableFuture<NotificationResponse> sendAndStoreForUser(
            Long userId, String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel, Long houseId) {

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

        log.info("Sending {} push notification: houseId={}", logLabel, houseId);

        try {
            NotificationResponse response = pushNotificationService.sendToUser(
                    userId, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push: houseId={}, error={}",
                    logLabel, houseId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0).failureCount(0).totalAttempted(0)
                            .build()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<NotificationResponse> sendAndStoreForStudents(
            String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel, Long houseId) {

        try {
            List<Long> studentUserIds = entityManager.createQuery(
                    "SELECT u.id FROM BaseUser u WHERE u.role = :role AND u.isActive = true AND u.isDeleted = false"
            ).setParameter("role", UserRole.ROLE_STUDENT).getResultList();

            if (!studentUserIds.isEmpty()) {
                notificationHistoryService.storeNotificationForUsers(
                        studentUserIds, title, body, type, clickAction, null, data
                );
            }
        } catch (Exception e) {
            log.warn("Failed to store {} notification history: {}", logLabel, e.getMessage());
        }

        if (!pushNotificationService.isEnabled()) {
            log.debug("Push disabled - skipping {} push (history stored)", logLabel);
            return CompletableFuture.completedFuture(NotificationResponse.disabled());
        }

        log.info("Sending {} push notification: houseId={}", logLabel, houseId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push: houseId={}, error={}",
                    logLabel, houseId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0).failureCount(0).totalAttempted(0)
                            .build()
            );
        }
    }
}
