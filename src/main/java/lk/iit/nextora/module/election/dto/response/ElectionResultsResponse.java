package lk.iit.nextora.module.election.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for election results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResultsResponse {

    private Long electionId;
    private String electionTitle;
    private String clubName;
    private LocalDateTime resultsPublishedAt;

    // Summary
    private Integer totalVotes;
    private Integer eligibleVoters;
    private Double participationRate;
    private Integer winnersCount;

    // Winners
    private List<CandidateResponse> winners;

    // All candidates ranked
    private List<CandidateResponse> rankedCandidates;

    // Voting statistics
    private VotingStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotingStatistics {
        private Map<String, Long> votesByDate;
        private Map<Integer, Long> votesByHour;
        private LocalDateTime firstVoteAt;
        private LocalDateTime lastVoteAt;
        private Double averageVotesPerHour;
    }
}
