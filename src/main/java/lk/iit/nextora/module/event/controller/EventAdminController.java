package lk.iit.nextora.module.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for Event operations
 * Handles admin-level event management endpoints
 */
@RestController
@RequestMapping(ApiConstants.EVENT_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Event Admin", description = "Admin event management endpoints")
public class EventAdminController {

    private final EventService eventService;

    // ==================== Admin Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all events", description = "Admin: Get all events including drafts")
    @PreAuthorize("hasAuthority('EVENT:ADMIN_UPDATE')")
    public ApiResponse<PagedResponse<EventResponse>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<EventResponse> response = eventService.adminGetAllEvents(pageable);
        return ApiResponse.success("Events retrieved successfully", response);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Admin update event", description = "Admin: Update any event")
    @PreAuthorize("hasAuthority('EVENT:ADMIN_UPDATE')")
    public ApiResponse<EventResponse> adminUpdateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.adminUpdateEvent(eventId, request);
        return ApiResponse.success("Event updated successfully", response);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete event", description = "Admin: Soft delete an event")
    @PreAuthorize("hasAuthority('EVENT:ADMIN_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.adminDeleteEvent(eventId);
        return ApiResponse.success("Event deleted successfully", null);
    }

    // ==================== Platform Statistics ====================

    @GetMapping(ApiConstants.EVENT_STATS)
    @Operation(summary = "Get platform statistics", description = "Admin: Get event platform usage statistics")
    @PreAuthorize("hasAuthority('EVENT:VIEW_STATS')")
    public ApiResponse<EventPlatformStatsResponse> getPlatformStats() {
        EventPlatformStatsResponse response = eventService.getPlatformStats();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }

    // ==================== Super Admin Endpoints ====================

    @DeleteMapping("/{eventId}/permanent")
    @Operation(summary = "Permanently delete event", description = "Super Admin: Permanently delete an event")
    @PreAuthorize("hasAuthority('EVENT:PERMANENT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> permanentlyDeleteEvent(@PathVariable Long eventId) {
        eventService.permanentlyDeleteEvent(eventId);
        return ApiResponse.success("Event permanently deleted", null);
    }
}
