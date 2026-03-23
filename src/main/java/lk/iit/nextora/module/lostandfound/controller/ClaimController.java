package lk.iit.nextora.module.lostandfound.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.LOST_AND_FOUND_CLAIMS)
@RequiredArgsConstructor
@Tag(name = "Lost & Found Claims", description = "Claim management endpoints for lost and found items")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @Operation(summary = "Submit a claim", description = "Submit a claim that a found item belongs to you")
    @PreAuthorize("hasAuthority('LOST_FOUND:CLAIM')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request) {
        ClaimResponse response = claimService.createClaim(request);
        return ApiResponse.success("Claim submitted successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID", description = "Retrieve a specific claim by its ID")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<ClaimResponse> getClaim(@PathVariable Long id) {
        ClaimResponse response = claimService.getClaimById(id);
        return ApiResponse.success("Claim retrieved successfully", response);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my claims", description = "Get all claims submitted by the current student")
    @PreAuthorize("hasAuthority('LOST_FOUND:CLAIM')")
    public ApiResponse<PagedResponse<ClaimResponse>> getMyClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<ClaimResponse> response = claimService.getMyClaims(pageable);
        return ApiResponse.success("Your claims retrieved successfully", response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get claims by status", description = "Get all claims filtered by status (admin)")
    @PreAuthorize("hasAuthority('LOST_FOUND:ADMIN_VIEW')")
    public ApiResponse<PagedResponse<ClaimResponse>> getClaimsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<ClaimResponse> response = claimService.getClaimsByStatus(status.toUpperCase(), pageable);
        return ApiResponse.success("Claims retrieved successfully", response);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve claim", description = "Approve a pending claim (admin)")
    @PreAuthorize("hasAuthority('LOST_FOUND:ADMIN_UPDATE')")
    public ApiResponse<ClaimResponse> approveClaim(@PathVariable Long id) {
        ClaimResponse response = claimService.approveClaim(id);
        return ApiResponse.success("Claim approved successfully", response);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject claim", description = "Reject a pending claim with optional reason (admin)")
    @PreAuthorize("hasAuthority('LOST_FOUND:ADMIN_UPDATE')")
    public ApiResponse<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        ClaimResponse response = claimService.rejectClaim(id, reason);
        return ApiResponse.success("Claim rejected successfully", response);
    }
}
