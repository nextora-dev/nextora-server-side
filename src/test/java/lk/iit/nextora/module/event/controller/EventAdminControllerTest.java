package lk.iit.nextora.module.event.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventPlatformStatsResponse;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventAdminController Unit Tests")
class EventAdminControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventAdminController controller;

    // ============================================================
    // ADMIN GET ALL EVENTS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/events (get all events)")
    class AdminGetAllEventsTests {

        @Test
        @DisplayName("Should return all events including drafts and published")
        void getAllEvents_success() {
            // Given
            List<EventResponse> events = List.of(
                    EventResponse.builder().id(1L).title("Published Event").build(),
                    EventResponse.builder().id(2L).title("Draft Event").build()
            );
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(events)
                    .totalElements(2L)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            when(eventService.adminGetAllEvents(any())).thenReturn(response);

            // When
            ApiResponse<PagedResponse<EventResponse>> result = controller.getAllEvents(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getData().getContent()).hasSize(2);
            assertThat(result.getData().getTotalElements()).isEqualTo(2L);
            verify(eventService, times(1)).adminGetAllEvents(any());
        }

        @Test
        @DisplayName("Should return empty list when no events exist")
        void getAllEvents_empty() {
            // Given
            PagedResponse<EventResponse> response = PagedResponse.<EventResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .empty(true)
                    .build();

            when(eventService.adminGetAllEvents(any())).thenReturn(response);

            // When
            ApiResponse<PagedResponse<EventResponse>> result = controller.getAllEvents(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.getData().isEmpty()).isTrue();
        }
    }

    // ============================================================
    // ADMIN UPDATE EVENT TESTS
    // ============================================================

    @Nested
    @DisplayName("PUT /api/v1/admin/events/{id} (admin update)")
    class AdminUpdateEventTests {

        @Test
        @DisplayName("Should update event successfully")
        void adminUpdateEvent_success() {
            // Given
            Long eventId = 1L;
            UpdateEventRequest request = UpdateEventRequest.builder()
                    .title("Updated Event Title")
                    .build();
            EventResponse response = EventResponse.builder()
                    .id(eventId)
                    .title("Updated Event Title")
                    .build();

            when(eventService.adminUpdateEvent(eventId, request)).thenReturn(response);

            // When
            ApiResponse<EventResponse> result = controller.adminUpdateEvent(eventId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getData().getTitle()).isEqualTo("Updated Event Title");
            verify(eventService, times(1)).adminUpdateEvent(eventId, request);
        }
    }

    // ============================================================
    // ADMIN DELETE EVENT TESTS
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/v1/admin/events/{id} (delete)")
    class AdminDeleteEventTests {

        @Test
        @DisplayName("Should delete event successfully")
        void deleteEvent_success() {
            // Given
            Long eventId = 1L;
            doNothing().when(eventService).adminDeleteEvent(eventId);

            // When
            ApiResponse<Void> response = controller.deleteEvent(eventId);

            // Then
            assertThat(response).isNotNull();
            verify(eventService, times(1)).adminDeleteEvent(eventId);
        }

        @Test
        @DisplayName("Should handle delete of multiple events independently")
        void deleteEvent_multipleEvents() {
            // Given
            Long eventId1 = 1L;
            Long eventId2 = 2L;
            Long eventId3 = 3L;
            doNothing().when(eventService).adminDeleteEvent(anyLong());

            // When
            ApiResponse<Void> response1 = controller.deleteEvent(eventId1);
            ApiResponse<Void> response2 = controller.deleteEvent(eventId2);
            ApiResponse<Void> response3 = controller.deleteEvent(eventId3);

            // Then
            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            assertThat(response3).isNotNull();
            verify(eventService, times(3)).adminDeleteEvent(anyLong());
        }
    }

    // ============================================================
    // PERMANENTLY DELETE EVENT TESTS
    // ============================================================

    @Nested
    @DisplayName("DELETE /api/v1/admin/events/{id}/permanent (permanently delete)")
    class PermanentlyDeleteEventTests {

        @Test
        @DisplayName("Should permanently delete event successfully")
        void permanentlyDeleteEvent_success() {
            // Given
            Long eventId = 1L;
            doNothing().when(eventService).permanentlyDeleteEvent(eventId);

            // When
            ApiResponse<Void> response = controller.permanentlyDeleteEvent(eventId);

            // Then
            assertThat(response).isNotNull();
            verify(eventService, times(1)).permanentlyDeleteEvent(eventId);
        }
    }

    // ============================================================
    // GET PLATFORM STATS TESTS
    // ============================================================

    @Nested
    @DisplayName("GET /api/v1/admin/events/stats (platform statistics)")
    class GetPlatformStatsTests {

        @Test
        @DisplayName("Should return platform statistics")
        void getPlatformStats_success() {
            // Given
            EventPlatformStatsResponse stats = EventPlatformStatsResponse.builder()
                    .totalEvents(100L)
                    .totalRegistrations(5000L)
                    .upcomingEvents(25L)
                    .build();

            when(eventService.getPlatformStats()).thenReturn(stats);

            // When
            ApiResponse<EventPlatformStatsResponse> response = controller.getPlatformStats();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().getTotalEvents()).isEqualTo(100L);
            assertThat(response.getData().getTotalRegistrations()).isEqualTo(5000L);
            verify(eventService, times(1)).getPlatformStats();
        }
    }
}



