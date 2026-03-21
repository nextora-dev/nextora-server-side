package lk.iit.nextora.module.kuppi.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiAnalyticsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiSessionController Unit Tests")
class KuppiSessionControllerTest {

    @Mock private KuppiSessionService sessionService;

    @InjectMocks
    private KuppiSessionController controller;

    // ============================================================
    // GET PUBLIC SESSIONS
    // ============================================================

    @Nested
    @DisplayName("GET / (public sessions)")
    class GetPublicSessionsTests {

        @Test
        @DisplayName("Should return paginated public sessions")
        void getPublicSessions_returnsPaginatedResult() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of(KuppiSessionResponse.builder().id(1L).build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false)
                    .build();
            when(sessionService.getPublicSessions(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.getPublicSessions(0, 10, "scheduledStartTime", "ASC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
        }
    }

    // ============================================================
    // POST / - createSession
    // ============================================================

    @Nested
    @DisplayName("POST / - createSession")
    class CreateSessionTests {

        @Test
        @DisplayName("Should create session with files and return response")
        void createSession_withFiles_returnsCreated() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 1, 12, 0);
            MultipartFile[] files = {
                    new MockMultipartFile("file", "notes.pdf", "application/pdf", "pdf content".getBytes())
            };
            KuppiSessionResponse response = KuppiSessionResponse.builder()
                    .id(1L).title("Math Kuppi").subject("Mathematics").build();
            when(sessionService.createSession(any(), any(MultipartFile[].class))).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.createSession(
                    "Math Kuppi", "Description", "Mathematics",
                    start, end, "https://meet.google.com/abc", "Google Meet",
                    files, null, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getTitle()).isEqualTo("Math Kuppi");
            verify(sessionService).createSession(any(), any(MultipartFile[].class));
        }

        @Test
        @DisplayName("Should normalize files[] array parameter to files")
        void createSession_filesArrayParam_normalizesCorrectly() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 1, 12, 0);
            MultipartFile[] filesArray = {
                    new MockMultipartFile("files[]", "notes.pdf", "application/pdf", "pdf".getBytes())
            };
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(1L).build();
            when(sessionService.createSession(any(), any(MultipartFile[].class))).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.createSession(
                    "Title", null, "Math", start, end, "link", null,
                    null, filesArray, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).createSession(any(), eq(filesArray));
        }

        @Test
        @DisplayName("Should normalize single file to files array")
        void createSession_singleFile_normalizesCorrectly() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 1, 12, 0);
            MultipartFile singleFile = new MockMultipartFile("file", "notes.pdf", "application/pdf", "pdf".getBytes());
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(1L).build();
            when(sessionService.createSession(any(), any(MultipartFile[].class))).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.createSession(
                    "Title", null, "Math", start, end, "link", null,
                    null, null, singleFile);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).createSession(any(), argThat(f -> f.length == 1));
        }

        @Test
        @DisplayName("Should create session without files")
        void createSession_noFiles_passesNullFiles() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 1, 12, 0);
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(1L).build();
            when(sessionService.createSession(any(), isNull())).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.createSession(
                    "Title", null, "Math", start, end, "link", null,
                    null, null, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).createSession(any(), isNull());
        }
    }

    // ============================================================
    // PUT /{sessionId} - updateSession
    // ============================================================

    @Nested
    @DisplayName("PUT /{sessionId} - updateSession")
    class UpdateSessionTests {

        @Test
        @DisplayName("Should update session with files and return response")
        void updateSession_withFiles_returnsUpdated() {
            // Given
            MultipartFile[] files = {
                    new MockMultipartFile("file", "updated.pdf", "application/pdf", "pdf".getBytes())
            };
            KuppiSessionResponse response = KuppiSessionResponse.builder()
                    .id(5L).title("Updated Title").build();
            when(sessionService.updateSession(eq(5L), any(), any(MultipartFile[].class), any()))
                    .thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.updateSession(
                    5L, "Updated Title", null, null, null, null, null, null,
                    files, null, null, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @DisplayName("Should pass removeNoteIds to service")
        void updateSession_withRemoveNoteIds_passesToService() {
            // Given
            List<Long> removeNoteIds = List.of(1L, 2L, 3L);
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(5L).build();
            when(sessionService.updateSession(eq(5L), any(), isNull(), eq(removeNoteIds)))
                    .thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.updateSession(
                    5L, null, null, null, null, null, null, null,
                    null, null, null, removeNoteIds);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).updateSession(eq(5L), any(), isNull(), eq(removeNoteIds));
        }
    }

    // ============================================================
    // GET SESSION BY ID
    // ============================================================

    @Nested
    @DisplayName("GET /{sessionId}")
    class GetSessionByIdTests {

        @Test
        @DisplayName("Should return session by ID")
        void getSessionById_returnsSession() {
            // Given
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(5L).title("Math Kuppi").build();
            when(sessionService.getSessionById(5L)).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.getSessionById(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(5L);
            assertThat(result.getData().getTitle()).isEqualTo("Math Kuppi");
        }
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Nested
    @DisplayName("GET /search")
    class SearchSessionsTests {

        @Test
        @DisplayName("Should search sessions by keyword")
        void searchSessions_returnsResults() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.searchSessions(eq("math"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.searchSessions("math", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).searchSessions(eq("math"), any());
        }

        @Test
        @DisplayName("Should search sessions by subject")
        void searchBySubject_delegatesToService() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.searchBySubject(eq("Physics"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.searchBySubject("Physics", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).searchBySubject(eq("Physics"), any());
        }

        @Test
        @DisplayName("Should search sessions by host name")
        void searchByHostName_delegatesToService() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.searchByHostName(eq("John"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.searchByHostName("John", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).searchByHostName(eq("John"), any());
        }

        @Test
        @DisplayName("Should search sessions by date range")
        void searchByDateRange_delegatesToService() {
            // Given
            LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59);
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.searchByDateRange(eq(start), eq(end), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.searchByDateRange(start, end, 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).searchByDateRange(eq(start), eq(end), any());
        }
    }

    // ============================================================
    // UPCOMING SESSIONS
    // ============================================================

    @Nested
    @DisplayName("GET /upcoming")
    class GetUpcomingSessionsTests {

        @Test
        @DisplayName("Should return upcoming sessions")
        void getUpcomingSessions_delegatesToService() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.getUpcomingSessions(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result =
                    controller.getUpcomingSessions(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ============================================================
    // CANCEL SESSION
    // ============================================================

    @Nested
    @DisplayName("POST /{sessionId}/cancel")
    class CancelSessionTests {

        @Test
        @DisplayName("Should cancel session and return success")
        void cancelSession_returnsSuccess() {
            // Given
            doNothing().when(sessionService).cancelSession(5L, "Schedule conflict");

            // When
            ApiResponse<Void> result = controller.cancelSession(5L, "Schedule conflict");

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).cancelSession(5L, "Schedule conflict");
        }
    }

    // ============================================================
    // RESCHEDULE SESSION
    // ============================================================

    @Nested
    @DisplayName("POST /{sessionId}/reschedule")
    class RescheduleSessionTests {

        @Test
        @DisplayName("Should reschedule session and return updated response")
        void rescheduleSession_returnsUpdatedResponse() {
            // Given
            LocalDateTime newStart = LocalDateTime.of(2026, 4, 1, 10, 0);
            LocalDateTime newEnd = LocalDateTime.of(2026, 4, 1, 12, 0);
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(5L).build();
            when(sessionService.rescheduleSession(5L, newStart, newEnd)).thenReturn(response);

            // When
            ApiResponse<KuppiSessionResponse> result = controller.rescheduleSession(5L, newStart, newEnd);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(5L);
        }
    }

    // ============================================================
    // SOFT DELETE SESSION
    // ============================================================

    @Nested
    @DisplayName("DELETE /{sessionId}")
    class SoftDeleteSessionTests {

        @Test
        @DisplayName("Should soft delete session and return success")
        void softDeleteSession_returnsSuccess() {
            // Given
            doNothing().when(sessionService).softDeleteSession(5L);

            // When
            ApiResponse<Void> result = controller.softDeleteSession(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(sessionService).softDeleteSession(5L);
        }
    }

    // ============================================================
    // GET MY SESSIONS
    // ============================================================

    @Nested
    @DisplayName("GET /my")
    class GetMySessionsTests {

        @Test
        @DisplayName("Should return current user's sessions")
        void getMySessions_delegatesToService() {
            // Given
            PagedResponse<KuppiSessionResponse> paged = PagedResponse.<KuppiSessionResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(sessionService.getMySessions(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiSessionResponse>> result = controller.getMySessions(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ============================================================
    // GET MY ANALYTICS
    // ============================================================

    @Nested
    @DisplayName("GET /analytics")
    class GetMyAnalyticsTests {

        @Test
        @DisplayName("Should return analytics for current user")
        void getMyAnalytics_returnsAnalytics() {
            // Given
            KuppiAnalyticsResponse analytics = KuppiAnalyticsResponse.builder()
                    .totalSessions(10L).completedSessions(5L).upcomingSessions(3L).build();
            when(sessionService.getMyAnalytics()).thenReturn(analytics);

            // When
            ApiResponse<KuppiAnalyticsResponse> result = controller.getMyAnalytics();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTotalSessions()).isEqualTo(10L);
        }
    }
}