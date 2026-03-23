package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private int totalAttempted;
    private int successCount;
    private int failureCount;
    private String message;
    private List<String> invalidTokens;
    private List<String> messageIds;

    public static NotificationResponse success(int successCount, List<String> messageIds) {
        return NotificationResponse.builder()
                .totalAttempted(successCount)
                .successCount(successCount)
                .failureCount(0)
                .message("All notifications sent successfully")
                .messageIds(messageIds)
                .invalidTokens(Collections.emptyList())
                .build();
    }

    public static NotificationResponse partial(int totalAttempted, int successCount, int failureCount,
                                               List<String> invalidTokens, List<String> messageIds) {
        return NotificationResponse.builder()
                .totalAttempted(totalAttempted)
                .successCount(successCount)
                .failureCount(failureCount)
                .message(String.format("Sent %d/%d notifications successfully", successCount, totalAttempted))
                .invalidTokens(invalidTokens)
                .messageIds(messageIds)
                .build();
    }

    public static NotificationResponse noTargets() {
        return NotificationResponse.builder()
                .totalAttempted(0)
                .successCount(0)
                .failureCount(0)
                .message("No valid targets found for notification")
                .invalidTokens(Collections.emptyList())
                .messageIds(Collections.emptyList())
                .build();
    }

    public static NotificationResponse disabled() {
        return NotificationResponse.builder()
                .totalAttempted(0)
                .successCount(0)
                .failureCount(0)
                .message("Push notifications are disabled")
                .invalidTokens(Collections.emptyList())
                .messageIds(Collections.emptyList())
                .build();
    }
}
