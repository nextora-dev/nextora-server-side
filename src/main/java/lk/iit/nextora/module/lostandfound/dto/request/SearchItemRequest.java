package lk.iit.nextora.module.lostandfound.dto.request;

// ── Jakarta validation constraints ──────────────────────────────────────────
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the simple (non-pageable) search endpoint.
 * Bound via @ModelAttribute from query parameters — no @RequestBody needed.
 *
 * Used by:
 *   GET /lost    → lostItemService.searchLostItems(SearchItemRequest)
 *   GET /found   → foundItemService.searchFoundItems(SearchItemRequest)
 *
 * ✅ FIX: Added @Builder, @Data, @AllArgsConstructor to match Kuppi DTO pattern.
 * ✅ FIX: Added @Size, @Min, @Max constraints for server-side input safety.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchItemRequest {

    // Free-text keyword to match against item title (case-insensitive LIKE query)
    @Size(max = 200, message = "Keyword must not exceed 200 characters")
    private String keyword;

    // Category name to filter by (e.g. "Electronics") — case-insensitive match
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    // Zero-based page number — defaults to first page
    @Min(value = 0, message = "Page number must be 0 or greater")
    @Builder.Default
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private int size = 10;
}