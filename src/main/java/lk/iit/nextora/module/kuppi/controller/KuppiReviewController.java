package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.request.TutorReviewResponseRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiReviewResponse;
import lk.iit.nextora.module.kuppi.service.KuppiReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Kuppi Review operations.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.KUPPI_REVIEWS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Reviews", description = "Session review and rating endpoints")
public class KuppiReviewController {

    private final KuppiReviewService reviewService;

    // ==================== Student CRUD ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create review", description = "Create a review with 1-5 star rating for a completed session")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiReviewResponse> createReview(@Valid @RequestBody CreateKuppiReviewRequest request) {
        log.info("Creating review for session: {}", request.getSessionId());
        return ApiResponse.success("Review created successfully", reviewService.createReview(request));
    }

    @PutMapping(ApiConstants.KUPPI_REVIEW_BY_ID)
    @Operation(summary = "Update review", description = "Update your own review")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateKuppiReviewRequest request) {
        log.info("Updating review: {}", reviewId);
        return ApiResponse.success("Review updated successfully", reviewService.updateReview(reviewId, request));
    }

    @DeleteMapping(ApiConstants.KUPPI_REVIEW_BY_ID)
    @Operation(summary = "Delete review", description = "Delete your own review")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        log.info("Deleting review: {}", reviewId);
        reviewService.deleteReview(reviewId);
        return ApiResponse.success("Review deleted successfully");
    }

    @GetMapping(ApiConstants.KUPPI_REVIEW_BY_ID)
    @Operation(summary = "Get review by ID")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiReviewResponse> getReviewById(@PathVariable Long reviewId) {
        return ApiResponse.success("Review retrieved", reviewService.getReviewById(reviewId));
    }

    @GetMapping(ApiConstants.KUPPI_MY)
    @Operation(summary = "Get my reviews", description = "Get all reviews written by current user")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getMyReviews(pageable));
    }

    // ==================== Session Reviews ====================

    @GetMapping(ApiConstants.KUPPI_REVIEW_SESSION)
    @Operation(summary = "Get session reviews", description = "Get all reviews for a session")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getReviewsBySession(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getReviewsBySession(sessionId, pageable));
    }

    // ==================== Tutor Reviews ====================

    @GetMapping(ApiConstants.KUPPI_REVIEW_TUTOR)
    @Operation(summary = "Get tutor reviews", description = "Get all reviews for a tutor")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getReviewsByTutor(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getReviewsByTutor(tutorId, pageable));
    }

    // ==================== Tutor Response ====================

    @PostMapping(ApiConstants.KUPPI_REVIEW_TUTOR_RESPONSE)
    @Operation(summary = "Add tutor response", description = "Tutor responds to a review")
    @PreAuthorize("hasAuthority('KUPPI:WRITE')")
    public ApiResponse<KuppiReviewResponse> addTutorResponse(
            @PathVariable Long reviewId,
            @Valid @RequestBody TutorReviewResponseRequest request) {
        log.info("Adding tutor response to review: {}", reviewId);
        return ApiResponse.success("Response added", reviewService.addTutorResponse(reviewId, request));
    }

    @GetMapping(ApiConstants.KUPPI_REVIEW_MY_HOSTED)
    @Operation(summary = "Get reviews for my sessions", description = "Tutor gets reviews for their hosted sessions")
    @PreAuthorize("hasAuthority('KUPPI:WRITE')")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getReviewsForMyHostedSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getReviewsForMyHostedSessions(pageable));
    }
}
