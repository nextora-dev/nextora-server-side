package lk.iit.nextora.module.club.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing a member's position within a club
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMemberPositionRequest {

    @NotNull(message = "Membership ID is required")
    private Long membershipId;

    @NotNull(message = "New position is required")
    private ClubPositionsType newPosition;

    private String reason;
}

