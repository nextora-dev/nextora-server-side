package lk.iit.nextora.module.lostandfound.service;

// ── Common project DTOs ─────────────────────────────────────────────────────
import lk.iit.nextora.common.dto.PagedResponse;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateClaimRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;

// ── Spring Data ─────────────────────────────────────────────────────────────
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Claim operations in the Lost & Found module.
 *
 * ✅ FIX: Added getMyClaims, getClaimsByStatus, approveClaim, rejectClaim —
 *         all of which were missing from the original interface but are needed
 *         by the new ClaimController endpoints.
 */
public interface ClaimService {

    // =====================================================================
    // Student Operations
    // =====================================================================

    /**
     * Submit a new claim asserting that a found item belongs to this student.
     * Validates that both the lost item and found item exist before creating the claim.
     */
    ClaimResponse createClaim(CreateClaimRequest request);

    /**
     * Retrieve a specific claim by its ID.
     */
    ClaimResponse getClaimById(Long id);

    /**
     * Get all claims submitted by the currently authenticated student.
     * Paginated — used by GET /claims/my.
     *
     * ✅ FIX: was completely missing — required by ClaimController.getMyClaims()
     */
    PagedResponse<ClaimResponse> getMyClaims(Pageable pageable);

    // =====================================================================
    // Admin Operations
    // =====================================================================

    /**
     * Get all claims filtered by status (PENDING / APPROVED / REJECTED).
     * Paginated — used by GET /claims/status/{status}.
     *
     * ✅ FIX: was completely missing — required by ClaimController.getClaimsByStatus()
     */
    PagedResponse<ClaimResponse> getClaimsByStatus(String status, Pageable pageable);

    /**
     * Approve a pending claim.
     * Sets status → APPROVED and marks both linked items as inactive (resolved).
     *
     * ✅ FIX: was completely missing — required by ClaimController.approveClaim()
     */
    ClaimResponse approveClaim(Long id);

    /**
     * Reject a pending claim with an optional rejection reason.
     * Sets status → REJECTED and stores the reason on the Claim entity.
     *
     * ✅ FIX: was completely missing — required by ClaimController.rejectClaim()
     */
    ClaimResponse rejectClaim(Long id, String reason);
}