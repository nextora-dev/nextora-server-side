package lk.iit.nextora.module.user.dto.request;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        @NotBlank(message = "New password is required")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
}




