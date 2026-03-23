package lk.iit.nextora.module.lostandfound.dto.request;

// ── Jakarta validation constraints ──────────────────────────────────────────
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a claim on a found item.
 * A claim links a LostItem (reported by the owner) to a FoundItem (reported by the finder),
 * and provides optional proof that the found item is theirs.
 *
 * ✅ FIX: Added @Builder, @Data, @AllArgsConstructor to match Kuppi request DTO pattern.
 * ✅ FIX: proofDescription was being silently dropped — now properly validated and sized.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequest {

    // ID of the LostItem the claimant previously reported as missing
    @NotNull(message = "Lost item ID is required")
    private Long lostItemId;

    // ID of the FoundItem that the claimant believes is theirs
    @NotNull(message = "Found item ID is required")
    private Long foundItemId;

    // ✅ FIX: this field existed in the request but was NEVER persisted to the Claim entity.
    // The Claim entity now also has this field so it is properly saved and returned.
    // Optional text describing how the claimant can prove ownership
    // (e.g. "I have the receipt", "There is a sticker with my name inside")
    @Size(max = 1000, message = "Proof description must not exceed 1000 characters")
    private String proofDescription;
}