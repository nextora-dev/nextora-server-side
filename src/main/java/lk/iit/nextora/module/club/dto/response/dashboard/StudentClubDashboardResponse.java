package lk.iit.nextora.module.club.dto.response.dashboard;

import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard response for normal students (ROLE_STUDENT without CLUB_MEMBER sub-role).
 * Provides an overview: browseable clubs, open registrations, memberships, public announcements, upcoming elections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentClubDashboardResponse {

    // Overview counts
    private long totalActiveClubs;
    private long openForRegistrationCount;
    private long myMembershipsCount;
    private long myPendingApplicationsCount;

    // Featured clubs (recently created, open for registration)
    private List<ClubResponse> featuredClubs;

    // Latest public announcements across all clubs
    private List<ClubAnnouncementResponse> latestPublicAnnouncements;

    // My active memberships summary
    private List<MembershipSummaryItem> myMemberships;

    // Metadata
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipSummaryItem {
        private Long clubId;
        private String clubName;
        private String clubCode;
        private String position;
        private String status;
        private String logoUrl;
    }
}

