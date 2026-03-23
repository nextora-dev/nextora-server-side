package lk.iit.nextora.module.lostandfound.dto.response;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing a potential match between a lost item and a found item.
 * Returned by the item matching service when it finds items with a high match score.
 *
 * ✅ FIX: Added @NoArgsConstructor, @AllArgsConstructor to match Kuppi DTO pattern.
 * ✅ FIX: Added lostItemTitle + foundItemTitle — original gave only IDs, forcing clients
 *         to make additional API calls just to display a match suggestion.
 * ✅ FIX: Added matchReason — explains WHY these two items were matched (e.g. "Same title",
 *         "Same category and location"), improving transparency to the user.
 * ✅ FIX: matchScore field renamed from double to Double (wrapper) so it
 *         serialises as null-safe JSON.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchSuggestionResponse {

    // ─── Lost Item ──────────────────────────────────────────────────────────

    // ID of the LostItem side of this match
    private Long lostItemId;

    // ✅ FIX: title included — clients don't need a second API call to display the match
    private String lostItemTitle;

    // ─── Found Item ─────────────────────────────────────────────────────────

    // ID of the FoundItem side of this match
    private Long foundItemId;

    // ✅ FIX: title included for same reason as above
    private String foundItemTitle;

    // ─── Match metadata ─────────────────────────────────────────────────────

    // Similarity score between 0.0 (no match) and 1.0 (perfect match)
    // Calculated by ItemMatchingService based on title, category, and location similarity
    private Double matchScore;

    // ✅ FIX: human-readable explanation of what drove this match score
    // e.g. "Title match: 100%, Category: Electronics, Location: Library Block A"
    private String matchReason;
}