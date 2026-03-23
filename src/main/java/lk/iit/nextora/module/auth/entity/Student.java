package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.common.enums.StudentRoleType;
import lombok.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Multiple student role types - a student can have one or more roles
     * Every student has at least NORMAL role
     */
    @ElementCollection(targetClass = StudentRoleType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "student_role_types", joinColumns = @JoinColumn(name = "student_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 30)
    @Builder.Default
    private Set<StudentRoleType> studentRoleTypes = EnumSet.of(StudentRoleType.NORMAL);

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
     * Check if student has a specific role type
     */
    public boolean hasRoleType(StudentRoleType roleType) {
        return studentRoleTypes != null && studentRoleTypes.contains(roleType);
    }

    /**
     * Add a role type to the student
     */
    public void addRoleType(StudentRoleType roleType) {
        if (studentRoleTypes == null) {
            studentRoleTypes = EnumSet.of(StudentRoleType.NORMAL);
        }
        studentRoleTypes.add(roleType);
    }

    /**
     * Remove a role type from the student (cannot remove NORMAL)
     */
    public void removeRoleType(StudentRoleType roleType) {
        if (roleType != StudentRoleType.NORMAL && studentRoleTypes != null) {
            studentRoleTypes.remove(roleType);
        }
    }

    /**
     * Get student sub-role display names (comma separated)
     */
    public String getStudentRoleDisplayName() {
        if (studentRoleTypes == null || studentRoleTypes.isEmpty()) {
            return StudentRoleType.NORMAL.getDisplayName();
        }
        return studentRoleTypes.stream()
                .map(StudentRoleType::getDisplayName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get the primary (highest priority) role type for display purposes
     * Priority: KUPPI_STUDENT > BATCH_REP > CLUB_MEMBER > NORMAL
     */
    public StudentRoleType getPrimaryRoleType() {
        if (studentRoleTypes == null || studentRoleTypes.isEmpty()) {
            return StudentRoleType.NORMAL;
        }
        // Check for KUPPI_STUDENT first (new role), then deprecated SENIOR_KUPPI for backward compatibility
        if (studentRoleTypes.contains(StudentRoleType.KUPPI_STUDENT)) {
            return StudentRoleType.KUPPI_STUDENT;
        }
        if (studentRoleTypes.contains(StudentRoleType.SENIOR_KUPPI)) {
            return StudentRoleType.KUPPI_STUDENT; // Return new role name for display
        }
        if (studentRoleTypes.contains(StudentRoleType.BATCH_REP)) {
            return StudentRoleType.BATCH_REP;
        }
        if (studentRoleTypes.contains(StudentRoleType.CLUB_MEMBER)) {
            return StudentRoleType.CLUB_MEMBER;
        }
        return StudentRoleType.NORMAL;
    }

    /**
     * Check if student has Kuppi capabilities (either new KUPPI_STUDENT or deprecated SENIOR_KUPPI)
     */
    public boolean hasKuppiCapability() {
        return hasRoleType(StudentRoleType.KUPPI_STUDENT) || hasRoleType(StudentRoleType.SENIOR_KUPPI);
    }

    /**
     * Returns combined additional permissions from all student sub-role types
     */
    @Override
    protected Set<Permission> getAdditionalPermissions() {
        if (studentRoleTypes == null || studentRoleTypes.isEmpty()) {
            return StudentRoleType.NORMAL.getAdditionalPermissions();
        }

        Set<Permission> combinedPermissions = new HashSet<>();
        for (StudentRoleType roleType : studentRoleTypes) {
            combinedPermissions.addAll(roleType.getAdditionalPermissions());
        }
        return combinedPermissions;
    }

    // ==================== Backward compatibility methods ====================

    /**
     * @deprecated Use getStudentRoleTypes() instead. This returns the primary role for backward compatibility.
     */
    @Deprecated
    public StudentRoleType getStudentRoleType() {
        return getPrimaryRoleType();
    }

    /**
     * @deprecated Use addRoleType() instead. This sets a single role type for backward compatibility.
     */
    @Deprecated
    public void setStudentRoleType(StudentRoleType roleType) {
        if (roleType == null) {
            this.studentRoleTypes = EnumSet.of(StudentRoleType.NORMAL);
        } else if (roleType == StudentRoleType.NORMAL) {
            this.studentRoleTypes = EnumSet.of(StudentRoleType.NORMAL);
        } else {
            if (this.studentRoleTypes == null) {
                this.studentRoleTypes = EnumSet.of(StudentRoleType.NORMAL);
            }
            this.studentRoleTypes.add(roleType);
        }
    }
}

