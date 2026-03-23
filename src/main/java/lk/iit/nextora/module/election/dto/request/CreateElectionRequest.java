package lk.iit.nextora.module.election.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.ElectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new election
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateElectionRequest {

    @NotBlank(message = "Election title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Club ID is required")
    private Long clubId;

    @NotNull(message = "Election type is required")
    private ElectionType electionType;

    @NotNull(message = "Nomination start time is required")
    private LocalDateTime nominationStartTime;

    @NotNull(message = "Nomination end time is required")
    private LocalDateTime nominationEndTime;

    @NotNull(message = "Voting start time is required")
    private LocalDateTime votingStartTime;

    @NotNull(message = "Voting end time is required")
    private LocalDateTime votingEndTime;

    @Builder.Default
    private Integer maxCandidates = 10;

    @Builder.Default
    private Integer winnersCount = 1;

    @Builder.Default
    private Boolean isAnonymousVoting = true;

    @Builder.Default
    private Boolean requireManifesto = false;

    @Size(max = 1000, message = "Eligibility criteria must not exceed 1000 characters")
    private String eligibilityCriteria;
}
