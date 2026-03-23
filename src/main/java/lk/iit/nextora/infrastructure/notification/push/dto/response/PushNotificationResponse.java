package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for push notification send results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResponse {

    /**
     * Total number of tokens targeted.
     */
    private int totalTokens;

    /**
     * Number of successful deliveries.
     */
    private int successCount;

    /**
     * Number of failed deliveries.
     */
    private int failureCount;

    /**
     * List of failed token IDs (for debugging).
     */
    private List<String> failedTokens;

    /**
     * Firebase message IDs for successful sends.
     */
    private List<String> messageIds;
}
