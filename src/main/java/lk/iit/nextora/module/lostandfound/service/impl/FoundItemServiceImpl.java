package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.entity.ItemImage;
import lk.iit.nextora.module.lostandfound.mapper.FoundItemMapper;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemImageRepository;
import lk.iit.nextora.module.lostandfound.service.FoundItemService;
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
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemImageRepository imageRepository;
    private final FoundItemMapper foundItemMapper;
    private final SecurityService securityService;

    @Override
    @Transactional
    public ItemResponse createFoundItem(CreateFoundItemRequest request) {
        ItemCategory category = findCategoryByName(request.getCategory());

        BaseUser currentUser = securityService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("User must be authenticated to report a found item"));

        FoundItem item = foundItemMapper.toEntity(request);
        item.setCategory(category);
        item.setReportedBy(currentUser.getId());
        item.setReporterName(currentUser.getFirstName() + " " + currentUser.getLastName());

        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item created with id: {} by user: {}", saved.getId(), currentUser.getId());

        return enrichWithImages(foundItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    @Transactional
    public ItemResponse updateFoundItem(Long id, UpdateItemRequest request) {
        FoundItem item = findFoundItemById(id);

        Long currentUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin() && !currentUserId.equals(item.getReportedBy())) {
            throw new BadRequestException("You can only update items you reported");
        }

        foundItemMapper.updateFoundItemFromRequest(request, item);
        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item {} updated by user {}", id, currentUserId);

        return enrichWithImages(foundItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    public ItemResponse getFoundItemById(Long id) {
        FoundItem item = foundItemRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));
        return enrichWithImages(foundItemMapper.toResponse(item), item.getId());
    }

    @Override
    public ItemListResponse searchFoundItems(SearchItemRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<FoundItem> page;

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            ItemCategory category = categoryRepository
                    .findByNameIgnoreCase(request.getCategory())
                    .orElse(null);
            if (category != null) {
                page = foundItemRepository.findByCategoryIdAndActiveTrue(category.getId(), pageable);
            } else {
                return buildEmptyItemListResponse();
            }
        } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            page = foundItemRepository.searchByTitle(request.getKeyword(), pageable);
        } else {
            page = foundItemRepository.findByActiveTrue(pageable);
        }

        List<ItemResponse> items = page.getContent().stream()
                .map(item -> enrichWithImages(foundItemMapper.toResponse(item), item.getId()))
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
    public PagedResponse<ItemResponse> searchFoundItems(String keyword, String category, Pageable pageable) {
        Page<FoundItem> page;

        if (category != null && !category.isBlank()) {
            ItemCategory cat = categoryRepository.findByNameIgnoreCase(category).orElse(null);
            if (cat != null) {
                page = foundItemRepository.findByCategoryIdAndActiveTrue(cat.getId(), pageable);
                return PagedResponse.of(page.map(item -> enrichWithImages(foundItemMapper.toResponse(item), item.getId())));
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            page = foundItemRepository.searchByTitle(keyword, pageable);
            return PagedResponse.of(page.map(item -> enrichWithImages(foundItemMapper.toResponse(item), item.getId())));
        }

        page = foundItemRepository.findByActiveTrue(pageable);
        return PagedResponse.of(page.map(item -> enrichWithImages(foundItemMapper.toResponse(item), item.getId())));
    }

    @Override
    @Transactional
    public ItemResponse adminUpdateFoundItem(Long id, UpdateItemRequest request) {
        FoundItem item = findFoundItemById(id);
        foundItemMapper.updateFoundItemFromRequest(request, item);
        FoundItem saved = foundItemRepository.save(item);
        log.info("Found item {} updated by admin", id);
        return enrichWithImages(foundItemMapper.toResponse(saved), saved.getId());
    }

    @Override
    @Transactional
    public void adminDeleteFoundItem(Long id) {
        FoundItem item = findFoundItemById(id);
        item.setActive(false);
        foundItemRepository.save(item);
        log.info("Found item {} soft-deleted by admin", id);
    }

    private FoundItem findFoundItemById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));
    }

    private ItemCategory findCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCategory", "name", name));
    }

    private ItemResponse enrichWithImages(ItemResponse response, Long itemId) {
        List<String> imageUrls = imageRepository.findByFoundItemIdOrderByDisplayOrderAsc(itemId)
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
