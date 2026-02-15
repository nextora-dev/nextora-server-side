package lk.iit.nextora.module.lostandfound.mapper;

import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.entity.Claim;

public class ClaimMapper {

    public static ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .lostItemId(claim.getLostItem().getId())
                .foundItemId(claim.getFoundItem().getId())
                .status(claim.getStatus())
                .build();
    }
}
