package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for send notification operations.
 * Provides detailed feedback on delivery success/failure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /**
     * Total number of notifications attempted.
     */
    private int totalAttempted;

    /**
     * Number of successfully delivered notifications.
     */
    private int successCount;

    /**
     * Number of failed notifications.
     */
    private int failureCount;

    /**
     * List of tokens that failed and were removed (invalid/expired).
     */
    private List<String> invalidTokens;

    /**
     * Optional message IDs for successful sends (for tracking).
     */
    private List<String> messageIds;

    /**
     * Overall status message.
     */
    private String message;

    /**
     * Factory method for successful send to all targets.
     */
    public static NotificationResponse success(int count, List<String> messageIds) {
        return NotificationResponse.builder()
                .totalAttempted(count)
                .successCount(count)
                .failureCount(0)
                .messageIds(messageIds)
                .message("All notifications sent successfully")
                .build();
    }

    /**
     * Factory method for partial success.
     */
    public static NotificationResponse partial(int total, int success, int failure,
                                                List<String> invalidTokens, List<String> messageIds) {
        return NotificationResponse.builder()
                .totalAttempted(total)
                .successCount(success)
                .failureCount(failure)
                .invalidTokens(invalidTokens)
                .messageIds(messageIds)
                .message(String.format("Sent %d of %d notifications. %d failed.", success, total, failure))
                .build();
    }

    /**
     * Factory method for no targets found.
     */
    public static NotificationResponse noTargets() {
        return NotificationResponse.builder()
                .totalAttempted(0)
                .successCount(0)
                .failureCount(0)
                .message("No active tokens found for the specified targets")
                .build();
    }
}
