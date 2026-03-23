package lk.iit.nextora.module.event.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.entity.Event;
import lk.iit.nextora.module.event.mapper.EventMapper;
import lk.iit.nextora.module.event.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Unit Tests")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Nested
    @DisplayName("getPublishedEvents")
    class GetPublishedEventsTests {

        @Test
        @DisplayName("Should return paginated published events")
        void getPublishedEvents_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Event event = Event.builder().id(1L).title("Tech Conference").status(EventStatus.PUBLISHED).build();
            Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

            when(eventRepository.findByStatusAndIsDeletedFalse(EventStatus.PUBLISHED, pageable)).thenReturn(page);
            when(eventMapper.toResponse(event)).thenReturn(EventResponse.builder().id(1L).title("Tech Conference").build());

            PagedResponse<EventResponse> result = eventService.getPublishedEvents(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(eventRepository, times(1)).findByStatusAndIsDeletedFalse(EventStatus.PUBLISHED, pageable);
        }
    }

    @Nested
    @DisplayName("getEventById")
    class GetEventByIdTests {

        @Test
        @DisplayName("Should return event by ID with incremented view count")
        void getEventById_success() {
            Long eventId = 1L;
            Event event = Event.builder().id(eventId).title("Tech Conference").viewCount(100L).build();
            EventResponse response = EventResponse.builder().id(eventId).title("Tech Conference").viewCount(101L).build();

            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(response);

            EventResponse result = eventService.getEventById(eventId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(eventId);
            verify(eventRepository, times(1)).findByIdAndIsDeletedFalse(eventId);
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void getEventById_notFound() {
            Long eventId = 999L;
            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.getEventById(eventId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createEvent")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event with DRAFT status")
        void createEvent_success() {
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            CreateEventRequest request = CreateEventRequest.builder()
                    .title("Annual Tech Conference").startAt(start).endAt(start.plusHours(2))
                    .eventType(EventType.ACADEMIC).build();

            Event entity = Event.builder().id(1L).title("Annual Tech Conference").status(EventStatus.DRAFT).build();
            EventResponse response = EventResponse.builder().id(1L).title("Annual Tech Conference").status(EventStatus.DRAFT).build();

            when(eventRepository.save(any(Event.class))).thenReturn(entity);
            when(eventMapper.toResponse(entity)).thenReturn(response);

            EventResponse result = eventService.createEvent(request, null);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(EventStatus.DRAFT);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("publishEvent")
    class PublishEventTests {

        @Test
        @DisplayName("Should publish event to PUBLISHED status")
        void publishEvent_success() {
            Long eventId = 1L;
            Event event = Event.builder().id(eventId).title("Tech Conference").status(EventStatus.DRAFT).build();
            event.setStatus(EventStatus.PUBLISHED);
            EventResponse response = EventResponse.builder().id(eventId).status(EventStatus.PUBLISHED).build();

            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(response);

            EventResponse result = eventService.publishEvent(eventId);

            assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("cancelEvent")
    class CancelEventTests {

        @Test
        @DisplayName("Should cancel event with reason")
        void cancelEvent_success() {
            Long eventId = 1L;
            Event event = Event.builder().id(eventId).title("Tech Conference").status(EventStatus.PUBLISHED).build();
            event.setStatus(EventStatus.CANCELLED);
            EventResponse response = EventResponse.builder().id(eventId).status(EventStatus.CANCELLED).build();

            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(response);

            EventResponse result = eventService.cancelEvent(eventId, "Unforeseen circumstances");

            assertThat(result.getStatus()).isEqualTo(EventStatus.CANCELLED);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEventTests {

        @Test
        @DisplayName("Should soft delete event")
        void deleteEvent_success() {
            Long eventId = 1L;
            Event event = Event.builder().id(eventId).isDeleted(false).build();

            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            eventService.softDeleteEvent(eventId);

            verify(eventRepository, times(1)).findByIdAndIsDeletedFalse(eventId);
            verify(eventRepository, times(1)).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("adminForceDelete")
    class AdminForceDeleteTests {

        @Test
        @DisplayName("Should permanently delete event")
        void adminForceDelete_success() {
            Long eventId = 1L;
            Event event = Event.builder().id(eventId).title("Tech Conference").build();

            when(eventRepository.findByIdAndIsDeletedFalse(eventId)).thenReturn(Optional.of(event));
            doNothing().when(eventRepository).delete(event);

            eventService.permanentlyDeleteEvent(eventId);

            verify(eventRepository, times(1)).findByIdAndIsDeletedFalse(eventId);
            verify(eventRepository, times(1)).delete(event);
        }
    }
}



