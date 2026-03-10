package lk.iit.nextora.module.lostandfound.service.impl;

// ── Common project DTOs and exceptions ─────────────────────────────────────
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Entity ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.entity.LostItem;

// ── Mapper ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.mapper.LostItemMapper;

// ── Repositories ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;

// ── Service interface ───────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.LostItemService;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ── Spring ──────────────────────────────────────────────────────────────────
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of LostItemService.
 * Follows the same structural pattern as KuppiNoteServiceImpl:
 *   - Class-level @Transactional(readOnly = true)
 *   - Individual write methods override with @Transactional
 *   - @Slf4j for structured logging
 *   - ResourceNotFoundException with meaningful messages (no bare orElseThrow())
 *   - MapStruct mapper instead of manual field assignment
 *   - No findAll() + Java stream filter — all filtering done in DB
 */
@Slf4j
@Service
@RequiredArgsConstructor
// ✅ FIX: was missing @Transactional entirely — all reads now use readOnly=true for performance
@Transactional(readOnly = true)
public class LostItemServiceImpl implements LostItemService {

    // Repository for LostItem CRUD and search queries
    private final LostItemRepository lostItemRepository;

    // Repository to resolve category names to ItemCategory entities
    private final ItemCategoryRepository categoryRepository;

    // ✅ FIX: replaced static utility mapper with MapStruct interface mapper (Spring-injected)
    private final LostItemMapper lostItemMapper;

    // =====================================================================
    // Student Operations
    // =====================================================================

    @Override
    @Transactional  // write operation — override the class-level readOnly
    public ItemResponse createLostItem(CreateLostItemRequest request) {
        // ✅ FIX: replaced categoryRepository.findAll() + Java stream filter
        //         with a single targeted DB query — O(1) instead of O(n)
        ItemCategory category = findCategoryByName(request.getCategory());

        // ✅ FIX: replaced manual new + setX() calls with MapStruct toEntity()
        LostItem item = lostItemMapper.toEntity(request);
        // Set the resolved category entity (mapper ignores this field by design)
        item.setCategory(category);

        // Persist and map to response in one step
        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item created with id: {}", saved.getId());

        return lostItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemResponse updateLostItem(Long id, UpdateItemRequest request) {
        // ✅ FIX: replaced bare orElseThrow() with meaningful ResourceNotFoundException
        LostItem item = findLostItemById(id);

        // ✅ FIX: replaced manual null checks + setters with MapStruct partial update
        //         (NullValuePropertyMappingStrategy.IGNORE skips null fields automatically)
        lostItemMapper.updateLostItemFromRequest(request, item);

        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item {} updated", id);

        return lostItemMapper.toResponse(saved);
    }

    @Override
    public ItemResponse getLostItemById(Long id) {
        // Fetch with category pre-loaded to avoid a secondary SELECT in the mapper
        LostItem item = lostItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));
        return lostItemMapper.toResponse(item);
    }

    @Override
    public ItemListResponse searchLostItems(SearchItemRequest request) {
        // Build a Pageable from the request's embedded page/size fields
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<LostItem> page;

        // ✅ FIX: original ignored keyword and category entirely — this now applies them
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            // Resolve category name to entity — returns empty page if category doesn't exist
            ItemCategory category = categoryRepository
                    .findByNameIgnoreCase(request.getCategory())
                    .orElse(null);

            if (category != null) {
                // Filter by category in the DB — not in Java memory
                page = lostItemRepository.findByCategoryIdAndActiveTrue(category.getId(), pageable);
            } else {
                // Unknown category name — return empty result rather than throwing
                return buildEmptyItemListResponse();
            }

        } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            // Keyword search: try title first, fall back to location if no results
            page = lostItemRepository.searchByTitle(request.getKeyword(), pageable);
            if (page.isEmpty()) {
                page = lostItemRepository.searchByLocation(request.getKeyword(), pageable);
            }

        } else {
            // No filters — return all active items
            page = lostItemRepository.findByActiveTrue(pageable);
        }

        // Map to response, build the ItemListResponse wrapper with pagination metadata
        List<ItemResponse> items = lostItemMapper.toResponseList(page.getContent());
        return ItemListResponse.builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    public PagedResponse<ItemResponse> searchLostItems(String keyword, String category, Pageable pageable) {
        Page<LostItem> page;

        if (category != null && !category.isBlank()) {
            // ✅ FIX: replaced findAll().stream().filter() with single targeted DB query
            ItemCategory cat = categoryRepository
                    .findByNameIgnoreCase(category)
                    .orElse(null);

            if (cat != null) {
                page = lostItemRepository.findByCategoryIdAndActiveTrue(cat.getId(), pageable);
                return PagedResponse.of(page.map(lostItemMapper::toResponse));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            // Title search first; location search as fallback
            page = lostItemRepository.searchByTitle(keyword, pageable);
            if (page.isEmpty()) {
                page = lostItemRepository.searchByLocation(keyword, pageable);
            }
            return PagedResponse.of(page.map(lostItemMapper::toResponse));
        }

        // No filters — return all active lost items
        page = lostItemRepository.findByActiveTrue(pageable);
        return PagedResponse.of(page.map(lostItemMapper::toResponse));
    }

    // =====================================================================
    // Admin Operations
    // =====================================================================

    @Override
    @Transactional
    public ItemResponse adminUpdateLostItem(Long id, UpdateItemRequest request) {
        // Admin can update any item — no ownership check needed
        LostItem item = findLostItemById(id);
        lostItemMapper.updateLostItemFromRequest(request, item);
        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item {} updated by admin", id);
        return lostItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void adminDeleteLostItem(Long id) {
        LostItem item = findLostItemById(id);
        // Soft delete — set active = false instead of physically removing the row
        item.setActive(false);
        lostItemRepository.save(item);
        log.info("Lost item {} soft-deleted by admin", id);
    }

    // =====================================================================
    // Private Helper Methods
    // =====================================================================

    /**
     * Find a LostItem by ID or throw a descriptive ResourceNotFoundException.
     * ✅ FIX: replaces all bare .orElseThrow() calls throughout the service.
     */
    private LostItem findLostItemById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));
    }

    /**
     * Resolve a category name to an ItemCategory entity with a single DB query.
     * ✅ FIX: replaces categoryRepository.findAll().stream().filter() anti-pattern.
     */
    private ItemCategory findCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCategory", "name", name));
    }

    /**
     * Build an empty ItemListResponse with zeroed pagination metadata.
     * Returned when a search filter produces no valid category to query against.
     */
    private ItemListResponse buildEmptyItemListResponse() {
        return ItemListResponse.builder()
                .items(List.of())
                .totalElements(0L)
                .totalPages(0)
                .pageNumber(0)
                .pageSize(0)
                .empty(true)
                .build();
    }
}