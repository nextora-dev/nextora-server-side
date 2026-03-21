package lk.iit.nextora.module.kuppi.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.module.kuppi.dto.request.ReviewKuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiAdminController Unit Tests")
class KuppiAdminControllerTest {

    @Mock private KuppiSessionService sessionService;
    @Mock private KuppiNoteService noteService;
    @Mock private KuppiApplicationService applicationService;

    @InjectMocks
    private KuppiAdminController controller;

    // ============================================================
    // GET /applications - getAllApplications
    // ============================================================

    @Nested
    @DisplayName("GET /applications - getAllApplications")
    class GetAllApplicationsTests {

        @Test
        @DisplayName("Should return paginated applications")
        void getAllApplications_returnsPaginatedResult() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of(
                            KuppiApplicationResponse.builder().id(1L).build(),
                            KuppiApplicationResponse.builder().id(2L).build()
                    ))
                    .totalElements(2L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false).build();
            when(applicationService.getAllApplications(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.getAllApplications(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(2);
            assertThat(result.getData().getTotalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle empty applications list")
        void getAllApplications_empty_returnsEmptyPage() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(applicationService.getAllApplications(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.getAllApplications(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should pass correct sort parameters to service")
        void getAllApplications_customSort_passesCorrectPageable() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(1).pageSize(5)
                    .totalPages(0).first(false).last(true).empty(true).build();
            when(applicationService.getAllApplications(any())).thenReturn(paged);

            // When
            controller.getAllApplications(1, 5, "email", "ASC");

            // Then
            verify(applicationService).getAllApplications(argThat(pageable ->
                    pageable.getPageNumber() == 1 && pageable.getPageSize() == 5
            ));
        }
    }

    // ============================================================
    // GET /applications/status/{status} - getApplicationsByStatus
    // ============================================================

    @Nested
    @DisplayName("GET /applications/status/{status} - getApplicationsByStatus")
    class GetApplicationsByStatusTests {

        @Test
        @DisplayName("Should return applications filtered by status")
        void getApplicationsByStatus_returnsFilteredResult() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of(KuppiApplicationResponse.builder().id(1L)
                            .status(KuppiApplicationStatus.PENDING).build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false).build();
            when(applicationService.getApplicationsByStatus(eq(KuppiApplicationStatus.PENDING), any()))
                    .thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.getApplicationsByStatus(KuppiApplicationStatus.PENDING, 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
            verify(applicationService).getApplicationsByStatus(eq(KuppiApplicationStatus.PENDING), any());
        }
    }

    // ============================================================
    // GET /applications/pending - getPendingApplications
    // ============================================================

    @Nested
    @DisplayName("GET /applications/pending - getPendingApplications")
    class GetPendingApplicationsTests {

        @Test
        @DisplayName("Should return pending applications")
        void getPendingApplications_returnsResult() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of(KuppiApplicationResponse.builder().id(1L).build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false).build();
            when(applicationService.getPendingApplications(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.getPendingApplications(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Pending");
        }
    }

    // ============================================================
    // GET /applications/active - getActiveApplications
    // ============================================================

    @Nested
    @DisplayName("GET /applications/active - getActiveApplications")
    class GetActiveApplicationsTests {

        @Test
        @DisplayName("Should return active applications")
        void getActiveApplications_returnsResult() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(applicationService.getActiveApplications(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.getActiveApplications(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(applicationService).getActiveApplications(any());
        }
    }

    // ============================================================
    // GET /applications/{applicationId} - getApplicationById
    // ============================================================

    @Nested
    @DisplayName("GET /applications/{applicationId} - getApplicationById")
    class GetApplicationByIdTests {

        @Test
        @DisplayName("Should return application by ID")
        void getApplicationById_returnsApplication() {
            // Given
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.UNDER_REVIEW).build();
            when(applicationService.getApplicationById(5L)).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.getApplicationById(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(5L);
            assertThat(result.getData().getStatus()).isEqualTo(KuppiApplicationStatus.UNDER_REVIEW);
        }

        @Test
        @DisplayName("Should propagate ResourceNotFoundException for non-existent application")
        void getApplicationById_notFound_propagatesException() {
            // Given
            when(applicationService.getApplicationById(999L))
                    .thenThrow(new lk.iit.nextora.common.exception.custom.ResourceNotFoundException(
                            "KuppiApplication", "id", 999L));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.ResourceNotFoundException.class,
                    () -> controller.getApplicationById(999L));
        }
    }

    // ============================================================
    // GET /applications/search - searchApplications
    // ============================================================

    @Nested
    @DisplayName("GET /applications/search - searchApplications")
    class SearchApplicationsTests {

        @Test
        @DisplayName("Should search applications by keyword")
        void searchApplications_delegatesToService() {
            // Given
            PagedResponse<KuppiApplicationResponse> paged = PagedResponse.<KuppiApplicationResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(applicationService.searchApplications(eq("john"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiApplicationResponse>> result =
                    controller.searchApplications("john", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(applicationService).searchApplications(eq("john"), any());
        }
    }

    // ============================================================
    // GET /applications/stats - getApplicationStats
    // ============================================================

    @Nested
    @DisplayName("GET /applications/stats - getApplicationStats")
    class GetApplicationStatsTests {

        @Test
        @DisplayName("Should return application statistics")
        void getApplicationStats_returnsStats() {
            // Given
            KuppiApplicationStatsResponse stats = KuppiApplicationStatsResponse.builder()
                    .totalApplications(50L).pendingApplications(10L)
                    .approvedApplications(30L).rejectedApplications(5L)
                    .cancelledApplications(3L).underReviewApplications(2L)
                    .applicationsToday(5L).totalKuppiStudents(25L).build();
            when(applicationService.getApplicationStats()).thenReturn(stats);

            // When
            ApiResponse<KuppiApplicationStatsResponse> result = controller.getApplicationStats();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTotalApplications()).isEqualTo(50L);
            assertThat(result.getData().getPendingApplications()).isEqualTo(10L);
            assertThat(result.getData().getTotalKuppiStudents()).isEqualTo(25L);
        }
    }

    // ============================================================
    // PUT /applications/{applicationId}/approve - approveApplication
    // ============================================================

    @Nested
    @DisplayName("PUT /applications/{applicationId}/approve - approveApplication")
    class ApproveApplicationTests {

        @Test
        @DisplayName("Should approve application with review notes")
        void approveApplication_withNotes_returnsApproved() {
            // Given
            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            request.setReviewNotes("Good academic record");
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.APPROVED).build();
            when(applicationService.approveApplication(eq(5L), any())).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.approveApplication(5L, request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getStatus()).isEqualTo(KuppiApplicationStatus.APPROVED);
            assertThat(result.getMessage()).contains("approved");
        }

        @Test
        @DisplayName("Should handle null request body by creating empty request")
        void approveApplication_nullRequest_createsEmptyRequest() {
            // Given
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.APPROVED).build();
            when(applicationService.approveApplication(eq(5L), any())).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.approveApplication(5L, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(applicationService).approveApplication(eq(5L), any(ReviewKuppiApplicationRequest.class));
        }
    }

    // ============================================================
    // PUT /applications/{applicationId}/reject - rejectApplication
    // ============================================================

    @Nested
    @DisplayName("PUT /applications/{applicationId}/reject - rejectApplication")
    class RejectApplicationTests {

        @Test
        @DisplayName("Should reject application with reason")
        void rejectApplication_returnsRejected() {
            // Given
            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            request.setRejectionReason("GPA too low");
            request.setReviewNotes("Minimum GPA is 3.0");
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.REJECTED).build();
            when(applicationService.rejectApplication(eq(5L), any())).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.rejectApplication(5L, request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getStatus()).isEqualTo(KuppiApplicationStatus.REJECTED);
            verify(applicationService).rejectApplication(5L, request);
        }
    }

    // ============================================================
    // PUT /applications/{applicationId}/under-review - markUnderReview
    // ============================================================

    @Nested
    @DisplayName("PUT /applications/{applicationId}/under-review - markUnderReview")
    class MarkUnderReviewTests {

        @Test
        @DisplayName("Should mark application as under review")
        void markUnderReview_returnsUpdated() {
            // Given
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.UNDER_REVIEW).build();
            when(applicationService.markUnderReview(5L)).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.markUnderReview(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getStatus()).isEqualTo(KuppiApplicationStatus.UNDER_REVIEW);
            assertThat(result.getMessage()).contains("under review");
        }
    }

    // ============================================================
    // GET /stats - getPlatformStats
    // ============================================================

    @Nested
    @DisplayName("GET /stats - getPlatformStats")
    class GetPlatformStatsTests {

        @Test
        @DisplayName("Should return platform statistics")
        void getPlatformStats_returnsStats() {
            // Given
            KuppiPlatformStatsResponse stats = KuppiPlatformStatsResponse.builder()
                    .totalSessions(100L).completedSessions(80L)
                    .cancelledSessions(5L).totalNotes(200L).build();
            when(sessionService.getPlatformStats()).thenReturn(stats);

            // When
            ApiResponse<KuppiPlatformStatsResponse> result = controller.getPlatformStats();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTotalSessions()).isEqualTo(100L);
            assertThat(result.getData().getCompletedSessions()).isEqualTo(80L);
        }
    }

    // ============================================================
    // DELETE /applications/{applicationId}/permanent - permanentlyDeleteApplication
    // ============================================================

    @Nested
    @DisplayName("DELETE /applications/{applicationId}/permanent - permanentlyDeleteApplication")
    class PermanentlyDeleteApplicationTests {

        @Test
        @DisplayName("Should permanently delete application and return success")
        void permanentlyDeleteApplication_returnsSuccess() {
            // Given
            doNothing().when(applicationService).permanentlyDeleteApplication(5L);

            // When
            ApiResponse<Void> result = controller.permanentlyDeleteApplication(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("permanently deleted");
            verify(applicationService).permanentlyDeleteApplication(5L);
        }
    }

    // ============================================================
    // DELETE /students/{studentId}/revoke - revokeKuppiStudentRole
    // ============================================================

    @Nested
    @DisplayName("DELETE /students/{studentId}/revoke - revokeKuppiStudentRole")
    class RevokeKuppiStudentRoleTests {

        @Test
        @DisplayName("Should revoke kuppi student role and return success")
        void revokeKuppiStudentRole_returnsSuccess() {
            // Given
            doNothing().when(applicationService).revokeKuppiStudentRole(10L, "Misconduct");

            // When
            ApiResponse<Void> result = controller.revokeKuppiStudentRole(10L, "Misconduct");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("revoked");
            verify(applicationService).revokeKuppiStudentRole(10L, "Misconduct");
        }
    }

    // ============================================================
    // DELETE /sessions/{sessionId}/permanent - permanentlyDeleteSession
    // ============================================================

    @Nested
    @DisplayName("DELETE /sessions/{sessionId}/permanent - permanentlyDeleteSession")
    class PermanentlyDeleteSessionTests {

        @Test
        @DisplayName("Should permanently delete session and return success")
        void permanentlyDeleteSession_returnsSuccess() {
            // Given
            doNothing().when(sessionService).permanentlyDeleteSession(5L);

            // When
            ApiResponse<Void> result = controller.permanentlyDeleteSession(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("permanently deleted");
            verify(sessionService).permanentlyDeleteSession(5L);
        }
    }

    // ============================================================
    // DELETE /notes/{noteId}/permanent - permanentlyDeleteNote
    // ============================================================

    @Nested
    @DisplayName("DELETE /notes/{noteId}/permanent - permanentlyDeleteNote")
    class PermanentlyDeleteNoteTests {

        @Test
        @DisplayName("Should permanently delete note and return success")
        void permanentlyDeleteNote_returnsSuccess() {
            // Given
            doNothing().when(noteService).permanentlyDeleteNote(3L);

            // When
            ApiResponse<Void> result = controller.permanentlyDeleteNote(3L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("permanently deleted");
            verify(noteService).permanentlyDeleteNote(3L);
        }
    }
}
