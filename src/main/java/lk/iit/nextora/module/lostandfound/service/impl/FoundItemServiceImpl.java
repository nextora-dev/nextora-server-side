package lk.iit.nextora.module.lostandfound.service.impl;

// ── Common project DTOs and exceptions ─────────────────────────────────────
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Entity ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;

// ── Mapper ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.mapper.FoundItemMapper;

// ── Repositories ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;

// ── Service interface ───────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.FoundItemService;

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
 * Implementation of FoundItemService.
 * Identical structural pattern to LostItemServiceImpl — symmetric by design.
 * All the same fixes applied:
 *   - @Transactional(readOnly = true) at class level
 *   - @Slf4j structured logging
 *   - ResourceNotFoundException with messages
 *   - MapStruct mapper instead of manual field assignment
 *   - findByNameIgnoreCase() instead of findAll() + Java stream
 */
@Slf4j
@Service
@RequiredArgsConstructor
// ✅ FIX: @Transactional was missing — all reads now use readOnly=true
@Transactional(readOnly = true)
public class FoundItemServiceImpl implements FoundItemService {

    // Repository for FoundItem CRUD and search queries
    private final FoundItemRepository foundItemRepository;

    // Repository to resolve category names to ItemCategory entities
    private final ItemCategoryRepository categoryRepository;

    // ✅ FIX: replaced static utility mapper with Spring-injected MapStruct mapper
    private final FoundItemMapper foundItemMapper;

    // =====================================================================
    // Student Operations
    // =====================================================================

    @Override
    @Transactional  // write — overrides class-level readOnly
    public ItemResponse createFoundItem(CreateFoundItemRequest request) {
        // ✅ FIX: single targeted query instead of findAll() + Java stream filter
        ItemCategory category = findCategoryByName(request.getCategory());

        // ✅ FIX: MapStruct toEntity() instead of manual new FoundItem() + setX() calls
        FoundItem item = foundItemMapper.toEntity(request);
        // Set the category entity (ignored in mapper, set here after resolution)
        item.setCategory(category);

        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item created with id: {}", saved.getId());

        return foundItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemResponse updateFoundItem(Long id, UpdateItemRequest request) {
        // ✅ FIX: descriptive exception message instead of bare orElseThrow()
        FoundItem item = findFoundItemById(id);

        // ✅ FIX: MapStruct partial update instead of manual null checks + setters
        foundItemMapper.updateFoundItemFromRequest(request, item);

        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item {} updated", id);

        return foundItemMapper.toResponse(saved);
    }

    @Override
    public ItemResponse getFoundItemById(Long id) {
        // Use the JOIN FETCH query to avoid N+1 when mapper accesses category.name
        FoundItem item = foundItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));
        return foundItemMapper.toResponse(item);
    }

    @Override
    public ItemListResponse searchFoundItems(SearchItemRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<FoundItem> page;

        // ✅ FIX: original ignored keyword and category — this now correctly applies them
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            ItemCategory category = categoryRepository
                    .findByNameIgnoreCase(request.getCategory())
                    .orElse(null);

            if (category != null) {
                page = foundItemRepository.findByCategoryIdAndActiveTrue(category.getId(), pageable);
            } else {
                // Unknown category — return empty result
                return buildEmptyItemListResponse();
            }

        } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            // Keyword search on title only — FoundItem has no location fallback search
            page = foundItemRepository.searchByTitle(request.getKeyword(), pageable);

        } else {
            // No filters — return all active found items
            page = foundItemRepository.findByActiveTrue(pageable);
        }

        // Map results and build the response wrapper
        List<ItemResponse> items = foundItemMapper.toResponseList(page.getContent());
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
    public PagedResponse<ItemResponse> searchFoundItems(String keyword, String category, Pageable pageable) {
        Page<FoundItem> page;

        if (category != null && !category.isBlank()) {
            // ✅ FIX: single DB query instead of findAll().stream().filter()
            ItemCategory cat = categoryRepository
                    .findByNameIgnoreCase(category)
                    .orElse(null);

            if (cat != null) {
                page = foundItemRepository.findByCategoryIdAndActiveTrue(cat.getId(), pageable);
                return PagedResponse.of(page.map(foundItemMapper::toResponse));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            page = foundItemRepository.searchByTitle(keyword, pageable);
            return PagedResponse.of(page.map(foundItemMapper::toResponse));
        }

        // No filters — return all active found items
        page = foundItemRepository.findByActiveTrue(pageable);
        return PagedResponse.of(page.map(foundItemMapper::toResponse));
    }

    // =====================================================================
    // Admin Operations
    // =====================================================================

    @Override
    @Transactional
    public ItemResponse adminUpdateFoundItem(Long id, UpdateItemRequest request) {
        // Admin bypasses ownership check — can update any found item
        FoundItem item = findFoundItemById(id);
        foundItemMapper.updateFoundItemFromRequest(request, item);
        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item {} updated by admin", id);
        return foundItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void adminDeleteFoundItem(Long id) {
        FoundItem item = findFoundItemById(id);
        // Soft delete — preserve the row, just deactivate it
        item.setActive(false);
        foundItemRepository.save(item);
        log.info("Found item {} soft-deleted by admin", id);
    }

    // =====================================================================
    // Private Helper Methods
    // =====================================================================

    /**
     * Fetch a FoundItem by ID or throw a descriptive ResourceNotFoundException.
     * ✅ FIX: replaces all bare .orElseThrow() calls in the class.
     */
    private FoundItem findFoundItemById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));
    }

    /**
     * Resolve a category name to an entity using a single indexed DB query.
     * ✅ FIX: replaces findAll().stream().filter() performance anti-pattern.
     */
    private ItemCategory findCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCategory", "name", name));
    }

    /**
     * Build a zeroed-out ItemListResponse for when a search yields no valid filter.
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