package lk.iit.nextora.module.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for creating Admin users by Super Admin.
 * Only Super Admin can create Admin users.
 * Note: Super Admin cannot be created via API - only one exists (created via DataInitializer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @Size(max = 15, message = "Phone number must be at most 15 characters")
    private String phone;

    @NotBlank(message = "Admin ID is required")
    @Size(max = 20, message = "Admin ID must be at most 20 characters")
    private String adminId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;
}
