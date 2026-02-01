package lk.iit.nextora.infrastructure.notification.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering an FCM token.
 * Sent by the frontend after obtaining a token from Firebase.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTokenRequest {

    @NotBlank(message = "FCM token is required")
    @Size(max = 512, message = "FCM token must not exceed 512 characters")
    private String token;

    /**
     * Optional device information for debugging.
     * Example: "Chrome on Windows", "Safari on iPhone"
     */
    @Size(max = 255, message = "Device info must not exceed 255 characters")
    private String deviceInfo;
}
