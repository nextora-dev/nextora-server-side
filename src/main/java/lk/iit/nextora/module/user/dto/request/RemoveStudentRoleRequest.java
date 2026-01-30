package lk.iit.nextora.module.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.iit.nextora.common.enums.StudentRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for removing a role from a student.
 * Note: NORMAL role cannot be removed - it's the base role for all students.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveStudentRoleRequest {

    @NotNull(message = "Role type is required")
    private StudentRoleType roleType;

    // Reason for role removal (required for audit purposes)
    private String reason;
}
