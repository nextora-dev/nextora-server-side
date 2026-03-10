package lk.iit.nextora.module.lostandfound.service;

// ── Common project DTOs ─────────────────────────────────────────────────────
import lk.iit.nextora.common.dto.PagedResponse;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Spring Data ─────────────────────────────────────────────────────────────
import org.springframework.data.domain.Pageable;

/**
 * Service interface for lost item operations in the Lost & Found module.
 * Mirrors the structure of KuppiNoteService — one method per use case,
 * each clearly documented with who can call it.
 */
public interface LostItemService {

    // =====================================================================
    // Student Operations
    // =====================================================================

    /**
     * Report a new lost item.
     * Any authenticated student can report a lost item.
     */
    ItemResponse createLostItem(CreateLostItemRequest request);

    /**
     * Update an existing lost item by ID.
     * Only the original reporter (or an admin) may update the item.
     */
    ItemResponse updateLostItem(Long id, UpdateItemRequest request);

    /**
     * Retrieve a single lost item by ID (increments view count).
     */
    ItemResponse getLostItemById(Long id);

    /**
     * Simple keyword + category search — backward-compatible endpoint.
     * Pagination is embedded inside SearchItemRequest.
     */
    ItemListResponse searchLostItems(SearchItemRequest request);

    /**
     * Kuppi-style pageable search.
     * Supports keyword, category, and full Spring Data Pageable (sort, page, size).
     */
    PagedResponse<ItemResponse> searchLostItems(String keyword, String category, Pageable pageable);

    // =====================================================================
    // Admin Operations
    // =====================================================================

    /**
     * Admin override — update any lost item regardless of who reported it.
     */
    ItemResponse adminUpdateLostItem(Long id, UpdateItemRequest request);

    /**
     * Soft-delete a lost item (sets active = false).
     */
    void adminDeleteLostItem(Long id);
}