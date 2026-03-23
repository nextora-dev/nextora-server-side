package lk.iit.nextora.module.election.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for vote confirmation (receipt)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private Long electionId;
    private String electionTitle;
    private String verificationToken;
    private LocalDateTime votedAt;
    private String message;

    // For non-anonymous elections
    private Long candidateId;
    private String candidateName;
}
