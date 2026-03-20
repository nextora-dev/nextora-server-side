package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.infrastructure.notification.push.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Response DTO for notification history entries.
 * Maps the Notification entity to a client-friendly format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryResponse {

    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private Boolean read;
    private String clickAction;
    private String imageUrl;
    private Map<String, String> data;
    private ZonedDateTime sentAt;
    private ZonedDateTime readAt;
    private ZonedDateTime createdAt;

    /**
     * Factory method to convert a Notification entity to a response DTO.
     */
    public static NotificationHistoryResponse from(Notification notification) {
        return NotificationHistoryResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .read(notification.getRead())
                .clickAction(notification.getClickAction())
                .imageUrl(notification.getImageUrl())
                .data(notification.getData())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

