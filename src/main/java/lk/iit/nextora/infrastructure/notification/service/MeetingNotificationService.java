package lk.iit.nextora.infrastructure.notification.service;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.infrastructure.notification.push.dto.response.NotificationResponse;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
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
public class MeetingNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyNewMeetingRequest(
            Long meetingId, Long lecturerUserId, String studentName, String subject, String priority) {

        String notificationTitle = "New Meeting Request";
        String notificationBody = String.format(
                "From: %s\nSubject: %s\nPriority: %s",
                studentName, subject, priority
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_REQUEST_CREATED");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings/" + meetingId);

        return sendAndStoreForUser(
                lecturerUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings/" + meetingId,
                "new meeting request", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingAccepted(
            Long meetingId, Long studentUserId, String lecturerName,
            LocalDateTime scheduledStart, String location, String meetingLink, Boolean isOnline) {

        String notificationTitle = "Meeting Request Accepted";
        String locationInfo = Boolean.TRUE.equals(isOnline) ? "Online" : location;
        String notificationBody = String.format(
                "Your meeting with %s has been scheduled.\nDate: %s at %s\nLocation: %s",
                lecturerName,
                scheduledStart.format(DATE_FORMATTER),
                scheduledStart.format(TIME_FORMATTER),
                locationInfo
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_ACCEPTED");
        data.put("meetingId", String.valueOf(meetingId));
        if (meetingLink != null) data.put("meetingLink", meetingLink);
        data.put("clickAction", "/meetings/" + meetingId);

        return sendAndStoreForUser(
                studentUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings/" + meetingId,
                "meeting accepted", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingRejected(
            Long meetingId, Long studentUserId, String lecturerName, String reason) {

        String notificationTitle = "Meeting Request Declined";
        String notificationBody = String.format(
                "Your meeting request with %s has been declined.%s",
                lecturerName, reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_REJECTED");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings");

        return sendAndStoreForUser(
                studentUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings",
                "meeting rejected", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingRescheduled(
            Long meetingId, Long studentUserId, String lecturerName, LocalDateTime newTime) {

        String notificationTitle = "Meeting Rescheduled";
        String notificationBody = String.format(
                "Your meeting with %s has been rescheduled.\nNew time: %s at %s",
                lecturerName,
                newTime.format(DATE_FORMATTER),
                newTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_RESCHEDULED");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings/" + meetingId);

        return sendAndStoreForUser(
                studentUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings/" + meetingId,
                "meeting rescheduled", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingCancelledByLecturer(
            Long meetingId, Long studentUserId, String lecturerName, String reason) {

        String notificationTitle = "Meeting Cancelled";
        String notificationBody = String.format(
                "Your meeting with %s has been cancelled.%s",
                lecturerName, reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_CANCELLED_BY_LECTURER");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings");

        return sendAndStoreForUser(
                studentUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings",
                "meeting cancelled by lecturer", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingCancelledByStudent(
            Long meetingId, Long lecturerUserId, String studentName, String reason) {

        String notificationTitle = "Meeting Cancelled by Student";
        String notificationBody = String.format(
                "%s has cancelled the meeting request.%s",
                studentName, reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_CANCELLED_BY_STUDENT");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings");

        return sendAndStoreForUser(
                lecturerUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings",
                "meeting cancelled by student", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingCompleted(
            Long meetingId, Long studentUserId, String lecturerName) {

        String notificationTitle = "Meeting Completed - Leave Feedback";
        String notificationBody = String.format(
                "Your meeting with %s is now complete.\nPlease leave your feedback and rating.",
                lecturerName
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_COMPLETED");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings/" + meetingId + "/feedback");

        return sendAndStoreForUser(
                studentUserId, notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings/" + meetingId + "/feedback",
                "meeting completed", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyAdminCancelledMeeting(
            Long meetingId, Long studentUserId, Long lecturerUserId, String reason) {

        String notificationTitle = "Meeting Cancelled by Admin";
        String notificationBody = String.format(
                "Your meeting has been cancelled by an administrator.%s",
                reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_CANCELLED_BY_ADMIN");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings");

        return sendAndStoreForUsers(
                List.of(studentUserId, lecturerUserId),
                notificationTitle, notificationBody, data,
                NotificationType.MEETING, "/meetings",
                "admin meeting cancellation", meetingId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyMeetingReminder(
            Long meetingId, Long studentUserId, Long lecturerUserId,
            String subject, LocalDateTime scheduledTime, int minutesBefore) {

        String notificationTitle = String.format("Meeting Reminder - %d %s",
                minutesBefore >= 60 ? minutesBefore / 60 : minutesBefore,
                minutesBefore >= 60 ? "hour(s)" : "minutes");
        String notificationBody = String.format(
                "Reminder: %s\nScheduled at %s",
                subject, scheduledTime.format(TIME_FORMATTER)
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "MEETING_REMINDER");
        data.put("meetingId", String.valueOf(meetingId));
        data.put("clickAction", "/meetings/" + meetingId);

        return sendAndStoreForUsers(
                List.of(studentUserId, lecturerUserId),
                notificationTitle, notificationBody, data,
                NotificationType.MEETING_REMINDER, "/meetings/" + meetingId,
                "meeting reminder", meetingId
        );
    }

    // ==================== PRIVATE HELPERS ====================

    private CompletableFuture<NotificationResponse> sendAndStoreForUser(
            Long userId, String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel, Long meetingId) {

        return sendAndStoreForUsers(List.of(userId), title, body, data, type, clickAction, logLabel, meetingId);
    }

    private CompletableFuture<NotificationResponse> sendAndStoreForUsers(
            List<Long> userIds, String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel, Long meetingId) {

        try {
            notificationHistoryService.storeNotificationForUsers(
                    userIds, title, body, type, clickAction, null, data
            );
        } catch (Exception e) {
            log.warn("Failed to store {} notification history: {}", logLabel, e.getMessage());
        }

        if (!pushNotificationService.isEnabled()) {
            log.debug("Push disabled - skipping {} push (history stored)", logLabel);
            return CompletableFuture.completedFuture(NotificationResponse.disabled());
        }

        log.info("Sending {} push notification: meetingId={}", logLabel, meetingId);

        try {
            NotificationResponse response = pushNotificationService.sendToUsers(
                    userIds, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push: meetingId={}, error={}",
                    logLabel, meetingId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0).failureCount(0).totalAttempted(0)
                            .build()
            );
        }
    }
}
