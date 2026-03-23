package lk.iit.nextora.module.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for forgot password flow operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Forgot password operation response")
public class ForgotPasswordResponse {

    @Schema(description = "Operation message", example = "Password reset link sent to your email")
    private String message;

    @Schema(description = "Masked email address where reset link was sent", example = "j***e@iit.ac.lk")
    private String maskedEmail;

    @Schema(description = "Token expiry time in minutes", example = "60")
    private Integer expiryMinutes;
}
