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
 * Student registration request with role-specific fields.
 * A student can have multiple role types simultaneously.
 *
 * - NORMAL: Basic student (always included)
 * - CLUB_MEMBER: Requires club-related fields
 * - SENIOR_KUPPI: Requires kuppi-related fields
 * - BATCH_REP: Requires batch representative fields
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentRegisterRequest extends RegisterRequest {

    // ==================== Common Fields ====================

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

    /**
     * Multiple student sub-role types (defaults to NORMAL if not provided)
     * A student can have multiple roles like CLUB_MEMBER and SENIOR_KUPPI simultaneously
     */
    private Set<StudentRoleType> studentRoleTypes;

    /**
     * @deprecated Use studentRoleTypes instead. Kept for backward compatibility.
     */
    @Deprecated
    private StudentRoleType studentRoleType;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @Size(max = 50, message = "Guardian name must not exceed 50 characters")
    private String guardianName;

    @Size(max = 15, message = "Guardian phone must not exceed 15 characters")
    private String guardianPhone;

    // ==================== CLUB_MEMBER Specific Fields ====================

    @Size(max = 100, message = "Club name must not exceed 100 characters")
    private String clubName;

    @Size(max = 50, message = "Club position must not exceed 50 characters")
    private String clubPosition;

    private LocalDate clubJoinDate;

    @Size(max = 50, message = "Club membership ID must not exceed 50 characters")
    private String clubMembershipId;

    // ==================== SENIOR_KUPPI Specific Fields ====================

    /**
     * Subjects the student can teach in Kuppi sessions
     */
    private Set<String> kuppiSubjects;

    @Size(max = 20, message = "Experience level must not exceed 20 characters")
    private String kuppiExperienceLevel;

    @Size(max = 500, message = "Availability must not exceed 500 characters")
    private String kuppiAvailability;

    // ==================== BATCH_REP Specific Fields ====================

    @Size(max = 10, message = "Batch rep year must not exceed 10 characters")
    private String batchRepYear;

    @Size(max = 20, message = "Batch rep semester must not exceed 20 characters")
    private String batchRepSemester;

    private LocalDate batchRepElectedDate;

    @Size(max = 500, message = "Responsibilities must not exceed 500 characters")
    private String batchRepResponsibilities;

    /**
     * Get effective role types - combines new field with deprecated field for backward compatibility
     */
    public Set<StudentRoleType> getEffectiveRoleTypes() {
        if (studentRoleTypes != null && !studentRoleTypes.isEmpty()) {
            return studentRoleTypes;
        }
        if (studentRoleType != null) {
            return Set.of(studentRoleType);
        }
        return Set.of(StudentRoleType.NORMAL);
    }
}