package lk.iit.nextora.infrastructure.notification.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title exceeds maximum length")
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 500, message = "Body exceeds maximum length")
    private String body;

    @Size(max = 500, message = "Image URL exceeds maximum length")
    private String imageUrl;

    @Size(max = 500, message = "Click action URL exceeds maximum length")
    private String clickAction;

    private NotificationType type;

    private List<Long> userIds;

    private UserRole targetRole;

    private Map<String, String> data;

    @Builder.Default
    private Integer ttlSeconds = 86400;
}
