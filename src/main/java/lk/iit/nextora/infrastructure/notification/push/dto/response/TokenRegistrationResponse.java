package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for token registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRegistrationResponse {

    private Long tokenId;
    private boolean isNew;
    private LocalDateTime registeredAt;
    private String message;

    public static TokenRegistrationResponse created(Long tokenId, LocalDateTime registeredAt) {
        return TokenRegistrationResponse.builder()
                .tokenId(tokenId)
                .isNew(true)
                .registeredAt(registeredAt)
                .message("FCM token registered successfully")
                .build();
    }

    public static TokenRegistrationResponse updated(Long tokenId, LocalDateTime updatedAt) {
        return TokenRegistrationResponse.builder()
                .tokenId(tokenId)
                .isNew(false)
                .registeredAt(updatedAt)
                .message("FCM token updated successfully")
                .build();
    }
}
