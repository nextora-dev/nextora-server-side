package lk.iit.nextora.module.event.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventAnalyticsResponse;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Service interface for Event operations
 */
public interface EventService {

    // ==================== Public Operations (All Users) ====================

    /**
     * Get all published events
     */
    PagedResponse<EventResponse> getPublishedEvents(Pageable pageable);

    /**
     * Get event by ID (increments view count)
     */
    EventResponse getEventById(Long eventId);

    /**
     * Search events by keyword (title + description)
     */
    PagedResponse<EventResponse> searchEvents(String keyword, Pageable pageable);

    /**
     * Get upcoming events
     */
    PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable);

    /**
     * Get ongoing events
     */
    PagedResponse<EventResponse> getOngoingEvents(Pageable pageable);

    /**
     * Get past events
     */
    PagedResponse<EventResponse> getPastEvents(Pageable pageable);

    /**
     * Search events by date range
     */
    PagedResponse<EventResponse> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Search events by event type
     */
    PagedResponse<EventResponse> searchByType(EventType eventType, Pageable pageable);

    /**
     * Search events by location
     */
    PagedResponse<EventResponse> searchByLocation(String location, Pageable pageable);

    /**
     * Search events by creator name
     */
    PagedResponse<EventResponse> searchByCreatorName(String creatorName, Pageable pageable);

    // ==================== Creator Operations ====================

    /**
     * Create a new event
     */
    EventResponse createEvent(CreateEventRequest request);

    /**
     * Update own event (only if DRAFT status)
     */
    EventResponse updateEvent(Long eventId, UpdateEventRequest request);

    /**
     * Publish event (DRAFT -> PUBLISHED)
     */
    EventResponse publishEvent(Long eventId);

    /**
     * Cancel event
     */
    EventResponse cancelEvent(Long eventId, String reason);

    /**
     * Reschedule event
     */
    EventResponse rescheduleEvent(Long eventId, LocalDateTime newStartTime, LocalDateTime newEndTime);

    /**
     * Get my events (created by current user)
     */
    PagedResponse<EventResponse> getMyEvents(Pageable pageable);

    /**
     * Get analytics for own events
     */
    EventAnalyticsResponse getMyAnalytics();

    // ==================== Admin Operations ====================

    /**
     * Admin: Update any event
     */
    EventResponse adminUpdateEvent(Long eventId, UpdateEventRequest request);

    /**
     * Admin: Delete event (soft delete)
     */
    void adminDeleteEvent(Long eventId);

    /**
     * Admin: Get all events (including drafts)
     */
    PagedResponse<EventResponse> adminGetAllEvents(Pageable pageable);

    /**
     * Admin: Get platform statistics
     */
    EventPlatformStatsResponse getPlatformStats();

    // ==================== Super Admin Operations ====================

    /**
     * Super Admin: Permanently delete an event
     */
    void permanentlyDeleteEvent(Long eventId);
}
