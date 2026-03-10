package lk.iit.nextora.module.lostandfound.service;

// ── Common project DTOs ─────────────────────────────────────────────────────
import lk.iit.nextora.common.dto.PagedResponse;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Spring Data ─────────────────────────────────────────────────────────────
import org.springframework.data.domain.Pageable;

/**
 * Service interface for found item operations in the Lost & Found module.
 * Mirrors LostItemService — symmetric API design for lost vs found items.
 */
public interface FoundItemService {

    // =====================================================================
    // Student Operations
    // =====================================================================

    /**
     * Report a newly found item on campus.
     * Any authenticated student can report a found item.
     */
    ItemResponse createFoundItem(CreateFoundItemRequest request);

    /**
     * Update an existing found item by ID.
     * Only the original finder (or an admin) may update the item.
     */
    ItemResponse updateFoundItem(Long id, UpdateItemRequest request);

    /**
     * Retrieve a single found item by ID.
     */
    ItemResponse getFoundItemById(Long id);

    /**
     * Simple keyword + category search — backward-compatible endpoint.
     */
    ItemListResponse searchFoundItems(SearchItemRequest request);

    /**
     * Kuppi-style pageable search with full sorting and pagination support.
     */
    PagedResponse<ItemResponse> searchFoundItems(String keyword, String category, Pageable pageable);

    // =====================================================================
    // Admin Operations
    // =====================================================================

    /**
     * Admin override — update any found item regardless of who reported it.
     */
    ItemResponse adminUpdateFoundItem(Long id, UpdateItemRequest request);

    /**
     * Soft-delete a found item (sets active = false).
     */
    void adminDeleteFoundItem(Long id);
}