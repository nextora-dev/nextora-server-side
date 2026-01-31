package lk.iit.nextora.module.election.dto.response;

import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.enums.ElectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for election details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResponse {

    private Long id;
    private String title;
    private String description;
    private ElectionType electionType;
    private ElectionStatus status;

    // Club details
    private Long clubId;
    private String clubName;
    private String clubCode;

    // Schedule
    private LocalDateTime nominationStartTime;
    private LocalDateTime nominationEndTime;
    private LocalDateTime votingStartTime;
    private LocalDateTime votingEndTime;
    private LocalDateTime resultsPublishedAt;

    // Settings
    private Integer maxCandidates;
    private Integer winnersCount;
    private Boolean isAnonymousVoting;
    private Boolean requireManifesto;
    private String eligibilityCriteria;

    // Creator
    private Long createdById;
    private String createdByName;

    // Statistics
    private Integer totalCandidates;
    private Integer approvedCandidates;
    private Integer totalVotes;
    private Integer eligibleVoters;
    private Double participationRate;

    // Status flags
    private Boolean canNominate;
    private Boolean canVote;
    private Boolean canViewResults;
    private Boolean isActive;

    // Candidates (optional, based on request)
    private List<CandidateResponse> candidates;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
