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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;
    private final EntityManager entityManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyEventPublished(
            Long eventId, String title, String eventType, String creatorName,
            String location, LocalDateTime startAt) {

        String notificationTitle = "New Event: " + title;
        String notificationBody = String.format(
                "%s by %s\nLocation: %s\nDate: %s at %s",
                eventType, creatorName, location,
                startAt.format(DATE_FORMATTER), startAt.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_PUBLISHED");
        data.put("eventId", String.valueOf(eventId));
        data.put("clickAction", "/events/" + eventId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.EVENT, "/events/" + eventId,
                "event published", eventId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyEventCancelled(
            Long eventId, String title, String reason) {

        String notificationTitle = "Event Cancelled: " + title;
        String notificationBody = reason != null
                ? "Reason: " + reason
                : "This event has been cancelled.";

        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_CANCELLED");
        data.put("eventId", String.valueOf(eventId));
        data.put("clickAction", "/events");

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.EVENT, "/events",
                "event cancellation", eventId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyEventRescheduled(
            Long eventId, String title, LocalDateTime newStartTime) {

        String notificationTitle = "Event Rescheduled: " + title;
        String notificationBody = String.format(
                "New time: %s at %s",
                newStartTime.format(DATE_FORMATTER), newStartTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_RESCHEDULED");
        data.put("eventId", String.valueOf(eventId));
        data.put("clickAction", "/events/" + eventId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.EVENT, "/events/" + eventId,
                "event reschedule", eventId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyRegistrationConfirmed(
            Long eventId, String title, LocalDateTime startAt, String location, Long userId) {

        String notificationTitle = "Registration Confirmed: " + title;
        String notificationBody = String.format(
                "You're registered!\nDate: %s at %s\nLocation: %s",
                startAt.format(DATE_FORMATTER), startAt.format(TIME_FORMATTER), location
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_REGISTRATION_CONFIRMED");
        data.put("eventId", String.valueOf(eventId));
        data.put("userId", String.valueOf(userId));
        data.put("clickAction", "/events/" + eventId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.EVENT, "/events/" + eventId,
                "registration confirmation", eventId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyRegistrationCancelled(
            Long eventId, String title, Long userId) {

        String notificationTitle = "Registration Cancelled: " + title;
        String notificationBody = "Your registration has been cancelled.";

        Map<String, String> data = new HashMap<>();
        data.put("type", "EVENT_REGISTRATION_CANCELLED");
        data.put("eventId", String.valueOf(eventId));
        data.put("userId", String.valueOf(userId));
        data.put("clickAction", "/events");

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.EVENT, "/events",
                "registration cancellation", eventId
        );
    }

    // ==================== PRIVATE HELPER ====================

    private CompletableFuture<NotificationResponse> sendAndStoreNotification(
            String notificationTitle, String notificationBody,
            Map<String, String> data, NotificationType type,
            String clickAction, String logLabel, Long eventId) {

        storeNotificationHistoryForStudents(notificationTitle, notificationBody, type, clickAction, data);

        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping {} push notification (history still stored)", logLabel);
            return CompletableFuture.completedFuture(NotificationResponse.disabled());
        }

        log.info("Sending {} push notification: eventId={}", logLabel, eventId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT, notificationTitle, notificationBody, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push notification: eventId={}, error={}",
                    logLabel, eventId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void storeNotificationHistoryForStudents(
            String title, String body, NotificationType type,
            String clickAction, Map<String, String> data) {
        try {
            List<Long> studentUserIds = entityManager.createQuery(
                    "SELECT u.id FROM BaseUser u WHERE u.role = :role AND u.isActive = true AND u.isDeleted = false"
            ).setParameter("role", UserRole.ROLE_STUDENT).getResultList();

            if (studentUserIds.isEmpty()) {
                log.debug("No active students found - skipping notification history storage");
                return;
            }

            notificationHistoryService.storeNotificationForUsers(
                    studentUserIds, title, body, type, clickAction, null, data
            );
            log.debug("Stored event notification history for {} students", studentUserIds.size());
        } catch (Exception e) {
            log.warn("Failed to store notification history: {}", e.getMessage());
        }
    }
}