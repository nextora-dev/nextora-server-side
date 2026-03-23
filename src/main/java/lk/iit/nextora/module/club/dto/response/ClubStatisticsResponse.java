package lk.iit.nextora.module.club.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for club-specific statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubStatisticsResponse {

    private Long clubId;
    private String clubName;
    private String clubCode;

    // Membership Statistics
    private Long totalMembers;
    private Long activeMembers;
    private Long pendingApplications;
    private Long suspendedMembers;
    private Long expiredMemberships;
    private Double membershipGrowthRate;

    // Election Statistics
    private Long totalElections;
    private Long activeElections;
    private Long completedElections;
    private Long cancelledElections;
    private Double averageParticipationRate;
    private Double highestParticipationRate;
    private String highestParticipationElectionTitle;

    // Candidate Statistics
    private Long totalCandidatesAllTime;
    private Long averageCandidatesPerElection;
    private Long totalApprovedCandidates;
    private Long totalRejectedCandidates;

    // Voting Statistics
    private Long totalVotesCastAllTime;
    private Double averageVotesPerElection;
    private Long mostVotesInSingleElection;
    private String mostVotedElectionTitle;

    // Time-based Statistics
    private Map<String, Long> membershipByMonth;
    private Map<String, Long> electionsByMonth;
    private Map<String, Long> votesByMonth;

    // Recent Activity
    private List<RecentActivityItem> recentActivities;

    // Top Members
    private List<TopMemberInfo> mostActiveCandidates;

    // Metadata
    private LocalDateTime generatedAt;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityItem {
        private String activityType;
        private String description;
        private LocalDateTime timestamp;
        private Long relatedEntityId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopMemberInfo {
        private Long memberId;
        private String memberName;
        private Integer candidacyCount;
        private Integer winsCount;
    }
}
