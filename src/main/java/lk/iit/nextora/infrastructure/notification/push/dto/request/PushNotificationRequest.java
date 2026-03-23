package lk.iit.nextora.infrastructure.notification.push.dto.request;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending push notifications.
 *
 * Supports three targeting modes:
 * 1. Single user (userId)
 * 2. Multiple users (userIds)
 * 3. Role-based (targetRole)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    /**
     * Notification title (displayed in notification header).
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title exceeds maximum length")
    private String title;

    /**
     * Notification body (main content).
     */
    @NotBlank(message = "Body is required")
    @Size(max = 500, message = "Body exceeds maximum length")
    private String body;

    /**
     * Optional image URL for rich notifications.
     */
    @Size(max = 500, message = "Image URL exceeds maximum length")
    private String imageUrl;

    /**
     * Optional click action URL.
     */
    @Size(max = 500, message = "Click action URL exceeds maximum length")
    private String clickAction;

    /**
     * Notification type for categorization.
     */
    private NotificationType type;

    /**
     * Target single user by ID.
     */
    private Long userId;

    /**
     * Target multiple users by IDs.
     */
    private List<Long> userIds;

    /**
     * Target all users with this role.
     */
    private UserRole targetRole;

    /**
     * Additional custom data payload.
     * This data is passed to the client app for custom handling.
     */
    private Map<String, String> data;

    /**
     * Whether to send as high priority notification.
     */
    @Builder.Default
    private boolean highPriority = false;

    /**
     * Time-to-live in seconds (how long FCM should retry).
     */
    @Builder.Default
    private int ttlSeconds = 86400; // 24 hours default
}
