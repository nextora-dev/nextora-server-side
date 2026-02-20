package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.module.kuppi.dto.request.ReviewKuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.*;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import lk.iit.nextora.module.kuppi.service.KuppiReviewService;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.KUPPI_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Kuppi Admin", description = "Admin endpoints for Kuppi management")
public class KuppiAdminController {

    private final KuppiSessionService sessionService;
    private final KuppiNoteService noteService;
    private final KuppiApplicationService applicationService;
    private final KuppiReviewService reviewService;

    // ==================== Session Management ====================

    @PutMapping(ApiConstants.KUPPI_ADMIN_SESSIONS_BY_ID)
    @Operation(summary = "Edit session", description = "Edit any Kuppi session (admin override)")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_UPDATE')")
    public ApiResponse<KuppiSessionResponse> adminUpdateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateKuppiSessionRequest request) {
        KuppiSessionResponse response = sessionService.adminUpdateSession(sessionId, request);
        return ApiResponse.success("Session updated successfully", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_SESSIONS_BY_ID)
    @Operation(summary = "Remove session", description = "Soft delete any Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_DELETE')")
    public ApiResponse<Void> adminDeleteSession(@PathVariable Long sessionId) {
        sessionService.adminDeleteSession(sessionId);
        return ApiResponse.success("Session removed successfully");
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_SESSIONS_PERMANENT)
    @Operation(summary = "Permanently delete session", description = "Permanently delete a Kuppi session (Super Admin only)")
    @PreAuthorize("hasAuthority('KUPPI:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteSession(@PathVariable Long sessionId) {
        sessionService.permanentlyDeleteSession(sessionId);
        return ApiResponse.success("Session permanently deleted");
    }

    // ==================== Note Management ====================

    @PutMapping(ApiConstants.KUPPI_ADMIN_NOTES_BY_ID)
    @Operation(summary = "Edit note", description = "Edit any note (admin override)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:ADMIN_UPDATE')")
    public ApiResponse<KuppiNoteResponse> adminUpdateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateKuppiNoteRequest request) {
        KuppiNoteResponse response = noteService.adminUpdateNote(noteId, request);
        return ApiResponse.success("Note updated successfully", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_NOTES_BY_ID)
    @Operation(summary = "Remove note", description = "Soft delete any note")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:ADMIN_DELETE')")
    public ApiResponse<Void> adminDeleteNote(@PathVariable Long noteId) {
        noteService.adminDeleteNote(noteId);
        return ApiResponse.success("Note removed successfully");
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_NOTES_PERMANENT)
    @Operation(summary = "Permanently delete note", description = "Permanently delete a note (Super Admin only)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteNote(@PathVariable Long noteId) {
        noteService.permanentlyDeleteNote(noteId);
        return ApiResponse.success("Note permanently deleted");
    }

    // ==================== Application Management ====================

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_BASE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Get all applications",
            description = "Get all Kuppi Student applications with pagination. " +
                    "Accessible by Admin, Super Admin, and Academic Staff."
    )
    public ApiResponse<PagedResponse<KuppiApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<KuppiApplicationResponse> response = applicationService.getAllApplications(pageable);
        return ApiResponse.success("Applications retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_STATUS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Get applications by status",
            description = "Get applications filtered by status (PENDING, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED)"
    )
    public ApiResponse<PagedResponse<KuppiApplicationResponse>> getApplicationsByStatus(
            @PathVariable KuppiApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiApplicationResponse> response = applicationService.getApplicationsByStatus(status, pageable);
        return ApiResponse.success("Applications retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_PENDING)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Get pending applications",
            description = "Get all pending applications awaiting review"
    )
    public ApiResponse<PagedResponse<KuppiApplicationResponse>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiApplicationResponse> response = applicationService.getPendingApplications(pageable);
        return ApiResponse.success("Pending applications retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_ACTIVE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Get active applications",
            description = "Get all active applications (pending + under review)"
    )
    public ApiResponse<PagedResponse<KuppiApplicationResponse>> getActiveApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiApplicationResponse> response = applicationService.getActiveApplications(pageable);
        return ApiResponse.success("Active applications retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Get application by ID",
            description = "Get detailed information about a specific application"
    )
    public ApiResponse<KuppiApplicationResponse> getApplicationById(@PathVariable Long applicationId) {
        KuppiApplicationResponse response = applicationService.getApplicationById(applicationId);
        return ApiResponse.success("Application retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_SEARCH)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Search applications",
            description = "Search applications by student name, email, or student ID"
    )
    public ApiResponse<PagedResponse<KuppiApplicationResponse>> searchApplications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiApplicationResponse> response = applicationService.searchApplications(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_STATS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:STATS')")
    @Operation(
            summary = "Get application statistics",
            description = "Get statistics about Kuppi Student applications"
    )
    public ApiResponse<KuppiApplicationStatsResponse> getApplicationStats() {
        KuppiApplicationStatsResponse stats = applicationService.getApplicationStats();
        return ApiResponse.success("Statistics retrieved successfully", stats);
    }

    @PutMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_APPROVE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:APPROVE')")
    @Operation(
            summary = "Approve application",
            description = "Approve a Kuppi Student application. " +
                    "This grants the KUPPI_STUDENT role to the student, " +
                    "allowing them to create Kuppi sessions and upload notes."
    )
    public ApiResponse<KuppiApplicationResponse> approveApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody(required = false) ReviewKuppiApplicationRequest request) {

        if (request == null) {
            request = new ReviewKuppiApplicationRequest();
        }
        KuppiApplicationResponse response = applicationService.approveApplication(applicationId, request);
        return ApiResponse.success("Application approved successfully", response);
    }

    @PutMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_REJECT)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:REJECT')")
    @Operation(
            summary = "Reject application",
            description = "Reject a Kuppi Student application. Rejection reason is required."
    )
    public ApiResponse<KuppiApplicationResponse> rejectApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewKuppiApplicationRequest request) {

        KuppiApplicationResponse response = applicationService.rejectApplication(applicationId, request);
        return ApiResponse.success("Application rejected", response);
    }

    @PutMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_UNDER_REVIEW)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_ALL')")
    @Operation(
            summary = "Mark as under review",
            description = "Mark a pending application as under review"
    )
    public ApiResponse<KuppiApplicationResponse> markUnderReview(@PathVariable Long applicationId) {
        KuppiApplicationResponse response = applicationService.markUnderReview(applicationId);
        return ApiResponse.success("Application marked as under review", response);
    }

    // ==================== Review Management ====================

    @GetMapping("/reviews")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_READ')")
    @Operation(summary = "Get all reviews", description = "Get all reviews for admin")
    public ApiResponse<PagedResponse<KuppiReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success("Reviews retrieved", reviewService.getAllReviews(pageable));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_DELETE')")
    @Operation(summary = "Delete review", description = "Admin delete a review")
    public ApiResponse<Void> adminDeleteReview(@PathVariable Long reviewId) {
        reviewService.adminDeleteReview(reviewId);
        return ApiResponse.success("Review deleted");
    }

    // ==================== Platform Statistics ====================

    @GetMapping(ApiConstants.KUPPI_STATS)
    @Operation(summary = "Get platform statistics", description = "Get Kuppi platform usage statistics")
    @PreAuthorize("hasAuthority('KUPPI:VIEW_STATS')")
    public ApiResponse<KuppiPlatformStatsResponse> getPlatformStats() {
        KuppiPlatformStatsResponse response = sessionService.getPlatformStats();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }
}
