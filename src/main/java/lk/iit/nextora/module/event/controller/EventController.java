package lk.iit.nextora.module.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.EventSearchRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventAnalyticsResponse;
import lk.iit.nextora.module.event.dto.response.EventRegistrationResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Controller for Event operations.
 * Handles public viewing, creator CRUD, registration, and analytics endpoints.
 */
@RestController
@RequestMapping(ApiConstants.EVENTS)
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

    private final EventService eventService;

    // ==================== Public Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all published events")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> getPublishedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success("Events retrieved successfully", eventService.getPublishedEvents(pageable));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID (increments view count)")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<EventResponse> getEventById(@PathVariable Long eventId) {
        return ApiResponse.success("Event retrieved successfully", eventService.getEventById(eventId));
    }

    @GetMapping(ApiConstants.EVENT_SEARCH)
    @Operation(summary = "Search events by keyword")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully", eventService.searchEvents(keyword, pageable));
    }

    @PostMapping("/search/advanced")
    @Operation(summary = "Advanced multi-criteria search")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> advancedSearch(@RequestBody EventSearchRequest request) {
        return ApiResponse.success("Search completed successfully", eventService.advancedSearch(request));
    }

    @GetMapping(ApiConstants.EVENT_UPCOMING)
    @Operation(summary = "Get upcoming events")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> getUpcomingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Upcoming events retrieved successfully",
                eventService.getUpcomingEvents(pageable));
    }

    @GetMapping(ApiConstants.EVENT_ONGOING)
    @Operation(summary = "Get ongoing events")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> getOngoingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Ongoing events retrieved successfully",
                eventService.getOngoingEvents(pageable));
    }

    @GetMapping(ApiConstants.EVENT_PAST)
    @Operation(summary = "Get past events")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> getPastEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Past events retrieved successfully",
                eventService.getPastEvents(pageable));
    }

    @GetMapping("/search/date")
    @Operation(summary = "Search by date range")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully",
                eventService.searchByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/search/type")
    @Operation(summary = "Filter events by type")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> searchByType(
            @RequestParam EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully",
                eventService.searchByType(eventType, pageable));
    }

    @GetMapping("/search/location")
    @Operation(summary = "Search by location")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> searchByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully",
                eventService.searchByLocation(location, pageable));
    }

    @GetMapping("/search/creator")
    @Operation(summary = "Search by creator name")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> searchByCreatorName(
            @RequestParam String creatorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Search completed successfully",
                eventService.searchByCreatorName(creatorName, pageable));
    }

    // ==================== Creator Endpoints ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create event with optional cover image")
    @PreAuthorize("hasAuthority('EVENT:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventResponse> createEvent(
            @Valid @RequestPart("event") CreateEventRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.success("Event created successfully",
                eventService.createEvent(request, coverImage));
    }

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update own event (DRAFT only) with optional cover image")
    @PreAuthorize("hasAuthority('EVENT:UPDATE')")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestPart("event") UpdateEventRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.success("Event updated successfully",
                eventService.updateEvent(eventId, request, coverImage));
    }

    @PostMapping("/{eventId}" + ApiConstants.EVENT_PUBLISH)
    @Operation(summary = "Publish event (DRAFT -> PUBLISHED)")
    @PreAuthorize("hasAuthority('EVENT:UPDATE')")
    public ApiResponse<EventResponse> publishEvent(@PathVariable Long eventId) {
        return ApiResponse.success("Event published successfully", eventService.publishEvent(eventId));
    }

    @PostMapping("/{eventId}" + ApiConstants.EVENT_CANCEL)
    @Operation(summary = "Cancel event with optional reason")
    @PreAuthorize("hasAuthority('EVENT:UPDATE')")
    public ApiResponse<EventResponse> cancelEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) String reason) {
        return ApiResponse.success("Event cancelled successfully", eventService.cancelEvent(eventId, reason));
    }

    @PostMapping("/{eventId}" + ApiConstants.EVENT_RESCHEDULE)
    @Operation(summary = "Reschedule event")
    @PreAuthorize("hasAuthority('EVENT:UPDATE')")
    public ApiResponse<EventResponse> rescheduleEvent(
            @PathVariable Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndTime) {
        return ApiResponse.success("Event rescheduled successfully",
                eventService.rescheduleEvent(eventId, newStartTime, newEndTime));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete own event (soft delete)")
    @PreAuthorize("hasAuthority('EVENT:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.softDeleteEvent(eventId);
        return ApiResponse.success("Event deleted successfully", null);
    }

    @GetMapping(ApiConstants.EVENT_MY)
    @Operation(summary = "Get events created by current user")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventResponse>> getMyEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponse.success("My events retrieved successfully", eventService.getMyEvents(pageable));
    }

    @GetMapping(ApiConstants.EVENT_MY + ApiConstants.EVENT_ANALYTICS)
    @Operation(summary = "Get analytics for own events")
    @PreAuthorize("hasAuthority('EVENT:VIEW_ANALYTICS')")
    public ApiResponse<EventAnalyticsResponse> getMyAnalytics() {
        return ApiResponse.success("Analytics retrieved successfully", eventService.getMyAnalytics());
    }

    // ==================== Registration Endpoints ====================

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register for an event")
    @PreAuthorize("hasAuthority('EVENT:REGISTER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventRegistrationResponse> registerForEvent(@PathVariable Long eventId) {
        return ApiResponse.success("Successfully registered for event",
                eventService.registerForEvent(eventId));
    }

    @DeleteMapping("/{eventId}/register")
    @Operation(summary = "Cancel registration for an event")
    @PreAuthorize("hasAuthority('EVENT:REGISTER')")
    public ApiResponse<Void> cancelRegistration(@PathVariable Long eventId) {
        eventService.cancelRegistration(eventId);
        return ApiResponse.success("Registration cancelled successfully", null);
    }

    @GetMapping("/{eventId}/is-registered")
    @Operation(summary = "Check if current user is registered for an event")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<Boolean> isRegistered(@PathVariable Long eventId) {
        return ApiResponse.success("Registration status retrieved", eventService.isRegistered(eventId));
    }

    @GetMapping("/my/registrations")
    @Operation(summary = "Get all events the current user is registered for")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventRegistrationResponse>> getMyRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Registrations retrieved successfully",
                eventService.getMyRegistrations(pageable));
    }

    @GetMapping("/{eventId}/registrations")
    @Operation(summary = "Get all registrations for an event (creator/admin only)")
    @PreAuthorize("hasAuthority('EVENT:READ')")
    public ApiResponse<PagedResponse<EventRegistrationResponse>> getEventRegistrations(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Event registrations retrieved successfully",
                eventService.getEventRegistrations(eventId, pageable));
    }
}
