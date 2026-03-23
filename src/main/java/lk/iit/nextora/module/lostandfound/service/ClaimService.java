package lk.iit.nextora.module.lostandfound.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import org.springframework.data.domain.Pageable;

public interface ClaimService {

    ClaimResponse createClaim(CreateClaimRequest request);

    ClaimResponse getClaimById(Long id);

    PagedResponse<ClaimResponse> getMyClaims(Pageable pageable);

    PagedResponse<ClaimResponse> getClaimsByStatus(String status, Pageable pageable);

    ClaimResponse approveClaim(Long id);

    ClaimResponse rejectClaim(Long id, String reason);
}
