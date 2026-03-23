package lk.iit.nextora.module.club.dto.response.dashboard;
import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Dashboard response for club members (ROLE_STUDENT + CLUB_MEMBER sub-role).
 * Provides club-specific data: members, announcements, elections, activity, and officer capabilities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberDashboardResponse {
    private ClubResponse club;
    private Long myMembershipId;
    private ClubPositionsType myPosition;
    private ClubMembershipStatus myStatus;
    private long totalMembers;
    private long activeMembers;
    private long pendingApplicationsCount;
    private List<ClubAnnouncementResponse> latestAnnouncements;
    private List<ElectionResponse> activeElections;
    private List<ElectionResponse> upcomingElections;
    private List<ClubActivityLogResponse> recentActivity;
    private boolean canManageMembers;
    private boolean canCreateAnnouncements;
    private boolean canManageElections;
    private boolean canViewStats;
    private boolean canUpdateClub;
    private boolean isOfficer;
    private LocalDateTime generatedAt;
}
