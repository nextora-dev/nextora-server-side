package lk.iit.nextora.infrastructure.notification.push.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRegistrationResponse {

    private Long tokenId;
    private String message;
    private boolean isNew;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public static TokenRegistrationResponse created(Long tokenId, ZonedDateTime createdAt) {
        return TokenRegistrationResponse.builder()
                .tokenId(tokenId)
                .message("FCM token registered successfully")
                .isNew(true)
                .createdAt(createdAt)
                .build();
    }

    public static TokenRegistrationResponse updated(Long tokenId, ZonedDateTime updatedAt) {
        return TokenRegistrationResponse.builder()
                .tokenId(tokenId)
                .message("FCM token updated successfully")
                .isNew(false)
                .updatedAt(updatedAt)
                .build();
    }
}
