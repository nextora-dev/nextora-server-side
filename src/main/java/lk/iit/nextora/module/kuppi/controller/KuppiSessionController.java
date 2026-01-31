package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.*;
import lk.iit.nextora.module.kuppi.dto.response.*;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for Kuppi session operations
 * Handles endpoints for Normal Students and Kuppi Students
 */
@RestController
@RequestMapping(ApiConstants.KUPPI_SESSIONS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Sessions", description = "Kuppi session management endpoints")
public class KuppiSessionController {

    private final KuppiSessionService sessionService;

    // ==================== Public/Normal Student Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all sessions", description = "View all approved Kuppi sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getPublicSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledStartTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<KuppiSessionResponse> response = sessionService.getPublicSessions(pageable);
        return ApiResponse.success("Sessions retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SESSION_BY_ID)
    @Operation(summary = "Get session by ID", description = "View a specific Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiSessionResponse> getSessionById(@PathVariable Long sessionId) {
        KuppiSessionResponse response = sessionService.getSessionById(sessionId);
        return ApiResponse.success("Session retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH)
    @Operation(summary = "Search sessions", description = "Search sessions by keyword")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchSessions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchSessions(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_SUBJECT)
    @Operation(summary = "Search by subject", description = "Search sessions by subject")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchBySubject(
            @RequestParam String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchBySubject(subject, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_HOST)
    @Operation(summary = "Search by host", description = "Search sessions by host/lecturer name")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchByHostName(
            @RequestParam String hostName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchByHostName(hostName, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_DATE)
    @Operation(summary = "Search by date range", description = "Search sessions by date range")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchByDateRange(startDate, endDate, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_UPCOMING)
    @Operation(summary = "Get upcoming sessions", description = "Get all upcoming Kuppi sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getUpcomingSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.getUpcomingSessions(pageable);
        return ApiResponse.success("Upcoming sessions retrieved successfully", response);
    }


    // ==================== Kuppi Student Endpoints ====================

    @PostMapping
    @Operation(summary = "Create session", description = "Create a new Kuppi session (Kuppi Students only)")
    @PreAuthorize("hasAuthority('KUPPI:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KuppiSessionResponse> createSession(@Valid @RequestBody CreateKuppiSessionRequest request) {
        KuppiSessionResponse response = sessionService.createSession(request);
        return ApiResponse.success("Session created successfully", response);
    }

    @PutMapping(ApiConstants.KUPPI_SESSION_BY_ID)
    @Operation(summary = "Update session", description = "Update own Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:UPDATE')")
    public ApiResponse<KuppiSessionResponse> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateKuppiSessionRequest request) {
        KuppiSessionResponse response = sessionService.updateSession(sessionId, request);
        return ApiResponse.success("Session updated successfully", response);
    }

    @PostMapping(ApiConstants.KUPPI_CANCEL)
    @Operation(summary = "Cancel session", description = "Cancel own Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:CANCEL')")
    public ApiResponse<Void> cancelSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        sessionService.cancelSession(sessionId, reason);
        return ApiResponse.success("Session cancelled successfully");
    }

    @PostMapping(ApiConstants.KUPPI_RESCHEDULE)
    @Operation(summary = "Reschedule session", description = "Reschedule own Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:RESCHEDULE')")
    public ApiResponse<KuppiSessionResponse> rescheduleSession(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndTime) {
        KuppiSessionResponse response = sessionService.rescheduleSession(sessionId, newStartTime, newEndTime);
        return ApiResponse.success("Session rescheduled successfully", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_SESSION_BY_ID)
    @Operation(summary = "Delete session", description = "Delete own Kuppi session (soft delete)")
    @PreAuthorize("hasAuthority('KUPPI:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return ApiResponse.success("Session deleted successfully");
    }

    @GetMapping(ApiConstants.KUPPI_MY)
    @Operation(summary = "Get my sessions", description = "Get own created sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getMySessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.getMySessions(pageable);
        return ApiResponse.success("Your sessions retrieved successfully", response);
    }

    @GetMapping( ApiConstants.KUPPI_ANALYTICS)
    @Operation(summary = "Get my analytics", description = "Get analytics for own sessions and notes")
    @PreAuthorize("hasAuthority('KUPPI:VIEW_ANALYTICS')")
    public ApiResponse<KuppiAnalyticsResponse> getMyAnalytics() {
        KuppiAnalyticsResponse response = sessionService.getMyAnalytics();
        return ApiResponse.success("Analytics retrieved successfully", response);
    }
}