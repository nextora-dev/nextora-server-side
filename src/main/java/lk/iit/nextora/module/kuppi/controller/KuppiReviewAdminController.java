package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiReviewResponse;
import lk.iit.nextora.module.kuppi.service.KuppiReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for Kuppi Review management.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.KUPPI_ADMIN_REVIEWS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Reviews Admin", description = "Admin endpoints for review management")
public class KuppiReviewAdminController {

    private final KuppiReviewService reviewService;

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Admin: Get all reviews with pagination")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN')")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all reviews - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getAllReviews(pageable));
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_REVIEW_BY_ID)
    @Operation(summary = "Get review by ID", description = "Admin: Get review details")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN')")
    public ApiResponse<KuppiReviewResponse> getReviewById(@PathVariable Long reviewId) {
        log.info("Admin fetching review: {}", reviewId);
        return ApiResponse.success("Review retrieved", reviewService.getReviewById(reviewId));
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_REVIEW_BY_ID)
    @Operation(summary = "Delete review", description = "Admin: Soft delete a review")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN')")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        log.info("Admin deleting review: {}", reviewId);
        reviewService.adminDeleteReview(reviewId);
        return ApiResponse.success("Review deleted successfully");
    }
}

