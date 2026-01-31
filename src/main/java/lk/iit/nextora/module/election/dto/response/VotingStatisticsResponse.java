package lk.iit.nextora.module.election.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for voting module overall statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingStatisticsResponse {

    // Club Statistics
    private Long totalClubs;
    private Long activeClubs;
    private Long totalMembers;
    private Long activeMembers;
    private Long pendingMembershipApplications;

    // Election Statistics
    private Long totalElections;
    private Long activeElections;
    private Long upcomingElections;
    private Long completedElections;
    private Long cancelledElections;

    // Voting Statistics
    private Long totalVotesCast;
    private Double averageParticipationRate;
    private Long totalCandidates;
    private Long approvedCandidates;
    private Long pendingCandidates;

    // Time-based Statistics
    private Map<String, Long> electionsByMonth;
    private Map<String, Long> votesByMonth;
    private Map<String, Long> membershipGrowthByMonth;

    // Top Statistics
    private String mostActiveClub;
    private Long mostActiveClubMemberCount;
    private String highestParticipationElection;
    private Double highestParticipationRate;

    // Metadata
    private LocalDateTime generatedAt;
    private String message;
}
