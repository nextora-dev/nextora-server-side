package lk.iit.nextora.infrastructure.notification.push.scheduler;

import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for FCM token maintenance.
 *
 * Purpose:
 * - Clean up stale/unused tokens to prevent database bloat
 * - Remove tokens that haven't been used in a configurable period
 *
 * Design:
 * - Runs daily by default (configurable via properties)
 * - Configurable token age threshold
 * - Only runs if push notifications are enabled
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FcmTokenCleanupScheduler {

    private final PushNotificationService pushNotificationService;

    /**
     * Number of days after which an unused token is considered stale.
     * Default: 30 days
     */
    @Value("${firebase.token-cleanup.days-old:30}")
    private int staleTokenDaysThreshold;

    /**
     * Whether token cleanup is enabled.
     * Default: true (only actually cleans if Firebase is enabled)
     */
    @Value("${firebase.token-cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Cleanup stale FCM tokens daily at 3 AM.
     *
     * Cron: second minute hour day-of-month month day-of-week
     * "0 0 3 * * *" = Every day at 3:00 AM
     */
    @Scheduled(cron = "${firebase.token-cleanup.cron:0 0 3 * * *}")
    public void cleanupStaleTokens() {
        if (!cleanupEnabled) {
            log.debug("FCM token cleanup is disabled");
            return;
        }

        if (!pushNotificationService.isEnabled()) {
            log.debug("Push notifications are disabled, skipping token cleanup");
            return;
        }

        log.info("Starting FCM token cleanup - removing tokens unused for {} days", staleTokenDaysThreshold);

        try {
            int deletedCount = pushNotificationService.cleanupStaleTokens(staleTokenDaysThreshold);
            log.info("FCM token cleanup completed - removed {} stale tokens", deletedCount);
        } catch (Exception e) {
            log.error("FCM token cleanup failed: {}", e.getMessage(), e);
        }
    }
}
