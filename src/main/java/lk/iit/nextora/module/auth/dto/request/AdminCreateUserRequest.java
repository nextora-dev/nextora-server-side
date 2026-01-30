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

/**
 * Base request for Admin/Super Admin to create users.
 * Only ROLE_STUDENT, ROLE_ACADEMIC_STAFF, and ROLE_NON_ACADEMIC_STAFF can be created.
 * Password is NOT provided - system generates a temporary password.
 */
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
        @JsonSubTypes.Type(value = AdminCreateStudentRequest.class, name = "ROLE_STUDENT"),
        @JsonSubTypes.Type(value = AdminCreateAcademicStaffRequest.class, name = "ROLE_ACADEMIC_STAFF"),
        @JsonSubTypes.Type(value = AdminCreateNonAcademicStaffRequest.class, name = "ROLE_NON_ACADEMIC_STAFF")
})
public class AdminCreateUserRequest {

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

    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;
}
