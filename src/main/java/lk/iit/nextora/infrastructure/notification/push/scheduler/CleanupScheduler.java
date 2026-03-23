package lk.iit.nextora.infrastructure.notification.push.scheduler;

import lk.iit.nextora.infrastructure.notification.push.service.FcmTokenService;
import lk.iit.nextora.infrastructure.notification.push.service.NotificationHistoryService;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled Cleanup Tasks
 *
 * Production-ready cleanup tasks that run periodically to:
 * 1. Clean up inactive FCM tokens
 * 2. Clean up old notification history
 * 3. Log statistics for monitoring
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class CleanupScheduler {

    private final FcmTokenService fcmTokenService;
    private final NotificationHistoryService notificationHistoryService;
    private final AuthenticationService authenticationService;

    /**
     * Clean up inactive FCM tokens.
     * Runs every day at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    public void cleanupInactiveFcmTokens() {
        log.info("Starting FCM token cleanup job");

        try {
            int deleted = fcmTokenService.cleanupInactiveTokens(30); // 30 days
            log.info("FCM token cleanup completed: {} tokens deleted", deleted);
        } catch (Exception e) {
            log.error("FCM token cleanup failed", e);
        }
    }

    /**
     * Clean up old notification history.
     * Runs every Sunday at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "UTC")
    public void cleanupOldNotifications() {
        log.info("Starting notification history cleanup job");

        try {
            int deleted = notificationHistoryService.deleteOldNotifications(90); // 90 days
            log.info("Notification history cleanup completed: {} notifications deleted", deleted);
        } catch (Exception e) {
            log.error("Notification history cleanup failed", e);
        }
    }

    /**
     * Clean up expired password reset tokens.
     * Runs every day at 2:30 AM.
     */
    @Scheduled(cron = "0 30 2 * * ?", zone = "UTC")
    public void cleanupExpiredPasswordResetTokens() {
        log.info("Starting password reset token cleanup job");

        try {
            authenticationService.cleanupExpiredTokens();
            log.info("Password reset token cleanup completed");
        } catch (Exception e) {
            log.error("Password reset token cleanup failed", e);
        }
    }

    /**
     * Log system statistics.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void logSystemStats() {
        try {
            // This can be enhanced to send metrics to monitoring systems
            log.debug("System statistics logged");
        } catch (Exception e) {
            log.warn("Failed to log system statistics", e);
        }
    }
}
