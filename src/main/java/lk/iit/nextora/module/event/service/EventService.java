package lk.iit.nextora.module.event.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.EventSearchRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventAnalyticsResponse;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventRegistrationResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Service interface for Event operations
 */
public interface EventService {

    // ==================== Public Operations (All Users) ====================

    PagedResponse<EventResponse> getPublishedEvents(Pageable pageable);

    EventResponse getEventById(Long eventId);

    PagedResponse<EventResponse> searchEvents(String keyword, Pageable pageable);

    PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable);

    PagedResponse<EventResponse> getOngoingEvents(Pageable pageable);

    PagedResponse<EventResponse> getPastEvents(Pageable pageable);

    PagedResponse<EventResponse> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    PagedResponse<EventResponse> searchByType(EventType eventType, Pageable pageable);

    PagedResponse<EventResponse> searchByLocation(String location, Pageable pageable);

    PagedResponse<EventResponse> searchByCreatorName(String creatorName, Pageable pageable);

    PagedResponse<EventResponse> advancedSearch(EventSearchRequest request);

    // ==================== Creator Operations ====================

    EventResponse createEvent(CreateEventRequest request, MultipartFile coverImage);

    EventResponse updateEvent(Long eventId, UpdateEventRequest request, MultipartFile coverImage);

    EventResponse publishEvent(Long eventId);

    EventResponse cancelEvent(Long eventId, String reason);

    EventResponse rescheduleEvent(Long eventId, LocalDateTime newStartTime, LocalDateTime newEndTime);

    void softDeleteEvent(Long eventId);

    PagedResponse<EventResponse> getMyEvents(Pageable pageable);

    EventAnalyticsResponse getMyAnalytics();

    // ==================== Registration Operations ====================

    EventRegistrationResponse registerForEvent(Long eventId);

    void cancelRegistration(Long eventId);

    boolean isRegistered(Long eventId);

    PagedResponse<EventRegistrationResponse> getMyRegistrations(Pageable pageable);

    PagedResponse<EventRegistrationResponse> getEventRegistrations(Long eventId, Pageable pageable);

    // ==================== Admin Operations ====================

    EventResponse adminUpdateEvent(Long eventId, UpdateEventRequest request);

    void adminDeleteEvent(Long eventId);

    PagedResponse<EventResponse> adminGetAllEvents(Pageable pageable);

    EventPlatformStatsResponse getPlatformStats();

    // ==================== Super Admin Operations ====================

    void permanentlyDeleteEvent(Long eventId);
}
