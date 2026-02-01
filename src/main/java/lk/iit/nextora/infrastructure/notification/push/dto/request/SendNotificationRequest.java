package lk.iit.nextora.infrastructure.notification.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending push notifications.
 * Supports various targeting options: single user, multiple users, role-based, or broadcast.
 *
 * Design: Only one targeting option should be used at a time.
 * Priority: userIds > role > topic (if multiple are provided)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotBlank(message = "Notification title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Notification body is required")
    @Size(max = 1000, message = "Body must not exceed 1000 characters")
    private String body;

    /**
     * Optional image URL to display in the notification.
     */
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    /**
     * Target specific users by their IDs.
     */
    private List<Long> userIds;

    /**
     * Target all users with a specific role.
     */
    private UserRole targetRole;

    /**
     * FCM topic for topic-based messaging (optional, for future use).
     */
    @Size(max = 100, message = "Topic must not exceed 100 characters")
    private String topic;

    /**
     * Optional click action URL - where to navigate when notification is clicked.
     */
    @Size(max = 500, message = "Click action URL must not exceed 500 characters")
    private String clickAction;

    /**
     * Additional custom data to include in the notification payload.
     * Frontend can use this for routing or custom handling.
     */
    private Map<String, String> data;

    /**
     * Priority: "high" or "normal". Defaults to "high".
     */
    private String priority;

    /**
     * Time-to-live in seconds. How long FCM should attempt delivery.
     */
    private Integer ttlSeconds;
}
