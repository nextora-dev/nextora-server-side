package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.StudentRoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

/**
 * Admin request to create a NORMAL Student user.
 * Students are always created as NORMAL - additional roles (CLUB_MEMBER, BATCH_REP, KUPPI_STUDENT)
 * are added progressively through system activities via the Student Role Management APIs.
 *
 * Student Lifecycle Flow:
 * 1. Admin creates student with this request → ROLE_STUDENT with StudentRoleType.NORMAL
 * 2. System sends email with login credentials
 * 3. Student logs in and changes password
 * 4. Additional roles are added progressively:
 *    - CLUB_MEMBER: When student joins a club (via club membership approval)
 *    - BATCH_REP: When admin assigns student as Batch Representative
 *    - KUPPI_STUDENT: When admin approves student's Kuppi session request
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCreateStudentRequest extends AdminCreateUserRequest {

    @NotBlank(message = "Student ID is required")
    @Size(max = 20, message = "Student ID must not exceed 20 characters")
    private String studentId;

    @NotBlank(message = "Batch is required")
    @Size(max = 50, message = "Batch must not exceed 50 characters")
    private String batch;

    @NotBlank(message = "Program is required")
    @Size(max = 100, message = "Program must not exceed 100 characters")
    private String program;

    @NotNull(message = "Faculty is required")
    private FacultyType faculty;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 200)
    private String address;

    @Size(max = 50)
    private String guardianName;

    @Size(max = 15)
    private String guardianPhone;

    /**
     * Returns only NORMAL role for admin-created students.
     * Additional roles must be added through the Student Role Management APIs.
     * This ensures the proper Admin-Controlled Student Role Upgrade Flow.
     */
    public Set<StudentRoleType> getEffectiveRoleTypes() {
        return Set.of(StudentRoleType.NORMAL);
    }

    // ==================== Placeholder methods for backward compatibility ====================
    // These return null as admin-created students should not have these fields set initially

    public String getClubName() { return null; }
    public LocalDate getClubJoinDate() { return null; }
    public String getClubMembershipId() { return null; }
    public Set<String> getKuppiSubjects() { return null; }
    public String getKuppiExperienceLevel() { return null; }
    public String getKuppiAvailability() { return null; }
    public String getBatchRepYear() { return null; }
    public String getBatchRepSemester() { return null; }
    public LocalDate getBatchRepElectedDate() { return null; }
    public String getBatchRepResponsibilities() { return null; }
}
