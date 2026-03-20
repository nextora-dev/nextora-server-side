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
public class ElectionNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryService notificationHistoryService;
    private final EntityManager entityManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyNominationsOpened(
            Long electionId, String electionTitle, String clubName, LocalDateTime nominationEndDate) {

        String notificationTitle = "Nominations Open: " + electionTitle;
        String notificationBody = String.format(
                "Club: %s\nNominate yourself before %s!",
                clubName, nominationEndDate != null ? nominationEndDate.format(DATE_FORMATTER) : "the deadline"
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ELECTION_NOMINATIONS_OPENED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId);

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId,
                "nominations opened", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyVotingOpened(
            Long electionId, String electionTitle, String clubName, int candidateCount) {

        String notificationTitle = "Voting Now Open: " + electionTitle;
        String notificationBody = String.format(
                "Club: %s\n%d candidates are competing. Cast your vote now!",
                clubName, candidateCount
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ELECTION_VOTING_OPENED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId + "/vote");

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId + "/vote",
                "voting opened", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyResultsPublished(
            Long electionId, String electionTitle, String clubName, String winnerName) {

        String notificationTitle = "Election Results: " + electionTitle;
        String notificationBody = String.format(
                "Club: %s\nWinner: %s\nView full results now!",
                clubName, winnerName != null ? winnerName : "See results"
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ELECTION_RESULTS_PUBLISHED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId + "/results");

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId + "/results",
                "results published", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyElectionCancelled(
            Long electionId, String electionTitle, String clubName, String reason) {

        String notificationTitle = "Election Cancelled: " + electionTitle;
        String notificationBody = String.format(
                "Club: %s\nThe election has been cancelled.%s",
                clubName, reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ELECTION_CANCELLED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections");

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections",
                "election cancelled", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyCandidateApproved(
            Long electionId, Long candidateUserId, String electionTitle) {

        String notificationTitle = "Nomination Approved";
        String notificationBody = String.format(
                "Your nomination for \"%s\" has been approved! Good luck!",
                electionTitle
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "CANDIDATE_APPROVED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId);

        return sendAndStoreForUser(
                candidateUserId, notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId,
                "candidate approved", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyCandidateRejected(
            Long electionId, Long candidateUserId, String electionTitle, String reason) {

        String notificationTitle = "Nomination Rejected";
        String notificationBody = String.format(
                "Your nomination for \"%s\" has been rejected.%s",
                electionTitle, reason != null ? "\nReason: " + reason : ""
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "CANDIDATE_REJECTED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId);

        return sendAndStoreForUser(
                candidateUserId, notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId,
                "candidate rejected", electionId
        );
    }

    @Async("pushNotificationExecutor")
    public CompletableFuture<NotificationResponse> notifyVotingClosed(
            Long electionId, String electionTitle, String clubName) {

        String notificationTitle = "Voting Closed: " + electionTitle;
        String notificationBody = String.format(
                "Club: %s\nVoting has ended. Results will be published soon.",
                clubName
        );

        Map<String, String> data = new HashMap<>();
        data.put("type", "ELECTION_VOTING_CLOSED");
        data.put("electionId", String.valueOf(electionId));
        data.put("clickAction", "/elections/" + electionId);

        return sendAndStoreForStudents(
                notificationTitle, notificationBody, data,
                NotificationType.ELECTION, "/elections/" + electionId,
                "voting closed", electionId
        );
    }

    // ==================== PRIVATE HELPERS ====================

    private CompletableFuture<NotificationResponse> sendAndStoreForUser(
            Long userId, String title, String body, Map<String, String> data,
            NotificationType type, String clickAction, String logLabel, Long electionId) {

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

        log.info("Sending {} push notification: electionId={}", logLabel, electionId);

        try {
            NotificationResponse response = pushNotificationService.sendToUser(
                    userId, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push: electionId={}, error={}",
                    logLabel, electionId, e.getMessage());
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
            NotificationType type, String clickAction, String logLabel, Long electionId) {

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

        log.info("Sending {} push notification: electionId={}", logLabel, electionId);

        try {
            NotificationResponse response = pushNotificationService.sendToRole(
                    UserRole.ROLE_STUDENT, title, body, data
            );
            log.info("{} notification sent: success={}, failed={}",
                    logLabel, response.getSuccessCount(), response.getFailureCount());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Failed to send {} push: electionId={}, error={}",
                    logLabel, electionId, e.getMessage());
            return CompletableFuture.completedFuture(
                    NotificationResponse.builder()
                            .message("Failed to send notification: " + e.getMessage())
                            .successCount(0).failureCount(0).totalAttempted(0)
                            .build()
            );
        }
    }
}
