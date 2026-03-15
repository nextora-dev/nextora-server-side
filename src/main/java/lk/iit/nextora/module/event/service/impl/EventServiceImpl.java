package lk.iit.nextora.module.event.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventAnalyticsResponse;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.entity.Event;
import lk.iit.nextora.module.event.mapper.EventMapper;
import lk.iit.nextora.module.event.repository.EventRepository;
import lk.iit.nextora.module.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * Implementation of EventService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final SecurityService securityService;
    private final EventMapper eventMapper;

    private static final List<EventStatus> PUBLIC_STATUSES = List.of(
            EventStatus.PUBLISHED,
            EventStatus.COMPLETED);

    // ==================== Public Operations ====================

    @Override
    public PagedResponse<EventResponse> getPublishedEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findPublishedEvents(pageable);
        return toPagedResponse(events);
    }

    @Override
    @Transactional
    public EventResponse getEventById(Long eventId) {
        Event event = findEventById(eventId);
        event.incrementViewCount();
        eventRepository.save(event);
        return eventMapper.toResponse(event);
    }

    @Override
    public PagedResponse<EventResponse> searchEvents(String keyword, Pageable pageable) {
        Page<Event> events = eventRepository.searchByTitleOrDescription(keyword, PUBLIC_STATUSES, pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> getUpcomingEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> getOngoingEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findOngoingEvents(LocalDateTime.now(), pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> getPastEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findPastEvents(LocalDateTime.now(), pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        Page<Event> events = eventRepository.findByDateRange(startDate, endDate, PUBLIC_STATUSES, pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> searchByType(EventType eventType, Pageable pageable) {
        Page<Event> events = eventRepository.findByEventType(eventType, PUBLIC_STATUSES, pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> searchByLocation(String location, Pageable pageable) {
        Page<Event> events = eventRepository.searchByLocation(location, PUBLIC_STATUSES, pageable);
        return toPagedResponse(events);
    }

    @Override
    public PagedResponse<EventResponse> searchByCreatorName(String creatorName, Pageable pageable) {
        Page<Event> events = eventRepository.searchByCreatorName(creatorName, PUBLIC_STATUSES, pageable);
        return toPagedResponse(events);
    }

    // ==================== Creator Operations ====================

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        BaseUser creator = securityService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required to create events"));

        // Validate time range
        validateTimeRange(request.getStartAt(), request.getEndAt());

        Event event = eventMapper.toEntity(request);
        event.setCreatedBy(creator);
        event.setStatus(EventStatus.DRAFT);

        // Set default event type if not provided
        if (request.getEventType() == null) {
            event.setEventType(EventType.OTHER);
        }

        event = eventRepository.save(event);
        log.info("Event created by user {} (role: {}): {}", creator.getId(), creator.getRole(), event.getId());

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        // Validate ownership
        validateEventOwnership(event, currentUserId);

        // Validate event can be edited (only DRAFT status)
        if (!event.canEdit()) {
            throw new BadRequestException("Event can only be edited in DRAFT status");
        }

        // Validate time range if both are provided
        if (request.getStartAt() != null && request.getEndAt() != null) {
            validateTimeRange(request.getStartAt(), request.getEndAt());
        }

        eventMapper.updateEventFromRequest(request, event);
        event = eventRepository.save(event);

        log.info("Event {} updated by user {}", eventId, currentUserId);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse publishEvent(Long eventId) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT events can be published");
        }

        // Validate time range before publishing
        if (!event.hasValidTimeRange()) {
            throw new BadRequestException("Event has invalid time range");
        }

        event.setStatus(EventStatus.PUBLISHED);
        event = eventRepository.save(event);

        log.info("Event {} published by user {}", eventId, currentUserId);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse cancelEvent(Long eventId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("Event is already cancelled");
        }
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed event");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setCancellationReason(reason);
        event.setCancelledAt(LocalDateTime.now());
        event = eventRepository.save(event);

        log.info("Event {} cancelled by user {}", eventId, currentUserId);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse rescheduleEvent(Long eventId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot reschedule a cancelled event");
        }
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new BadRequestException("Cannot reschedule a completed event");
        }

        validateTimeRange(newStartTime, newEndTime);

        event.setStartAt(newStartTime);
        event.setEndAt(newEndTime);
        event = eventRepository.save(event);

        log.info("Event {} rescheduled by user {}", eventId, currentUserId);
        return eventMapper.toResponse(event);
    }

    @Override
    public PagedResponse<EventResponse> getMyEvents(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Event> events = eventRepository.findByCreatedByIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(events);
    }

    @Override
    public EventAnalyticsResponse getMyAnalytics() {
        Long currentUserId = securityService.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        long totalEvents = eventRepository.countByCreatedByIdAndIsDeletedFalse(currentUserId);
        long publishedEvents = eventRepository.countByCreatedByIdAndStatusAndIsDeletedFalse(currentUserId,
                EventStatus.PUBLISHED);
        long upcomingEvents = eventRepository.countUpcomingByCreatedById(currentUserId, now);
        long completedEvents = eventRepository.countByCreatedByIdAndStatusAndIsDeletedFalse(currentUserId,
                EventStatus.COMPLETED);
        long cancelledEvents = eventRepository.countByCreatedByIdAndStatusAndIsDeletedFalse(currentUserId,
                EventStatus.CANCELLED);
        long totalViews = eventRepository.sumViewCountByCreatedById(currentUserId);

        // Find most viewed event
        List<Event> mostViewed = eventRepository.findMostViewedByCreatedById(currentUserId, PageRequest.of(0, 1));

        EventAnalyticsResponse.EventAnalyticsResponseBuilder builder = EventAnalyticsResponse.builder()
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .upcomingEvents(upcomingEvents)
                .completedEvents(completedEvents)
                .cancelledEvents(cancelledEvents)
                .totalViews(totalViews);

        if (!mostViewed.isEmpty()) {
            Event topEvent = mostViewed.get(0);
            builder.mostViewedEventId(topEvent.getId())
                    .mostViewedEventTitle(topEvent.getTitle())
                    .mostViewedEventViews(topEvent.getViewCount());
        }

        return builder.build();
    }

    // ==================== Admin Operations ====================

    @Override
    @Transactional
    public EventResponse adminUpdateEvent(Long eventId, UpdateEventRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        if (request.getStartAt() != null && request.getEndAt() != null) {
            validateTimeRange(request.getStartAt(), request.getEndAt());
        }

        eventMapper.updateEventFromRequest(request, event);
        event = eventRepository.save(event);

        log.info("Event {} updated by admin {}", eventId, currentUserId);
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void adminDeleteEvent(Long eventId) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        event.setIsDeleted(true);
        event.setDeletedAt(LocalDateTime.now());
        event.setDeletedBy(currentUserId);
        eventRepository.save(event);

        log.info("Event {} soft deleted by admin {}", eventId, currentUserId);
    }

    @Override
    public PagedResponse<EventResponse> adminGetAllEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        return toPagedResponse(events);
    }

    @Override
    public EventPlatformStatsResponse getPlatformStats() {
        LocalDateTime now = LocalDateTime.now();

        // Start of current week (Monday)
        LocalDateTime startOfWeek = now.with(ChronoField.DAY_OF_WEEK, 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Start of current month
        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        long totalEvents = eventRepository.countByIsDeletedFalse();
        long totalCreators = eventRepository.countDistinctCreators();
        long publishedEvents = eventRepository.countByStatusAndIsDeletedFalse(EventStatus.PUBLISHED);
        long cancelledEvents = eventRepository.countByStatusAndIsDeletedFalse(EventStatus.CANCELLED);
        long completedEvents = eventRepository.countByStatusAndIsDeletedFalse(EventStatus.COMPLETED);
        long draftEvents = eventRepository.countByStatusAndIsDeletedFalse(EventStatus.DRAFT);
        long totalViews = eventRepository.sumAllViewCounts();
        long eventsThisWeek = eventRepository.countByCreatedAtBetween(startOfWeek, now);
        long eventsThisMonth = eventRepository.countByCreatedAtBetween(startOfMonth, now);
        long upcomingEvents = eventRepository.countUpcomingEvents(now);
        long newCreatorsThisMonth = eventRepository.countNewCreatorsThisMonth(startOfMonth, now);

        return EventPlatformStatsResponse.builder()
                .totalEvents(totalEvents)
                .totalCreators(totalCreators)
                .publishedEvents(publishedEvents)
                .cancelledEvents(cancelledEvents)
                .completedEvents(completedEvents)
                .draftEvents(draftEvents)
                .totalViews(totalViews)
                .eventsThisWeek(eventsThisWeek)
                .eventsThisMonth(eventsThisMonth)
                .upcomingEvents(upcomingEvents)
                .newCreatorsThisMonth(newCreatorsThisMonth)
                .build();
    }

    // ==================== Super Admin Operations ====================

    @Override
    @Transactional
    public void permanentlyDeleteEvent(Long eventId) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        eventRepository.delete(event);
        log.info("Event {} permanently deleted by super admin {}", eventId, currentUserId);
    }

    // ==================== Helper Methods ====================

    private Event findEventById(Long eventId) {
        return eventRepository.findByIdWithCreator(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
    }


    private void validateEventOwnership(Event event, Long userId) {
        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own events");
        }
    }

    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
    }

    private PagedResponse<EventResponse> toPagedResponse(Page<Event> events) {
        List<EventResponse> content = eventMapper.toResponseList(events.getContent());
        return PagedResponse.<EventResponse>builder()
                .content(content)
                .pageNumber(events.getNumber())
                .pageSize(events.getSize())
                .totalElements(events.getTotalElements())
                .totalPages(events.getTotalPages())
                .first(events.isFirst())
                .last(events.isLast())
                .empty(events.isEmpty())
                .build();
    }
}
