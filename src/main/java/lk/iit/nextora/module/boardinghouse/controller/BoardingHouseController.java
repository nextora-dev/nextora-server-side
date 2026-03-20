package lk.iit.nextora.module.boardinghouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.GenderPreference;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseImageResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.BOARDINGHOUSE_PUBLIC)
@RequiredArgsConstructor
@Tag(name = "Boarding Houses", description = "Boarding house listing endpoints")
public class BoardingHouseController {

    private final BoardingHouseService boardingHouseService;

    // ==================== Browse Endpoints (All authenticated users with BOARDING_HOUSE:READ) ====================

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES)
    @Operation(summary = "Get all available listings", description = "Browse all available boarding houses (any authenticated user)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> getAllAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success("Boarding houses retrieved successfully",
                boardingHouseService.getAllAvailable(pageable));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @Operation(summary = "Get listing by ID", description = "View a specific boarding house listing")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<BoardingHouseResponse> getById(@PathVariable Long houseId) {
        return ApiResponse.success("Boarding house retrieved successfully",
                boardingHouseService.getById(houseId));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/search")
    @Operation(summary = "Search listings", description = "Search boarding houses by keyword")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully",
                boardingHouseService.search(keyword, pageable));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/filter")
    @Operation(summary = "Filter listings", description = "Filter boarding houses by city, district, gender, price")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> filter(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) GenderPreference genderPreference,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        BoardingHouseFilterRequest filterRequest = BoardingHouseFilterRequest.builder()
                .city(city)
                .district(district)
                .genderPreference(genderPreference)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return ApiResponse.success("Filtered successfully",
                boardingHouseService.filter(filterRequest));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/city")
    @Operation(summary = "Filter by city", description = "Get boarding houses in a specific city")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> filterByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("City filter applied successfully",
                boardingHouseService.filterByCity(city, pageable));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/district")
    @Operation(summary = "Filter by district", description = "Get boarding houses in a specific district")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> filterByDistrict(
            @RequestParam String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("District filter applied successfully",
                boardingHouseService.filterByDistrict(district, pageable));
    }

    // ==================== CRUD Endpoints (Admin, Non-Academic Staff, Super Admin) ====================

    @PostMapping(ApiConstants.BOARDINGHOUSE_HOUSES)
    @Operation(summary = "Create listing", description = "Create a new boarding house listing (Admin, Non-Academic Staff)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BoardingHouseResponse> create(
            @Valid @RequestBody CreateBoardingHouseRequest request) {
        return ApiResponse.success("Boarding house created successfully",
                boardingHouseService.create(request));
    }

    @PutMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @Operation(summary = "Update listing", description = "Update a boarding house listing (owner or admin)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:UPDATE')")
    public ApiResponse<BoardingHouseResponse> update(
            @PathVariable Long houseId,
            @Valid @RequestBody UpdateBoardingHouseRequest request) {
        return ApiResponse.success("Boarding house updated successfully",
                boardingHouseService.update(houseId, request));
    }

    @DeleteMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @Operation(summary = "Delete listing", description = "Soft delete a boarding house listing (owner or admin)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long houseId) {
        boardingHouseService.delete(houseId);
        return ApiResponse.success("Boarding house deleted successfully");
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/my")
    @Operation(summary = "Get my listings", description = "Get own boarding house listings")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:CREATE')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> getMyListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Your listings retrieved successfully",
                boardingHouseService.getMyListings(pageable));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES + "/all")
    @Operation(summary = "Get all listings (Admin)", description = "Get all listings including unavailable (Admin only)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:ADMIN_READ')")
    public ApiResponse<PagedResponse<BoardingHouseResponse>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("All listings retrieved successfully",
                boardingHouseService.getAllForAdmin(pageable));
    }

    // ==================== Image Endpoints (S3) ====================

    @PostMapping(value = ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID + "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload images", description = "Upload images for a boarding house listing (max 10 images, 5MB each)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<BoardingHouseImageResponse>> uploadImages(
            @PathVariable Long houseId,
            @RequestParam("files") List<MultipartFile> files) {
        return ApiResponse.success("Images uploaded successfully",
                boardingHouseService.uploadImages(houseId, files));
    }

    @DeleteMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID + "/images/{imageId}")
    @Operation(summary = "Delete image", description = "Delete an image from a boarding house listing")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteImage(
            @PathVariable Long houseId,
            @PathVariable Long imageId) {
        boardingHouseService.deleteImage(houseId, imageId);
        return ApiResponse.success("Image deleted successfully");
    }

    @PutMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID + "/images/{imageId}/primary")
    @Operation(summary = "Set primary image", description = "Set an image as the primary/cover image")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:UPDATE')")
    public ApiResponse<BoardingHouseImageResponse> setPrimaryImage(
            @PathVariable Long houseId,
            @PathVariable Long imageId) {
        return ApiResponse.success("Primary image updated",
                boardingHouseService.setPrimaryImage(houseId, imageId));
    }

    @GetMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID + "/images")
    @Operation(summary = "Get images", description = "Get all images for a boarding house listing")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:READ')")
    public ApiResponse<List<BoardingHouseImageResponse>> getImages(@PathVariable Long houseId) {
        return ApiResponse.success("Images retrieved successfully",
                boardingHouseService.getImages(houseId));
    }
}
