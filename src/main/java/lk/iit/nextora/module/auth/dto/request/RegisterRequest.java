package lk.iit.nextora.module.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "role",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StudentRegisterRequest.class, name = "ROLE_STUDENT"),
        @JsonSubTypes.Type(value = LecturerRegisterRequest.class, name = "ROLE_LECTURER"),
        @JsonSubTypes.Type(value = AcademicStaffRegisterRequest.class, name = "ROLE_ACADEMIC_STAFF"),
        @JsonSubTypes.Type(value = NonAcademicStaffRegisterRequest.class, name = "ROLE_NON_ACADEMIC_STAFF"),
        @JsonSubTypes.Type(value = AdminRegisterRequest.class, name = "ROLE_ADMIN"),
        @JsonSubTypes.Type(value = SuperAdminRegisterRequest.class, name = "ROLE_SUPER_ADMIN")
})
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    // Optional phone field used by registration strategies
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Role is required")
    private UserRole role;
}
