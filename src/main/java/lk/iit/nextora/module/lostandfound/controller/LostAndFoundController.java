package lk.iit.nextora.module.lostandfound.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.CategoryResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.service.FoundItemService;
import lk.iit.nextora.module.lostandfound.service.ItemImageService;
import lk.iit.nextora.module.lostandfound.service.ItemMatchingService;
import lk.iit.nextora.module.lostandfound.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiConstants.LOST_AND_FOUND_ITEMS)
@RequiredArgsConstructor
@Tag(name = "Lost & Found Items", description = "Lost and found item management endpoints")
public class LostAndFoundController {

    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final ItemImageService itemImageService;
    private final ItemMatchingService itemMatchingService;
    private final ItemCategoryRepository categoryRepository;

    // =====================================================================
    // CATEGORIES
    // =====================================================================

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve all available item categories")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success("Categories retrieved successfully", categories);
    }

    // =====================================================================
    // LOST ITEMS
    // =====================================================================

    @PostMapping("/lost")
    @Operation(summary = "Report lost item", description = "Report a new lost item with details")
    @PreAuthorize("hasAuthority('LOST_FOUND:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItemResponse> createLostItem(
            @Valid @RequestBody CreateLostItemRequest request) {
        ItemResponse response = lostItemService.createLostItem(request);
        return ApiResponse.success("Lost item reported successfully", response);
    }

    @GetMapping("/lost/{id}")
    @Operation(summary = "Get lost item by ID", description = "Retrieve a specific lost item by its ID")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<ItemResponse> getLostItemById(@PathVariable Long id) {
        ItemResponse response = lostItemService.getLostItemById(id);
        return ApiResponse.success("Lost item retrieved successfully", response);
    }

    @PutMapping("/lost/{id}")
    @Operation(summary = "Update lost item", description = "Update a lost item by ID (owner or admin)")
    @PreAuthorize("hasAuthority('LOST_FOUND:UPDATE')")
    public ApiResponse<ItemResponse> updateLostItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse response = lostItemService.updateLostItem(id, request);
        return ApiResponse.success("Lost item updated successfully", response);
    }

    @DeleteMapping("/lost/{id}")
    @Operation(summary = "Delete lost item", description = "Soft-delete a lost item (admin only)")
    @PreAuthorize("hasAuthority('LOST_FOUND:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteLostItem(@PathVariable Long id) {
        lostItemService.adminDeleteLostItem(id);
        return ApiResponse.success("Lost item deleted successfully");
    }

    @GetMapping("/lost")
    @Operation(summary = "Search lost items", description = "Search lost items with keyword and category filters")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<ItemListResponse> searchLostItems(
            @Valid @ModelAttribute SearchItemRequest request) {
        ItemListResponse response = lostItemService.searchLostItems(request);
        return ApiResponse.success("Lost items retrieved successfully", response);
    }

    @GetMapping("/lost/search")
    @Operation(summary = "Search lost items (pageable)", description = "Search lost items with full pagination and sorting")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<PagedResponse<ItemResponse>> searchLostItemsPageable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ItemResponse> response = lostItemService.searchLostItems(keyword, category, pageable);
        return ApiResponse.success("Lost items search completed successfully", response);
    }

    // =====================================================================
    // LOST ITEM IMAGES
    // =====================================================================

    @PostMapping(value = "/lost/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload lost item image", description = "Upload one or more images for a lost item (max 5 per item, max 5MB each)")
    @PreAuthorize("hasAuthority('LOST_FOUND:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<String>> uploadLostItemImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = itemImageService.uploadImages(id, true, files);
        return ApiResponse.success("Images uploaded successfully", urls);
    }

    @GetMapping("/lost/{id}/images")
    @Operation(summary = "Get lost item images", description = "Retrieve all image URLs for a lost item")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<List<String>> getLostItemImages(@PathVariable Long id) {
        List<String> urls = itemImageService.getImagesForLostItem(id);
        return ApiResponse.success("Images retrieved successfully", urls);
    }

    // =====================================================================
    // LOST ITEM MATCHES
    // =====================================================================

    @GetMapping("/lost/{id}/matches")
    @Operation(summary = "Find matches for lost item", description = "Find potential matching found items based on similarity")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<List<MatchSuggestionResponse>> findMatchesForLostItem(@PathVariable Long id) {
        List<MatchSuggestionResponse> matches = itemMatchingService.findMatchesForLostItem(id);
        return ApiResponse.success("Match suggestions retrieved successfully", matches);
    }

    // =====================================================================
    // FOUND ITEMS
    // =====================================================================

    @PostMapping("/found")
    @Operation(summary = "Report found item", description = "Report a new found item with details")
    @PreAuthorize("hasAuthority('LOST_FOUND:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ItemResponse> createFoundItem(
            @Valid @RequestBody CreateFoundItemRequest request) {
        ItemResponse response = foundItemService.createFoundItem(request);
        return ApiResponse.success("Found item reported successfully", response);
    }

    @GetMapping("/found/{id}")
    @Operation(summary = "Get found item by ID", description = "Retrieve a specific found item by its ID")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<ItemResponse> getFoundItemById(@PathVariable Long id) {
        ItemResponse response = foundItemService.getFoundItemById(id);
        return ApiResponse.success("Found item retrieved successfully", response);
    }

    @PutMapping("/found/{id}")
    @Operation(summary = "Update found item", description = "Update a found item by ID (owner or admin)")
    @PreAuthorize("hasAuthority('LOST_FOUND:UPDATE')")
    public ApiResponse<ItemResponse> updateFoundItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse response = foundItemService.updateFoundItem(id, request);
        return ApiResponse.success("Found item updated successfully", response);
    }

    @DeleteMapping("/found/{id}")
    @Operation(summary = "Delete found item", description = "Soft-delete a found item (admin only)")
    @PreAuthorize("hasAuthority('LOST_FOUND:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteFoundItem(@PathVariable Long id) {
        foundItemService.adminDeleteFoundItem(id);
        return ApiResponse.success("Found item deleted successfully");
    }

    @GetMapping("/found")
    @Operation(summary = "Search found items", description = "Search found items with keyword and category filters")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<ItemListResponse> searchFoundItems(
            @Valid @ModelAttribute SearchItemRequest request) {
        ItemListResponse response = foundItemService.searchFoundItems(request);
        return ApiResponse.success("Found items retrieved successfully", response);
    }

    @GetMapping("/found/search")
    @Operation(summary = "Search found items (pageable)", description = "Search found items with full pagination and sorting")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
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

    // =====================================================================
    // FOUND ITEM IMAGES
    // =====================================================================

    @PostMapping(value = "/found/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload found item image", description = "Upload one or more images for a found item (max 5 per item, max 5MB each)")
    @PreAuthorize("hasAuthority('LOST_FOUND:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<String>> uploadFoundItemImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = itemImageService.uploadImages(id, false, files);
        return ApiResponse.success("Images uploaded successfully", urls);
    }

    @GetMapping("/found/{id}/images")
    @Operation(summary = "Get found item images", description = "Retrieve all image URLs for a found item")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<List<String>> getFoundItemImages(@PathVariable Long id) {
        List<String> urls = itemImageService.getImagesForFoundItem(id);
        return ApiResponse.success("Images retrieved successfully", urls);
    }

    // =====================================================================
    // FOUND ITEM MATCHES
    // =====================================================================

    @GetMapping("/found/{id}/matches")
    @Operation(summary = "Find matches for found item", description = "Find potential matching lost items based on similarity")
    @PreAuthorize("hasAuthority('LOST_FOUND:READ')")
    public ApiResponse<List<MatchSuggestionResponse>> findMatchesForFoundItem(@PathVariable Long id) {
        List<MatchSuggestionResponse> matches = itemMatchingService.findMatchesForFoundItem(id);
        return ApiResponse.success("Match suggestions retrieved successfully", matches);
    }

    // =====================================================================
    // IMAGE MANAGEMENT
    // =====================================================================

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Delete image", description = "Delete a specific image by its ID")
    @PreAuthorize("hasAuthority('LOST_FOUND:UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteImage(@PathVariable Long imageId) {
        itemImageService.deleteImage(imageId);
        return ApiResponse.success("Image deleted successfully");
    }
}
