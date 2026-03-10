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

// ── Lost & Found request DTOs ───────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Lost & Found response DTOs ──────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Lost & Found services ───────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.FoundItemService;
import lk.iit.nextora.module.lostandfound.service.LostItemService;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;

// ── Spring Data pagination ──────────────────────────────────────────────────
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// ── Spring Web ──────────────────────────────────────────────────────────────
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Lost & Found item operations.
 * Handles all CRUD and search endpoints for both lost and found items.
 * Base URL: /api/v1/lost-and-found/items
 *
 * Two search styles are supported per item type:
 *   1. Simple search  – GET /lost        (uses SearchItemRequest @ModelAttribute)
 *   2. Pageable search – GET /lost/search (uses individual @RequestParam + Pageable)
 */
@RestController
// ✅ FIX: was "/api/v1/kuppi/items" — Lost & Found is its own module, not under Kuppi
// ✅ FIX: was "/api/v1/kuppi/items" — Lost & Found is its own module, not under the Kuppi module
@RequestMapping(ApiConstants.LOST_AND_FOUND_ITEMS)
@RequiredArgsConstructor
// ✅ FIX: added description to match the Kuppi module's pattern
@Tag(name = "Lost & Found Items", description = "Lost and found item management endpoints")
public class LostAndFoundController {

    // Service for all lost-item business logic
    private final LostItemService lostItemService;

    // Service for all found-item business logic
    private final FoundItemService foundItemService;

    // =====================================================================
    // LOST ITEMS
    // =====================================================================

    /**
     * POST /lost
     * Report a new lost item. Any authenticated student can report a lost item.
     */
    @PostMapping("/lost")
    // ✅ FIX: added @Operation — every endpoint must document itself like Kuppi
    @Operation(summary = "Report lost item", description = "Report a new lost item")
    // ✅ FIX: added matching authority guard like the Kuppi module uses on every endpoint
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:CREATE')")
    // ✅ FIX: added @ResponseStatus — POST must return 201 Created, not default 200
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItemResponse> createLostItem(
            @Valid @RequestBody CreateLostItemRequest request) {
        // Delegate to service; wrap result in standard ApiResponse envelope
        ItemResponse response = lostItemService.createLostItem(request);
        return ApiResponse.success("Lost item reported successfully", response);
    }

    /**
     * PUT /lost/{id}
     * Update an existing lost item by its ID (owner or admin).
     */
    @PutMapping("/lost/{id}")
    @Operation(summary = "Update lost item", description = "Update a lost item by ID")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:UPDATE')")
    public ApiResponse<ItemResponse> updateLostItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse response = lostItemService.updateLostItem(id, request);
        return ApiResponse.success("Lost item updated successfully", response);
    }

    /**
     * GET /lost
     * Simple search using the SearchItemRequest model attribute (backward-compatible).
     * Supports keyword and category filters; pagination handled inside the request object.
     */
    @GetMapping("/lost")
    @Operation(summary = "Search lost items", description = "Search lost items with keyword and category filters")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<ItemListResponse> searchLostItems(
            // @ModelAttribute binds query params directly to the SearchItemRequest object
            @Valid @ModelAttribute SearchItemRequest request) {
        ItemListResponse response = lostItemService.searchLostItems(request);
        return ApiResponse.success("Lost items retrieved successfully", response);
    }

    /**
     * GET /lost/search
     * Kuppi-style pageable search — individual @RequestParam with Sort and Pageable.
     * Returns a PagedResponse wrapper identical to the Kuppi module session/note searches.
     */
    @GetMapping("/lost/search")
    @Operation(summary = "Search lost items (pageable)", description = "Search lost items with full pagination and sorting support")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<PagedResponse<ItemResponse>> searchLostItemsPageable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // ✅ FIX: default sortBy changed to "createdAt" — matches entity field name
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        // Build a Sort + Pageable from the individual request params
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ItemResponse> response = lostItemService.searchLostItems(keyword, category, pageable);
        return ApiResponse.success("Lost items search completed successfully", response);
    }

    // =====================================================================
    // FOUND ITEMS
    // =====================================================================

    /**
     * POST /found
     * Report a new found item. Any authenticated student can report a found item.
     */
    @PostMapping("/found")
    @Operation(summary = "Report found item", description = "Report a new found item")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:CREATE')")
    // ✅ FIX: added @ResponseStatus — POST must return 201 Created
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItemResponse> createFoundItem(
            @Valid @RequestBody CreateFoundItemRequest request) {
        ItemResponse response = foundItemService.createFoundItem(request);
        return ApiResponse.success("Found item reported successfully", response);
    }

    /**
     * PUT /found/{id}
     * Update an existing found item by its ID (owner or admin).
     */
    @PutMapping("/found/{id}")
    @Operation(summary = "Update found item", description = "Update a found item by ID")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:UPDATE')")
    public ApiResponse<ItemResponse> updateFoundItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse response = foundItemService.updateFoundItem(id, request);
        return ApiResponse.success("Found item updated successfully", response);
    }

    /**
     * GET /found
     * Simple search — backward-compatible, uses SearchItemRequest @ModelAttribute.
     */
    @GetMapping("/found")
    @Operation(summary = "Search found items", description = "Search found items with keyword and category filters")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<ItemListResponse> searchFoundItems(
            @Valid @ModelAttribute SearchItemRequest request) {
        ItemListResponse response = foundItemService.searchFoundItems(request);
        return ApiResponse.success("Found items retrieved successfully", response);
    }

    /**
     * GET /found/search
     * Kuppi-style pageable search for found items.
     */
    @GetMapping("/found/search")
    @Operation(summary = "Search found items (pageable)", description = "Search found items with full pagination and sorting support")
    @PreAuthorize("hasAuthority('LOST_AND_FOUND:READ')")
    public ApiResponse<PagedResponse<ItemResponse>> searchFoundItemsPageable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ItemResponse> response = foundItemService.searchFoundItems(keyword, category, pageable);
        return ApiResponse.success("Found items search completed successfully", response);
    }
}