package lk.iit.nextora.module.kuppi.scheduler;

import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.infrastructure.notification.service.KuppiNotificationService;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for automatic Kuppi session status transitions.
 *
 * Status Transition Logic:
 * - DRAFT → SCHEDULED: Manual (when host publishes the session)
 * - SCHEDULED → LIVE: Automatic (when current time reaches scheduledStartTime)
 * - LIVE → COMPLETED: Automatic (when current time passes scheduledEndTime)
 * - SCHEDULED → COMPLETED: Automatic (if session was missed - end time passed while still SCHEDULED)
 *
 * The scheduler runs every minute to check and update session statuses.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class KuppiSessionStatusScheduler {

    private final KuppiSessionRepository sessionRepository;
    private final KuppiNotificationService kuppiNotificationService;

    /**
     * Main scheduler method that runs every minute to update session statuses.
     * Uses cron expression: "0 * * * * *" (every minute at second 0)
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateSessionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Running Kuppi session status update job at {}", now);

        try {
            // 0. Send reminders for sessions starting soon (30 min window)
            sendSessionReminders(now);

            // 1. Transition SCHEDULED → LIVE
            transitionScheduledToLive(now);

            // 2. Transition LIVE → COMPLETED
            transitionLiveToCompleted(now);

            // 3. Handle missed sessions (SCHEDULED → COMPLETED if end time passed)
            transitionMissedToCompleted(now);

        } catch (Exception e) {
            log.error("Error during Kuppi session status update job", e);
        }
    }

    /**
     * Send reminder notifications for sessions starting within the next 30 minutes.
     * Only sends once per session by checking that start time is between 29-30 minutes from now
     * (1-minute window matching the scheduler frequency).
     */
    private void sendSessionReminders(LocalDateTime now) {
        // Look for sessions starting between 29 and 30 minutes from now
        // This 1-minute window ensures we only send the reminder once
        LocalDateTime reminderFrom = now.plusMinutes(29);
        LocalDateTime reminderTo = now.plusMinutes(30);

        List<KuppiSession> sessionsToRemind = sessionRepository.findSessionsStartingBetween(
                KuppiSessionStatus.SCHEDULED, reminderFrom, reminderTo);

        if (!sessionsToRemind.isEmpty()) {
            log.info("Sending reminders for {} sessions starting in ~30 minutes", sessionsToRemind.size());

            for (KuppiSession session : sessionsToRemind) {
                kuppiNotificationService.notifyKuppiSessionReminder(
                        session.getId(),
                        session.getTitle(),
                        session.getSubject(),
                        session.getLiveLink(),
                        30
                );
                log.info("Sent reminder for session {} '{}'", session.getId(), session.getTitle());
            }
        }
    }

    /**
     * Transition sessions from SCHEDULED to LIVE when the scheduled start time is reached.
     */
    private void transitionScheduledToLive(LocalDateTime now) {
        List<KuppiSession> sessionsToGoLive = sessionRepository.findSessionsToGoLive(
                KuppiSessionStatus.SCHEDULED, now);

        if (!sessionsToGoLive.isEmpty()) {
            log.info("Transitioning {} sessions from SCHEDULED to LIVE", sessionsToGoLive.size());

            for (KuppiSession session : sessionsToGoLive) {
                session.setStatus(KuppiSessionStatus.LIVE);
                sessionRepository.save(session);

                log.info("Session {} '{}' is now LIVE", session.getId(), session.getTitle());

                // Send notification that session is now live
                kuppiNotificationService.notifyKuppiSessionLive(
                        session.getId(),
                        session.getTitle(),
                        session.getSubject(),
                        session.getLiveLink()
                );
            }
        }
    }

    /**
     * Transition sessions from LIVE to COMPLETED when the scheduled end time is passed.
     */
    private void transitionLiveToCompleted(LocalDateTime now) {
        List<KuppiSession> sessionsToComplete = sessionRepository.findSessionsToComplete(
                KuppiSessionStatus.LIVE, now);

        if (!sessionsToComplete.isEmpty()) {
            log.info("Transitioning {} sessions from LIVE to COMPLETED", sessionsToComplete.size());

            for (KuppiSession session : sessionsToComplete) {
                session.setStatus(KuppiSessionStatus.COMPLETED);
                sessionRepository.save(session);

                log.info("Session {} '{}' is now COMPLETED", session.getId(), session.getTitle());

                // Send notification that session has completed
                kuppiNotificationService.notifyKuppiSessionCompleted(
                        session.getId(),
                        session.getTitle(),
                        session.getSubject()
                );
            }
        }
    }

    /**
     * Handle missed sessions - sessions that remained SCHEDULED but their end time has passed.
     * These sessions are marked as COMPLETED since the time window has passed.
     */
    private void transitionMissedToCompleted(LocalDateTime now) {
        List<KuppiSession> missedSessions = sessionRepository.findMissedScheduledSessions(
                KuppiSessionStatus.SCHEDULED, now);

        if (!missedSessions.isEmpty()) {
            log.warn("Found {} missed sessions (SCHEDULED but end time passed)", missedSessions.size());

            for (KuppiSession session : missedSessions) {
                session.setStatus(KuppiSessionStatus.COMPLETED);
                sessionRepository.save(session);

                log.warn("Missed session {} '{}' marked as COMPLETED", session.getId(), session.getTitle());
            }
        }
    }

    /**
     * Manual trigger method for testing or administrative purposes.
     * Can be called via an admin endpoint if needed.
     */
    @Transactional
    public void triggerStatusUpdate() {
        log.info("Manual Kuppi session status update triggered");
        updateSessionStatuses();
    }
}

