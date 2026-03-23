package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.entity.ItemImage;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.mapper.LostItemMapper;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemImageRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.LostItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemImageRepository imageRepository;
    private final LostItemMapper lostItemMapper;
    private final SecurityService securityService;

    @Override
    @Transactional
    public ItemResponse createLostItem(CreateLostItemRequest request) {
        ItemCategory category = findCategoryByName(request.getCategory());

        BaseUser currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("User must be authenticated to report a lost item"));

        LostItem item = lostItemMapper.toEntity(request);
        item.setCategory(category);
        item.setReportedBy(currentUser.getId());
        item.setReporterName(currentUser.getFirstName() + " " + currentUser.getLastName());

        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item created with id: {} by user: {}", saved.getId(), currentUser.getId());

        return enrichWithImages(lostItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    @Transactional
    public ItemResponse updateLostItem(Long id, UpdateItemRequest request) {
        LostItem item = findLostItemById(id);

        Long currentUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin() && !currentUserId.equals(item.getReportedBy())) {
            throw new BadRequestException("You can only update items you reported");
        }

        lostItemMapper.updateLostItemFromRequest(request, item);
        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item {} updated by user {}", id, currentUserId);

        return enrichWithImages(lostItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    public ItemResponse getLostItemById(Long id) {
        LostItem item = lostItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));
        return enrichWithImages(lostItemMapper.toResponse(item), item.getId());
    }

    @Override
    public ItemListResponse searchLostItems(SearchItemRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<LostItem> page;

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            ItemCategory category = categoryRepository
                    .findByNameIgnoreCase(request.getCategory())
                    .orElse(null);
            if (category != null) {
                page = lostItemRepository.findByCategoryIdAndActiveTrue(category.getId(), pageable);
            } else {
                return buildEmptyItemListResponse();
            }
        } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            page = lostItemRepository.searchByTitle(request.getKeyword(), pageable);
            if (page.isEmpty()) {
                page = lostItemRepository.searchByLocation(request.getKeyword(), pageable);
            }
        } else {
            page = lostItemRepository.findByActiveTrue(pageable);
        }

        List<ItemResponse> items = page.getContent().stream()
                .map(item -> enrichWithImages(lostItemMapper.toResponse(item), item.getId()))
                .collect(Collectors.toList());

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
            ItemCategory cat = categoryRepository.findByNameIgnoreCase(category).orElse(null);
            if (cat != null) {
                page = lostItemRepository.findByCategoryIdAndActiveTrue(cat.getId(), pageable);
                return PagedResponse.of(page.map(item -> enrichWithImages(lostItemMapper.toResponse(item), item.getId())));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            page = lostItemRepository.searchByTitle(keyword, pageable);
            if (page.isEmpty()) {
                page = lostItemRepository.searchByLocation(keyword, pageable);
            }
            return PagedResponse.of(page.map(item -> enrichWithImages(lostItemMapper.toResponse(item), item.getId())));
        }

        page = lostItemRepository.findByActiveTrue(pageable);
        return PagedResponse.of(page.map(item -> enrichWithImages(lostItemMapper.toResponse(item), item.getId())));
    }

    @Override
    @Transactional
    public ItemResponse adminUpdateLostItem(Long id, UpdateItemRequest request) {
        LostItem item = findLostItemById(id);
        lostItemMapper.updateLostItemFromRequest(request, item);
        LostItem saved = lostItemRepository.save(item);
        log.info("Lost item {} updated by admin", id);
        return enrichWithImages(lostItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    @Transactional
    public void adminDeleteLostItem(Long id) {
        LostItem item = findLostItemById(id);
        item.setActive(false);
        lostItemRepository.save(item);
        log.info("Lost item {} soft-deleted by admin", id);
    }

    private LostItem findLostItemById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));
    }

    private ItemCategory findCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCategory", "name", name));
    }

    private ItemResponse enrichWithImages(ItemResponse response, Long itemId) {
        List<String> imageUrls = imageRepository.findByLostItemIdOrderByDisplayOrderAsc(itemId)
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());
        return ItemResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .categoryId(response.getCategoryId())
                .category(response.getCategory())
                .location(response.getLocation())
                .contactNumber(response.getContactNumber())
                .reportedBy(response.getReportedBy())
                .reporterName(response.getReporterName())
                .active(response.isActive())
                .imageUrls(imageUrls)
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }

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
