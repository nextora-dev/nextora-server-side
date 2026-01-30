package lk.iit.nextora.module.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.enums.StudentRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request DTO for adding a role to a student.
 * Used by Admin/Super Admin to upgrade student capabilities.
 *
 * Role Upgrade Rules:
 * - Roles are never removed automatically
 * - New roles are added on top of existing roles
 * - Admin approval is required for: BATCH_REP, KUPPI_STUDENT
 * - CLUB_MEMBER can be granted through club join approval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddStudentRoleRequest {

    @NotNull(message = "Role type is required")
    private StudentRoleType roleType;

    // Optional reason for role assignment (for audit purposes)
    private String reason;

    // ==================== CLUB_MEMBER Specific Fields ====================

    private String clubName;

    private ClubPositionsType clubPosition;

    private LocalDate clubJoinDate;

    private String clubMembershipId;

    // ==================== BATCH_REP Specific Fields ====================

    private String batchRepYear;

    private String batchRepSemester;

    private LocalDate batchRepElectedDate;

    private String batchRepResponsibilities;

    // ==================== KUPPI_STUDENT Specific Fields ====================

    private Set<String> kuppiSubjects;

    private String kuppiExperienceLevel;

    private String kuppiAvailability;
}
