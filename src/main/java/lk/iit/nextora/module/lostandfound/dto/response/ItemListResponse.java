package lk.iit.nextora.module.lostandfound.dto.response;

// ── Java collections ────────────────────────────────────────────────────────
import java.util.List;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response wrapper for the simple (non-pageable) item search endpoint.
 * Returned by GET /lost and GET /found when using SearchItemRequest.
 *
 * ✅ FIX: Added @NoArgsConstructor and @AllArgsConstructor to match Kuppi DTO pattern.
 * ✅ FIX: Added pagination metadata fields (totalPages, pageNumber, pageSize, empty)
 *         so clients can still navigate pages even in the simple-search flow.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemListResponse {

    // The page of item results returned for this request
    private List<ItemResponse> items;

    // Total number of matching items across all pages
    private long totalElements;

    // ✅ FIX: added pagination metadata — the original response gave no way for
    //         the client to know if more pages existed
    // Total number of pages available given the current page size
    private int totalPages;

    // Current zero-based page number
    private int pageNumber;

    // Number of items requested per page
    private int pageSize;

    // True when the items list is empty (no results found)
    private boolean empty;
}