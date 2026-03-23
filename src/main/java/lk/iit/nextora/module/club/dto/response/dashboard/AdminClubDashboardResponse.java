package lk.iit.nextora.module.club.dto.response.dashboard;

import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminClubDashboardResponse {

    private long totalClubs;
    private long activeClubs;
    private long openForRegistrationCount;
    private long totalMembersAcrossClubs;
    private long pendingRequestsAcrossClubs;
    private long totalElections;
    private long activeElectionsCount;
    private long completedElections;
    private long cancelledElections;
    private List<ClubResponse> recentlyCreatedClubs;
    private List<StaffClubDashboardResponse.PendingClubItem> clubsWithPendingApplications;
    private List<ClubActivityLogResponse> recentActivity;
    private Map<String, Long> clubsCreatedByMonth;
    private LocalDateTime generatedAt;
}

