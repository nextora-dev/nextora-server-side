package lk.iit.nextora.module.club.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for joining a club
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinClubRequest {

    @NotNull(message = "Club ID is required")
    private Long clubId;

    private ClubPositionsType position;

    private String remarks;
}
