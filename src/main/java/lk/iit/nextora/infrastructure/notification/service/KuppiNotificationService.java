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

/**
 * Service responsible for sending Kuppi-related push notifications
 * AND storing notification history for students.
 *
 * This service acts as a facade between the Kuppi module and the Push Notification infrastructure.
 * It encapsulates all notification logic for Kuppi sessions, providing:
 * - Clean separation of concerns (Kuppi service doesn't need to know notification details)
 * - Async notifications to not block the main request
 * - Consistent notification formatting
 * - Notification history storage so students can view past notifications
 * - Graceful degradation if push notifications are disabled
 *
 * Design Decisions:
 * - Uses @Async for non-blocking notifications
 * - All methods return CompletableFuture for tracking if needed
 * - Notifications are sent to ROLE_STUDENT (all students) for new sessions
 * - Notification history is ALWAYS stored (even if push is disabled) so students have a record
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KuppiNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;
    private final EntityManager entityManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // ==================== PUBLIC NOTIFICATION METHODS ====================

    /**
     * Send notification to all students when a new Kuppi session is created.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyNewKuppiSession(
            Long sessionId,
            String title,
            String subject,
            String hostName,
            LocalDateTime scheduledTime
    ) {
        String notificationTitle = "\uD83D\uDCDA New Kuppi Session Available!";
        String notificationBody = String.format(
                "%s - %s\nBy: %s\n\uD83D\uDCC5 %s at %s",
                title, subject, hostName,
                scheduledTime.format(DATE_FORMATTER),
                scheduledTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_CREATED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_SESSION,
                "/kuppi/sessions/" + sessionId,
                "new Kuppi session", sessionId
        );
    }

    /**
     * Send notification when a Kuppi session is cancelled.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionCancelled(
            Long sessionId,
            String title,
            String subject,
            String reason
    ) {
        String notificationTitle = "❌ Kuppi Session Cancelled";
        String notificationBody = String.format(
                "%s - %s has been cancelled.%s",
                title, subject,
                reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_CANCELLED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions");

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_SESSION,
                "/kuppi/sessions",
                "Kuppi session cancellation", sessionId
        );
    }

    /**
     * Send notification when a Kuppi session is rescheduled.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionRescheduled(
            Long sessionId,
            String title,
            String subject,
            LocalDateTime newScheduledTime
    ) {
        String notificationTitle = "\uD83D\uDD04 Kuppi Session Rescheduled";
        String notificationBody = String.format(
                "%s - %s\nNew time: %s at %s",
                title, subject,
                newScheduledTime.format(DATE_FORMATTER),
                newScheduledTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_RESCHEDULED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_SESSION,
                "/kuppi/sessions/" + sessionId,
                "Kuppi session reschedule", sessionId
        );
    }

    /**
     * Send reminder notification before a Kuppi session starts.
     * Called by scheduler, typically 30 minutes before session.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionReminder(
            Long sessionId,
            String title,
            String subject,
            String liveLink,
            int minutesBefore
    ) {
        String notificationTitle = String.format("⏰ Kuppi Session Starting in %d minutes!", minutesBefore);
        String notificationBody = String.format(
                "%s - %s\nClick to join now!",
                title, subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_REMINDER");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("liveLink", liveLink);
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_REMINDER,
                "/kuppi/sessions/" + sessionId,
                "Kuppi session reminder", sessionId
        );
    }

    /**
     * Send notification when a Kuppi session goes LIVE.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionLive(
            Long sessionId,
            String title,
            String subject,
            String liveLink
    ) {
        String notificationTitle = "\uD83D\uDD34 Kuppi Session is LIVE Now!";
        String notificationBody = String.format(
                "%s - %s\nJoin now and start learning!",
                title, subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_LIVE");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("liveLink", liveLink);
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_SESSION,
                "/kuppi/sessions/" + sessionId,
                "Kuppi session live", sessionId
        );
    }

    /**
     * Send notification when a Kuppi session is completed.
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionCompleted(
            Long sessionId,
            String title,
            String subject
    ) {
        String notificationTitle = "✅ Kuppi Session Completed";
        String notificationBody = String.format(
                "%s - %s has ended.\nThank you for participating!",
                title, subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_COMPLETED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        return sendAndStoreNotification(
                notificationTitle, notificationBody, data,
                NotificationType.KUPPI_SESSION,
                "/kuppi/sessions/" + sessionId,
                "Kuppi session completion", sessionId
        );
    }

    // ==================== PRIVATE HELPER ====================

    /**
     * Core helper that:
     * 1. Sends push notification to all ROLE_STUDENT tokens via FCM
     * 2. Stores notification history for each student so they can view it later
     * 3. Handles errors gracefully without crashing
     */
    private CompletableFuture<NotificationResponse> sendAndStoreNotification(
            String notificationTitle,
            String notificationBody,
            Map<String, String> data,
            NotificationType type,
            String clickAction,
            String logLabel,
            Long sessionId
    ) {
        // Always store notification history for students (even if push is disabled)
        storeNotificationHistoryForStudents(notificationTitle, notificationBody, type, clickAction, data);

        // Send push notification if enabled
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping {} push notification (history still stored)", logLabel);
            return CompletableFuture.completedFuture(NotificationResponse.disabled());
        }

        log.info("Sending {} push notification: sessionId={}", logLabel, sessionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push notification: sessionId={}, error={}",
                    logLabel, sessionId, e.getMessage());
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

    /**
     * Store notification history for ALL active students (not just those with FCM tokens).
     * This ensures every student can view past notifications even if they haven't
     * registered for push notifications yet.
     */
    private void storeNotificationHistoryForStudents(
            String title,
            String body,
            NotificationType type,
            String clickAction,
            Map<String, String> data
    ) {
        try {
            // Query ALL active students from the users table, not just those with FCM tokens
            @SuppressWarnings("unchecked")
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

            log.debug("Stored notification history for {} students", studentUserIds.size());
        } catch (Exception e) {
            // Don't let history storage failure break the push notification flow
            log.warn("Failed to store notification history: {}", e.getMessage());
        }
    }
}
