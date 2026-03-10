package lk.iit.nextora.module.lostandfound.controller;

// ── Swagger / OpenAPI annotations ──────────────────────────────────────────
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// ── Jakarta validation ──────────────────────────────────────────────────────
import jakarta.validation.Valid;

// ── Common project constants and DTOs ──────────────────────────────────────
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;

// ── Lost & Found request / response DTOs ───────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;

// ── Lost & Found service ────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.ClaimService;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;

// ── Spring Data pagination ──────────────────────────────────────────────────
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// ── Spring Web / Security ───────────────────────────────────────────────────
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Claim operations in the Lost & Found module.
 * A Claim is created when a student believes a found item is theirs.
 * Base URL: /api/v1/lost-and-found/claims
 */
@RestController
// ✅ FIX: was "/api/v1/claims" — must live under the module's own path, not root
@RequestMapping(ApiConstants.LOST_AND_FOUND_CLAIMS)
@RequiredArgsConstructor
// ✅ FIX: added @Tag — was completely missing, the Kuppi module uses it on every controller
@Tag(name = "Lost & Found Claims", description = "Claim management endpoints for lost and found items")
public class ClaimController {

    // Service that handles all claim business logic
    private final ClaimService claimService;

    /**
     * POST /
     * Submit a new claim linking a lost item to a found item.
     * The student provides proof that the found item belongs to them.
     */
    @PostMapping
    // ✅ FIX: @Operation was completely missing
    @Operation(summary = "Submit a claim", description = "Submit a claim that a found item belongs to you")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:CLAIM')")
    // ✅ FIX: POST must return 201 Created, not 200 OK
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request) {
        // Service validates that both lostItem and foundItem exist before creating claim
        ClaimResponse response = claimService.createClaim(request);
        return ApiResponse.success("Claim submitted successfully", response);
    }

    /**
     * GET /{id}
     * Retrieve a specific claim by its ID.
     */
    @GetMapping("/{id}")
    // ✅ FIX: @Operation was missing
    @Operation(summary = "Get claim by ID", description = "Retrieve a specific claim by its ID")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<ClaimResponse> getClaim(
            @PathVariable Long id) {
        ClaimResponse response = claimService.getClaimById(id);
        return ApiResponse.success("Claim retrieved successfully", response);
    }

    /**
     * GET /my
     * Retrieve all claims submitted by the currently authenticated student.
     * Uses pagination identical to the Kuppi module module's "my notes / my sessions" endpoints.
     */
    @GetMapping("/my")
    @Operation(summary = "Get my claims", description = "Get all claims submitted by the current student")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<PagedResponse<ClaimResponse>> getMyClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Build Pageable — no sort needed, default order by createdAt DESC in service
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClaimResponse> response = claimService.getMyClaims(pageable);
        return ApiResponse.success("Your claims retrieved successfully", response);
    }

    /**
     * GET /status/{status}
     * Filter claims by status (PENDING, APPROVED, REJECTED).
     * Admin-facing endpoint for managing the claims review queue.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get claims by status", description = "Get all claims filtered by status (admin)")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:ADMIN_VIEW')")
    public ApiResponse<PagedResponse<ClaimResponse>> getClaimsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClaimResponse> response = claimService.getClaimsByStatus(status, pageable);
        return ApiResponse.success("Claims retrieved successfully", response);
    }

    /**
     * PUT /{id}/approve
     * Admin approves a pending claim — marks the item as returned.
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve claim", description = "Approve a pending claim (admin)")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:ADMIN_UPDATE')")
    public ApiResponse<ClaimResponse> approveClaim(@PathVariable Long id) {
        ClaimResponse response = claimService.approveClaim(id);
        return ApiResponse.success("Claim approved successfully", response);
    }

    /**
     * PUT /{id}/reject
     * Admin rejects a pending claim with an optional reason.
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject claim", description = "Reject a pending claim (admin)")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:ADMIN_UPDATE')")
    public ApiResponse<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            // Optional reason for rejection shown to the student
            @RequestParam(required = false) String reason) {
        ClaimResponse response = claimService.rejectClaim(id, reason);
        return ApiResponse.success("Claim rejected successfully", response);
    }
}