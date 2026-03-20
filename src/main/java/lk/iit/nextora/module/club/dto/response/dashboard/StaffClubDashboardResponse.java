package lk.iit.nextora.module.club.dto.response.dashboard;

import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard response for Non-Academic Staff (ROLE_NON_ACADEMIC_STAFF).
 * Platform-wide club management overview with aggregated stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffClubDashboardResponse {

    private long totalClubs;
    private long activeClubs;
    private long openForRegistrationCount;
    private long totalMembersAcrossClubs;
    private long pendingRequestsAcrossClubs;
    private long activeElectionsCount;
    private long totalElections;

    private List<ClubResponse> recentlyCreatedClubs;
    private List<PendingClubItem> clubsWithPendingApplications;
    private List<ClubActivityLogResponse> recentActivity;

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingClubItem {
        private Long clubId;
        private String clubName;
        private String clubCode;
        private long pendingCount;
    }
}

