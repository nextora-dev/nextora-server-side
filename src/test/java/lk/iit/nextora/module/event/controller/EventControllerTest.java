package lk.iit.nextora.module.event.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.EventSearchRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Unit Tests")
class EventControllerTest {

    @Mock private EventService eventService;
    @InjectMocks private EventController controller;

    @Nested
    @DisplayName("getPublishedEvents")
    class GetPublishedEventsTests {

        @Test
        @DisplayName("Should return paginated published events")
        void getPublishedEvents_success() {
            Pageable pageable = PageRequest.of(0, 10);
            List<EventResponse> events = List.of(
                    EventResponse.builder().id(1L).title("Tech Conference").status(EventStatus.PUBLISHED).build(),
                    EventResponse.builder().id(2L).title("Webinar on AI").status(EventStatus.PUBLISHED).build()
            );
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(events).totalElements(2L).build();

            when(eventService.getPublishedEvents(any(Pageable.class))).thenReturn(response);

            ApiResponse<PagedResponse<EventResponse>> result = controller.getPublishedEvents(0, 10, "startAt", "ASC");

            assertThat(result.getData().getContent()).hasSize(2);
            verify(eventService, times(1)).getPublishedEvents(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no events")
        void getPublishedEvents_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(List.of()).totalElements(0L).build();

            when(eventService.getPublishedEvents(any(Pageable.class))).thenReturn(response);

            ApiResponse<PagedResponse<EventResponse>> result = controller.getPublishedEvents(0, 10, "startAt", "ASC");

            assertThat(result.getData().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEventById")
    class GetEventByIdTests {

        @Test
        @DisplayName("Should return event by ID")
        void getEventById_success() {
            Long eventId = 1L;
            EventResponse response = EventResponse.builder()
                    .id(eventId).title("Tech Conference").viewCount(101L).build();

            when(eventService.getEventById(eventId)).thenReturn(response);

            ApiResponse<EventResponse> result = controller.getEventById(eventId);

            assertThat(result.getData().getId()).isEqualTo(eventId);
            assertThat(result.getData().getViewCount()).isEqualTo(101L);
            verify(eventService, times(1)).getEventById(eventId);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getEventById_notFound() {
            Long eventId = 999L;
            when(eventService.getEventById(eventId))
                    .thenThrow(new ResourceNotFoundException("Event", "id", eventId.toString()));

            assertThatThrownBy(() -> controller.getEventById(eventId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchEvents")
    class SearchEventsTests {

        @Test
        @DisplayName("Should search events by keyword")
        void searchEvents_success() {
            String keyword = "tech";
            List<EventResponse> results = List.of(
                    EventResponse.builder().id(1L).title("Tech Conference").build()
            );
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(results).totalElements(1L).build();

            when(eventService.searchEvents(eq(keyword), any(Pageable.class))).thenReturn(response);

            ApiResponse<PagedResponse<EventResponse>> result = controller.searchEvents(keyword, 0, 10);

            assertThat(result.getData().getContent()).hasSize(1);
            verify(eventService, times(1)).searchEvents(eq(keyword), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("advancedSearch")
    class AdvancedSearchTests {

        @Test
        @DisplayName("Should perform advanced search")
        void advancedSearch_success() {
            EventSearchRequest request = EventSearchRequest.builder()
                    .keyword("conference").eventType(EventType.ACADEMIC).build();

            List<EventResponse> results = List.of(
                    EventResponse.builder().id(1L).title("Tech Conference").eventType(EventType.ACADEMIC).build()
            );
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(results).totalElements(1L).build();

            when(eventService.advancedSearch(any(EventSearchRequest.class))).thenReturn(response);

            ApiResponse<PagedResponse<EventResponse>> result = controller.advancedSearch(request);

            assertThat(result.getData().getContent()).hasSize(1);
            verify(eventService, times(1)).advancedSearch(any(EventSearchRequest.class));
        }
    }

    @Nested
    @DisplayName("createEvent")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event successfully")
        void createEvent_success() {
            LocalDateTime start = LocalDateTime.now().plusDays(7);
            CreateEventRequest request = CreateEventRequest.builder()
                    .title("Annual Tech Conference").startAt(start).endAt(start.plusHours(2))
                    .eventType(EventType.ACADEMIC).build();

            EventResponse response = EventResponse.builder()
                    .id(1L).title("Annual Tech Conference").status(EventStatus.DRAFT).build();

            when(eventService.createEvent(any(CreateEventRequest.class), any())).thenReturn(response);

            ApiResponse<EventResponse> result = controller.createEvent(request, null);

            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getStatus()).isEqualTo(EventStatus.DRAFT);
            verify(eventService, times(1)).createEvent(any(CreateEventRequest.class), any());
        }
    }

    @Nested
    @DisplayName("updateEvent")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event successfully")
        void updateEvent_success() {
            Long eventId = 1L;
            UpdateEventRequest request = UpdateEventRequest.builder()
                    .title("Updated Title").build();

            EventResponse response = EventResponse.builder()
                    .id(eventId).title("Updated Title").build();

            when(eventService.updateEvent(eq(eventId), any(UpdateEventRequest.class), any())).thenReturn(response);

            ApiResponse<EventResponse> result = controller.updateEvent(eventId, request, null);

            assertThat(result.getData().getTitle()).isEqualTo("Updated Title");
            verify(eventService, times(1)).updateEvent(eq(eventId), any(UpdateEventRequest.class), any());
        }
    }

    @Nested
    @DisplayName("publishEvent")
    class PublishEventTests {

        @Test
        @DisplayName("Should publish event successfully")
        void publishEvent_success() {
            Long eventId = 1L;
            EventResponse response = EventResponse.builder()
                    .id(eventId).title("Tech Conference").status(EventStatus.PUBLISHED).build();

            when(eventService.publishEvent(eventId)).thenReturn(response);

            ApiResponse<EventResponse> result = controller.publishEvent(eventId);

            assertThat(result.getData().getStatus()).isEqualTo(EventStatus.PUBLISHED);
            verify(eventService, times(1)).publishEvent(eventId);
        }
    }

    @Nested
    @DisplayName("cancelEvent")
    class CancelEventTests {

        @Test
        @DisplayName("Should cancel event successfully")
        void cancelEvent_success() {
            Long eventId = 1L;
            String reason = "Unforeseen circumstances";
            EventResponse response = EventResponse.builder()
                    .id(eventId).title("Tech Conference").status(EventStatus.CANCELLED).build();

            when(eventService.cancelEvent(eventId, reason)).thenReturn(response);

            ApiResponse<EventResponse> result = controller.cancelEvent(eventId, reason);

            assertThat(result.getData().getStatus()).isEqualTo(EventStatus.CANCELLED);
            verify(eventService, times(1)).cancelEvent(eventId, reason);
        }
    }


    @Nested
    @DisplayName("registerForEvent")
    class RegisterEventTests {

        @Test
        @DisplayName("Should register for event successfully")
        void registerForEvent_success() {
            Long eventId = 1L;
            when(eventService.registerForEvent(eventId)).thenReturn(null);

            ApiResponse<?> response = controller.registerForEvent(eventId);

            assertThat(response).isNotNull();
            verify(eventService, times(1)).registerForEvent(eventId);
        }
    }
}

