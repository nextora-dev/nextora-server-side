package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.common.enums.StudentRoleType;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(nullable = false, length = 50)
    private String batch;

    @Column(nullable = false, length = 100)
    private String program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FacultyType faculty;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_role_type", length = 30)
    @Builder.Default
    private StudentRoleType studentRoleType = StudentRoleType.NORMAL;

    private LocalDate enrollmentDate;

    private LocalDate dateOfBirth;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String guardianName;

    @Column(length = 15)
    private String guardianPhone;

    // ==================== CLUB_MEMBER Specific Fields ====================

    @Column(length = 100)
    private String clubName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ClubPositionsType clubPosition;

    private LocalDate clubJoinDate;

    @Column(length = 50)
    private String clubMembershipId;

    // ==================== SENIOR_KUPPI Specific Fields ====================

    @ElementCollection
    @CollectionTable(name = "student_kuppi_subjects", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "subject")
    @Builder.Default
    private Set<String> kuppiSubjects = new HashSet<>();

    @Column(length = 20)
    private String kuppiExperienceLevel;

    private Integer kuppiSessionsCompleted;

    private Double kuppiRating;

    @Column(length = 500)
    private String kuppiAvailability;

    // ==================== BATCH_REP Specific Fields ====================

    @Column(length = 10)
    private String batchRepYear;

    @Column(length = 20)
    private String batchRepSemester;

    private LocalDate batchRepElectedDate;

    @Column(length = 500)
    private String batchRepResponsibilities;

    @Override
    public String getUserType() {
        return "STUDENT";
    }

    /**
     * Get student sub-role display name
     */
    public String getStudentRoleDisplayName() {
        return studentRoleType != null ? studentRoleType.getDisplayName() : StudentRoleType.NORMAL.getDisplayName();
    }

    /**
     * Returns additional permissions based on student sub-role type
     */
    @Override
    protected Set<Permission> getAdditionalPermissions() {
        if (studentRoleType != null) {
            return studentRoleType.getAdditionalPermissions();
        }
        return StudentRoleType.NORMAL.getAdditionalPermissions();
    }
}

