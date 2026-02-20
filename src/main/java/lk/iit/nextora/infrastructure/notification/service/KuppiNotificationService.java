package lk.iit.nextora.infrastructure.notification.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for sending Kuppi-related push notifications.
 *
 * This service acts as a facade between the Kuppi module and the Push Notification infrastructure.
 * It encapsulates all notification logic for Kuppi sessions, providing:
 * - Clean separation of concerns (Kuppi service doesn't need to know notification details)
 * - Async notifications to not block the main request
 * - Consistent notification formatting
 * - Easy to extend for new notification types
 *
 * Design Decisions:
 * - Uses @Async for non-blocking notifications
 * - All methods return CompletableFuture for tracking if needed
 * - Notifications are sent to ROLE_STUDENT (all students) for new sessions
 * - Graceful degradation if push notifications are disabled
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KuppiNotificationService {

    private final PushNotificationService pushNotificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Send notification to all students when a new Kuppi session is created.
     *
     * @param sessionId The ID of the created session
     * @param title The session title
     * @param subject The session subject
     * @param hostName The name of the host (Kuppi student)
     * @param scheduledTime The scheduled start time
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyNewKuppiSession(
            Long sessionId,
            String title,
            String subject,
            String hostName,
            LocalDateTime scheduledTime
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping Kuppi session notification");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = "📚 New Kuppi Session Available!";
        String notificationBody = String.format(
                "%s - %s\nBy: %s\n📅 %s at %s",
                title,
                subject,
                hostName,
                scheduledTime.format(DATE_FORMATTER),
                scheduledTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_CREATED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        log.info("Sending push notification for new Kuppi session: id={}, title={}", sessionId, title);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi session notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi session notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
     * Send notification when a Kuppi session is cancelled.
     * Notifies all students who might have been interested.
     *
     * @param sessionId The ID of the cancelled session
     * @param title The session title
     * @param subject The session subject
     * @param reason The cancellation reason
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionCancelled(
            Long sessionId,
            String title,
            String subject,
            String reason
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping cancellation notification");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = "❌ Kuppi Session Cancelled";
        String notificationBody = String.format(
                "%s - %s has been cancelled.%s",
                title,
                subject,
                reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_CANCELLED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions");

        log.info("Sending cancellation notification for Kuppi session: id={}", sessionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi cancellation notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi cancellation notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
     * Send notification when a Kuppi session is rescheduled.
     *
     * @param sessionId The ID of the rescheduled session
     * @param title The session title
     * @param subject The session subject
     * @param newScheduledTime The new scheduled time
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionRescheduled(
            Long sessionId,
            String title,
            String subject,
            LocalDateTime newScheduledTime
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping reschedule notification");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = "🔄 Kuppi Session Rescheduled";
        String notificationBody = String.format(
                "%s - %s\nNew time: %s at %s",
                title,
                subject,
                newScheduledTime.format(DATE_FORMATTER),
                newScheduledTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_RESCHEDULED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        log.info("Sending reschedule notification for Kuppi session: id={}", sessionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi reschedule notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi reschedule notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
     * Send reminder notification before a Kuppi session starts.
     * Called by scheduler, typically 30 minutes before session.
     *
     * @param sessionId The ID of the session
     * @param title The session title
     * @param subject The session subject
     * @param liveLink The link to join the session
     * @param minutesBefore Minutes until session starts
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionReminder(
            Long sessionId,
            String title,
            String subject,
            String liveLink,
            int minutesBefore
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping session reminder");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = String.format("⏰ Kuppi Session Starting in %d minutes!", minutesBefore);
        String notificationBody = String.format(
                "%s - %s\nClick to join now!",
                title,
                subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_REMINDER");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("liveLink", liveLink);
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        log.info("Sending reminder notification for Kuppi session: id={}, minutesBefore={}",
                sessionId, minutesBefore);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi reminder notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi reminder notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
     * Send notification when a Kuppi session goes LIVE.
     * Notifies all students that the session is now active.
     *
     * @param sessionId The ID of the live session
     * @param title The session title
     * @param subject The session subject
     * @param liveLink The link to join the session
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionLive(
            Long sessionId,
            String title,
            String subject,
            String liveLink
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping session live notification");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = "🔴 Kuppi Session is LIVE Now!";
        String notificationBody = String.format(
                "%s - %s\nJoin now and start learning!",
                title,
                subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_LIVE");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("liveLink", liveLink);
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        log.info("Sending live notification for Kuppi session: id={}", sessionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi session live notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi session live notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
     * Send notification when a Kuppi session is completed.
     * Notifies students that the session has ended.
     *
     * @param sessionId The ID of the completed session
     * @param title The session title
     * @param subject The session subject
     * @return CompletableFuture with notification response
     */
    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyKuppiSessionCompleted(
            Long sessionId,
            String title,
            String subject
    ) {
        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications disabled - skipping session completed notification");
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Push notifications are disabled")
                            .successCount(0)
                            .failureCount(0)
                            .totalAttempted(0)
                            .build()
            );
        }

        String notificationTitle = "✅ Kuppi Session Completed";
        String notificationBody = String.format(
                "%s - %s has ended.\nThank you for participating!",
                title,
                subject
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "KUPPI_SESSION_COMPLETED");
        data.put("sessionId", String.valueOf(sessionId));
        data.put("clickAction", "/kuppi/sessions/" + sessionId);

        log.info("Sending completion notification for Kuppi session: id={}", sessionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT,
                    notificationTitle,
                    notificationBody,
                    data
            );

            log.info("Kuppi session completion notification sent: success={}, failed={}",
                    response.getSuccessCount(), response.getFailureCount());

            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send Kuppi session completion notification: sessionId={}, error={}",
                    sessionId, e.getMessage());
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
}
