package lk.iit.nextora.module.election.dto.request;

import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.ElectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an election
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateElectionRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private ElectionType electionType;

    private LocalDateTime nominationStartTime;

    private LocalDateTime nominationEndTime;

    private LocalDateTime votingStartTime;

    private LocalDateTime votingEndTime;

    private Integer maxCandidates;

    private Integer winnersCount;

    private Boolean isAnonymousVoting;

    private Boolean requireManifesto;

    @Size(max = 1000, message = "Eligibility criteria must not exceed 1000 characters")
    private String eligibilityCriteria;
}
