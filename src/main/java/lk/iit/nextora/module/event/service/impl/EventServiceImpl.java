package lk.iit.nextora.module.event.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.DuplicateResourceException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.infrastructure.notification.service.EventNotificationService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.EventSearchRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventAnalyticsResponse;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventRegistrationResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.entity.Event;
import lk.iit.nextora.module.event.entity.EventRegistration;
import lk.iit.nextora.module.event.mapper.EventMapper;
import lk.iit.nextora.module.event.repository.EventRegistrationRepository;
import lk.iit.nextora.module.event.repository.EventRepository;
import lk.iit.nextora.module.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of EventService with full business logic:
 * - Event CRUD with ownership validation
 * - Cover image upload to S3
 * - Registration/attendance tracking with capacity enforcement
 * - Async notifications on publish/cancel/reschedule
 * - Analytics and platform stats
 * - Soft & permanent delete with S3 cleanup
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final SecurityService securityService;
    private final EventMapper eventMapper;
    private final S3Service s3Service;
    private final EventNotificationService eventNotificationService;

    private static final String S3_EVENT_IMAGES_FOLDER = "event-images";
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

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
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
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

    @Override
    public PagedResponse<EventResponse> advancedSearch(EventSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "startAt";
        String sortDir = request.getSortDirection() != null ? request.getSortDirection() : "ASC";

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Event> events = eventRepository.advancedSearch(
                request.getKeyword(),
                request.getLocation(),
                request.getCreatorName(),
                request.getEventType(),
                request.getStartDate(),
                request.getEndDate(),
                PUBLIC_STATUSES,
                pageable
        );

        return toPagedResponse(events);
    }

    // ==================== Creator Operations ====================

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request, MultipartFile coverImage) {
        BaseUser creator = securityService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required to create events"));

        validateTimeRange(request.getStartAt(), request.getEndAt());

        if (request.getMaxAttendees() != null && request.getMaxAttendees() <= 0) {
            throw new BadRequestException("Max attendees must be a positive number");
        }

        Event event = eventMapper.toEntity(request);
        event.setCreatedBy(creator);
        event.setStatus(EventStatus.DRAFT);

        if (request.getEventType() == null) {
            event.setEventType(EventType.OTHER);
        }

        // Upload cover image to S3
        if (coverImage != null && !coverImage.isEmpty()) {
            validateImageFile(coverImage);
            String imageKey = s3Service.uploadFile(coverImage, S3_EVENT_IMAGES_FOLDER);
            event.setCoverImageKey(imageKey);
            event.setCoverImageUrl(s3Service.getPublicUrl(imageKey));
        }

        event = eventRepository.save(event);
        log.info("Event created by user {} (role: {}): {}", creator.getId(), creator.getRole(), event.getId());

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request, MultipartFile coverImage) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        if (!event.canEdit()) {
            throw new BadRequestException("Event can only be edited in DRAFT status");
        }

        // Validate time range: check both provided, or one provided with existing
        LocalDateTime newStart = request.getStartAt() != null ? request.getStartAt() : event.getStartAt();
        LocalDateTime newEnd = request.getEndAt() != null ? request.getEndAt() : event.getEndAt();
        if (request.getStartAt() != null || request.getEndAt() != null) {
            validateTimeRange(newStart, newEnd);
        }

        if (request.getMaxAttendees() != null && request.getMaxAttendees() <= 0) {
            throw new BadRequestException("Max attendees must be a positive number");
        }

        eventMapper.updateEventFromRequest(request, event);

        // Upload new cover image, cleanup old one
        if (coverImage != null && !coverImage.isEmpty()) {
            validateImageFile(coverImage);
            cleanupCoverImage(event);
            String imageKey = s3Service.uploadFile(coverImage, S3_EVENT_IMAGES_FOLDER);
            event.setCoverImageKey(imageKey);
            event.setCoverImageUrl(s3Service.getPublicUrl(imageKey));
        }

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

        if (!event.hasValidTimeRange()) {
            throw new BadRequestException("Event has invalid time range");
        }

        if (event.getStartAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot publish an event with a start time in the past");
        }

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            throw new BadRequestException("Event description is required before publishing");
        }

        if (event.getLocation() == null || event.getLocation().isBlank()) {
            throw new BadRequestException("Event location is required before publishing");
        }

        event.setStatus(EventStatus.PUBLISHED);
        event = eventRepository.save(event);

        log.info("Event {} published by user {}", eventId, currentUserId);

        // Send notification asynchronously - broadcast to all students
        eventNotificationService.notifyEventPublished(
                event.getId(),
                event.getTitle(),
                event.getEventType().getDisplayName(),
                event.getCreatedBy().getFullName(),
                event.getLocation(),
                event.getStartAt());

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse cancelEvent(Long eventId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        if (!event.getStatus().canCancel()) {
            throw new BadRequestException("Event in status " + event.getStatus().getDisplayName()
                    + " cannot be cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setCancellationReason(reason);
        event.setCancelledAt(LocalDateTime.now());
        event = eventRepository.save(event);

        log.info("Event {} cancelled by user {}", eventId, currentUserId);

        eventNotificationService.notifyEventCancelled(event.getId(), event.getTitle(), reason);

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

        if (event.getStatus() == EventStatus.PUBLISHED) {
            eventNotificationService.notifyEventRescheduled(
                    event.getId(), event.getTitle(), newStartTime);
        }

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void softDeleteEvent(Long eventId) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        validateEventOwnership(event, currentUserId);

        cleanupCoverImage(event);

        event.setIsDeleted(true);
        event.setDeletedAt(LocalDateTime.now());
        event.setDeletedBy(currentUserId);
        eventRepository.save(event);

        log.info("Event {} soft deleted by user {}", eventId, currentUserId);
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
                .totalViews(totalViews)
                .totalRegistrations(0L);

        if (!mostViewed.isEmpty()) {
            Event topEvent = mostViewed.get(0);
            builder.mostViewedEventId(topEvent.getId())
                    .mostViewedEventTitle(topEvent.getTitle())
                    .mostViewedEventViews(topEvent.getViewCount());
        }

        return builder.build();
    }

    // ==================== Registration Operations ====================

    @Override
    @Transactional
    public EventRegistrationResponse registerForEvent(Long eventId) {
        BaseUser user = securityService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication required to register"));
        Long userId = user.getId();

        Event event = findEventById(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Can only register for published events");
        }

        if (event.getStartAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot register for an event that has already started");
        }

        // Check if creator is trying to register for own event
        if (event.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("You cannot register for your own event");
        }

        // Check capacity
        if (event.getMaxAttendees() != null) {
            long currentCount = registrationRepository.countActiveByEventId(eventId);
            if (currentCount >= event.getMaxAttendees()) {
                throw new BadRequestException("Event is full. Maximum attendees (" + event.getMaxAttendees() + ") reached");
            }
        }

        // Check existing registration
        Optional<EventRegistration> existingReg = registrationRepository.findByEventIdAndUserId(eventId, userId);

        EventRegistration registration;
        if (existingReg.isPresent()) {
            registration = existingReg.get();
            if (!registration.getIsCancelled()) {
                throw new DuplicateResourceException("EventRegistration", "eventId_userId", eventId + "_" + userId);
            }
            // Re-register
            registration.reRegister();
            log.info("User {} re-registered for event {}", userId, eventId);
        } else {
            registration = EventRegistration.builder()
                    .event(event)
                    .user(user)
                    .registeredAt(LocalDateTime.now())
                    .isCancelled(false)
                    .build();
            log.info("User {} registered for event {}", userId, eventId);
        }

        registration = registrationRepository.save(registration);

        // Send registration confirmation notification
        eventNotificationService.notifyRegistrationConfirmed(
                event.getId(), event.getTitle(), event.getStartAt(), event.getLocation(), userId);

        return eventMapper.toRegistrationResponse(registration);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long eventId) {
        Long userId = securityService.getCurrentUserId();

        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "eventId", eventId));

        if (registration.getIsCancelled()) {
            throw new BadRequestException("Registration is already cancelled");
        }

        Event event = findEventById(eventId);

        registration.cancel();
        registrationRepository.save(registration);

        // Send cancellation notification
        eventNotificationService.notifyRegistrationCancelled(event.getId(), event.getTitle(), userId);

        log.info("User {} cancelled registration for event {}", userId, eventId);
    }

    @Override
    public boolean isRegistered(Long eventId) {
        Long userId = securityService.getCurrentUserId();
        return registrationRepository.isUserRegistered(eventId, userId);
    }

    @Override
    public PagedResponse<EventRegistrationResponse> getMyRegistrations(Pageable pageable) {
        Long userId = securityService.getCurrentUserId();
        Page<EventRegistration> registrations = registrationRepository.findActiveByUserId(userId, pageable);

        List<EventRegistrationResponse> content = eventMapper.toRegistrationResponseList(registrations.getContent());
        return PagedResponse.<EventRegistrationResponse>builder()
                .content(content)
                .pageNumber(registrations.getNumber())
                .pageSize(registrations.getSize())
                .totalElements(registrations.getTotalElements())
                .totalPages(registrations.getTotalPages())
                .first(registrations.isFirst())
                .last(registrations.isLast())
                .empty(registrations.isEmpty())
                .build();
    }

    @Override
    public PagedResponse<EventRegistrationResponse> getEventRegistrations(Long eventId, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        // Only event creator or admin can view registrations
        if (!event.getCreatedBy().getId().equals(currentUserId) && !securityService.isAdmin()) {
            throw new UnauthorizedException("Only the event creator or an admin can view registrations");
        }

        Page<EventRegistration> registrations = registrationRepository.findActiveByEventId(eventId, pageable);

        List<EventRegistrationResponse> content = eventMapper.toRegistrationResponseList(registrations.getContent());
        return PagedResponse.<EventRegistrationResponse>builder()
                .content(content)
                .pageNumber(registrations.getNumber())
                .pageSize(registrations.getSize())
                .totalElements(registrations.getTotalElements())
                .totalPages(registrations.getTotalPages())
                .first(registrations.isFirst())
                .last(registrations.isLast())
                .empty(registrations.isEmpty())
                .build();
    }

    // ==================== Admin Operations ====================

    @Override
    @Transactional
    public EventResponse adminUpdateEvent(Long eventId, UpdateEventRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        Event event = findEventById(eventId);

        LocalDateTime newStart = request.getStartAt() != null ? request.getStartAt() : event.getStartAt();
        LocalDateTime newEnd = request.getEndAt() != null ? request.getEndAt() : event.getEndAt();
        if (request.getStartAt() != null || request.getEndAt() != null) {
            if (newStart.isAfter(newEnd)) {
                throw new BadRequestException("Start time must be before end time");
            }
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

        cleanupCoverImage(event);

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

        LocalDateTime startOfWeek = now.with(ChronoField.DAY_OF_WEEK, 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
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
        long totalRegistrations = registrationRepository.countAllActive();

        return EventPlatformStatsResponse.builder()
                .totalEvents(totalEvents)
                .totalCreators(totalCreators)
                .publishedEvents(publishedEvents)
                .cancelledEvents(cancelledEvents)
                .completedEvents(completedEvents)
                .draftEvents(draftEvents)
                .totalViews(totalViews)
                .totalRegistrations(totalRegistrations)
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

        cleanupCoverImage(event);

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
        if (startAt.isAfter(endAt) || startAt.isEqual(endAt)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.getContentType() == null || !ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, and WebP images are allowed");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Image size must not exceed 5MB");
        }
    }

    private void cleanupCoverImage(Event event) {
        if (event.getCoverImageKey() != null) {
            try {
                s3Service.deleteFile(event.getCoverImageKey());
                log.debug("Cleaned up cover image for event {}", event.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup cover image for event {}: {}", event.getId(), e.getMessage());
            }
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
