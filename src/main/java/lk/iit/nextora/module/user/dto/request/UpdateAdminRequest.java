package lk.iit.nextora.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for updating Admin users by Super Admin.
 * Only Super Admin can update Admin users.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating an admin user. All fields are optional.")
public class UpdateAdminRequest {

    @Email(message = "Invalid email format")
    @Schema(description = "New email address for the admin", example = "admin@nextora.lk")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "New password for the admin (optional)", example = "NewSecure@123")
    private String password;

    @Size(max = 50, message = "First name must be at most 50 characters")
    @Schema(description = "First name of the admin", example = "John")
    private String firstName;

    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Schema(description = "Last name of the admin", example = "Doe")
    private String lastName;

    @Size(max = 15, message = "Phone number must be at most 15 characters")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Phone number of the admin", example = "+94771234567")
    private String phone;

    @Size(max = 100, message = "Department must be at most 100 characters")
    @Schema(description = "Department of the admin", example = "IT Administration")
    private String department;

    /**
     * Updated permissions for Admin user.
     * If provided, this will replace all existing permissions.
     */
    @Schema(description = "Set of permissions for the admin. Replaces existing permissions if provided.")
    private Set<String> permissions;

    /**
     * Check if any update field is provided
     */
    public boolean hasAnyUpdate() {
        return email != null || password != null || firstName != null ||
                lastName != null || phone != null || department != null ||
                permissions != null;
    }
}

