package lk.iit.nextora.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resetting password with verified token.
 * User provides the verified token and new password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reset password with token request")
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset token received via email", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\[\\]{}|;:',.<>/-]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(description = "New password (min 8 chars, must include uppercase, lowercase, digit, and special char)",
            example = "NewSecure@123")
    @JsonAlias({"password", "new_password"})
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password - must match newPassword", example = "NewSecure@123")
    @JsonAlias({"confirm_password", "passwordConfirm", "confirmNewPassword"})
    private String confirmPassword;
}
