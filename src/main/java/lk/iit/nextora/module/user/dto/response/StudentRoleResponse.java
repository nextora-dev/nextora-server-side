package lk.iit.nextora.module.user.dto.response;

import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for student role information.
 * Shows all roles assigned to a student.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRoleResponse {

    private Long id;
    private String studentId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String batch;
    private String program;
    private String faculty;
    private UserStatus status;

    // All roles assigned to the student (cumulative)
    private Set<StudentRoleType> roles;

    // Primary role for display purposes
    private StudentRoleType primaryRole;

    // Role display names (comma separated)
    private String roleDisplayNames;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Role-specific data
    private ClubMemberData clubMemberData;
    private BatchRepData batchRepData;
    private KuppiStudentData kuppiStudentData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClubMemberData {
        private String clubName;
        private String clubPosition;
        private String clubJoinDate;
        private String clubMembershipId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchRepData {
        private String batchRepYear;
        private String batchRepSemester;
        private String batchRepElectedDate;
        private String batchRepResponsibilities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KuppiStudentData {
        private Set<String> kuppiSubjects;
        private String kuppiExperienceLevel;
        private String kuppiAvailability;
        private Integer kuppiSessionsCompleted;
        private Double kuppiRating;
    }
}
