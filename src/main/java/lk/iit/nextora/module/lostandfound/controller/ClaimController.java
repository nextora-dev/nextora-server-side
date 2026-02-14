package lk.iit.nextora.module.lostandfound.controller;

import jakarta.validation.Valid;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ApiResponse<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request
    ) {
        return ApiResponse.success(
                claimService.createClaim(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ClaimResponse> getClaim(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                claimService.getClaimById(id)
        );
    }
}
