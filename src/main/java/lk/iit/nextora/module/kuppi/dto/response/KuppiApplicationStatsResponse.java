package lk.iit.nextora.module.kuppi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Kuppi Application statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiApplicationStatsResponse {

    private long totalApplications;
    private long pendingApplications;
    private long underReviewApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long cancelledApplications;
    private long applicationsToday;
    private long totalKuppiStudents;
}

