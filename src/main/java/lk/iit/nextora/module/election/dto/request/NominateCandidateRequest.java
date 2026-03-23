package lk.iit.nextora.module.election.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for nominating as a candidate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NominateCandidateRequest {

    @NotNull(message = "Election ID is required")
    private Long electionId;

    @Size(max = 3000, message = "Manifesto must not exceed 3000 characters")
    private String manifesto;

    @Size(max = 500, message = "Slogan must not exceed 500 characters")
    private String slogan;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @Size(max = 1000, message = "Qualifications must not exceed 1000 characters")
    private String qualifications;

    @Size(max = 500, message = "Previous experience must not exceed 500 characters")
    private String previousExperience;
}
