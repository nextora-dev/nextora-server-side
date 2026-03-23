package lk.iit.nextora.module.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user statistics summary.
 * Provides counts of users by status and total users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsSummaryResponse {

    private long totalUsers;
    private long activeUsers;
    private long deactivatedUsers;
    private long suspendedUsers;
    private long deletedUsers;
    private long passwordChangeRequiredUsers;

    // Role-based counts
    private long totalStudents;
    private long totalAdmins;
    private long totalSuperAdmins;
    private long totalAcademicStaff;
    private long totalNonAcademicStaff;
}

