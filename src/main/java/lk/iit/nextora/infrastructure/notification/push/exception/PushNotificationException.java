package lk.iit.nextora.infrastructure.notification.push.exception;

/**
 * Exception thrown when push notification operations fail.
 *
 * Use cases:
 * - Firebase not configured/initialized
 * - Failed to send notification
 * - Invalid token format
 */
public class PushNotificationException extends RuntimeException {

    public PushNotificationException(String message) {
        super(message);
    }

    public PushNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for disabled notifications.
     */
    public static PushNotificationException disabled() {
        return new PushNotificationException("Push notifications are disabled. Firebase is not configured.");
    }

    /**
     * Factory method for send failures.
     */
    public static PushNotificationException sendFailed(String reason) {
        return new PushNotificationException("Failed to send push notification: " + reason);
    }

    /**
     * Factory method for send failures with cause.
     */
    public static PushNotificationException sendFailed(String reason, Throwable cause) {
        return new PushNotificationException("Failed to send push notification: " + reason, cause);
    }
}
