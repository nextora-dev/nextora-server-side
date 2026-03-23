package lk.iit.nextora.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for initiating forgot password flow.
 * User provides email and role, system sends verification token to email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Forgot password request - sends verification token to user's email")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User's registered email address", example = "john.doe@iit.ac.lk")
    private String email;

    @Schema(description = "User's role (optional, helps find correct user type)", example = "ROLE_STUDENT")
    private UserRole role;
}
