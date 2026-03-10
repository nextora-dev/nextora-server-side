package lk.iit.nextora.module.lostandfound.dto.response;

// ── Java time ───────────────────────────────────────────────────────────────
import java.time.LocalDateTime;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after creating or retrieving a Claim.
 *
 * ✅ FIX: Added @NoArgsConstructor, @AllArgsConstructor to match Kuppi pattern.
 * ✅ FIX: Added proofDescription — it was in CreateClaimRequest but silently dropped here.
 * ✅ FIX: Added rejectionReason — needed so the student knows WHY their claim was rejected.
 * ✅ FIX: Added claimantId + claimantName — the response gave no info about WHO claimed.
 * ✅ FIX: Added lostItemTitle + foundItemTitle — avoids N+1 lookups from the client side.
 * ✅ FIX: Added createdAt — every Kuppi response exposes the creation timestamp.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    // Database primary key of the claim
    private Long id;

    // ─── Lost Item info ────────────────────────────────────────────────────

    // ID of the LostItem this claim is linked to
    private Long lostItemId;

    // ✅ FIX: title included so clients don't need a second GET /lost/{id} call
    private String lostItemTitle;

    // ─── Found Item info ───────────────────────────────────────────────────

    // ID of the FoundItem this claim is linked to
    private Long foundItemId;

    // ✅ FIX: title included so clients don't need a second GET /found/{id} call
    private String foundItemTitle;

    // ─── Claimant info ─────────────────────────────────────────────────────

    // ✅ FIX: claimant info was entirely missing from the response
    // ID of the student who submitted the claim
    private Long claimantId;

    // Full name of the student who submitted the claim
    private String claimantName;

    // ─── Claim details ─────────────────────────────────────────────────────

    // ✅ FIX: proofDescription was in the request but never returned in the response
    // Text proof provided by the claimant to support their ownership claim
    private String proofDescription;

    // Current status of the claim: PENDING | APPROVED | REJECTED
    private String status;

    // ✅ FIX: rejectionReason was completely absent — students had no way to see why
    //         their claim was rejected
    // Optional reason provided by admin when rejecting a claim
    private String rejectionReason;

    // ─── Timestamps ────────────────────────────────────────────────────────

    // ✅ FIX: timestamps were missing — all Kuppi responses expose createdAt
    private LocalDateTime createdAt;

    // When the claim was last updated (e.g. when status changed to APPROVED/REJECTED)
    private LocalDateTime updatedAt;
}