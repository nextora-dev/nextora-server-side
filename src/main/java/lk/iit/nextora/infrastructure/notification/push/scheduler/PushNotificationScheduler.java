package lk.iit.nextora.infrastructure.notification.push.scheduler;

import lk.iit.nextora.infrastructure.notification.push.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @deprecated Use {@link CleanupScheduler} instead.
 * Token cleanup is already handled by CleanupScheduler.cleanupInactiveFcmTokens()
 * which runs daily at 2:00 AM UTC. This class is kept for backward compatibility
 * but the scheduled method has been removed to prevent duplicate cleanup.
 */
@Deprecated
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationScheduler {

    private final FcmTokenService fcmTokenService;

    /**
     * Manual cleanup method - can be called from admin endpoints if needed.
     * The scheduled cleanup is now handled by CleanupScheduler.
     */
    public void manualCleanup() {
        log.info("Manual cleanup of inactive FCM tokens triggered");
        try {
            int deletedCount = fcmTokenService.cleanupInactiveTokens(30);
            log.info("Manual cleanup completed. Deleted {} inactive tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during manual token cleanup", e);
        }
    }
}
