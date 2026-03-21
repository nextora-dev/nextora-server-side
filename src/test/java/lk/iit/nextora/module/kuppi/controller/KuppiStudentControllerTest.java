package lk.iit.nextora.module.kuppi.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentDetailResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentResponse;
import lk.iit.nextora.module.kuppi.service.KuppiStudentService;
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
@DisplayName("KuppiStudentController Unit Tests")
class KuppiStudentControllerTest {

    @Mock private KuppiStudentService kuppiStudentService;

    @InjectMocks
    private KuppiStudentController controller;

    // ============================================================
    // GET ALL KUPPI STUDENTS
    // ============================================================

    @Nested
    @DisplayName("GET /")
    class GetAllKuppiStudentsTests {

        @Test
        @DisplayName("Should return paginated kuppi students")
        void getAllKuppiStudents_returnsPaginatedResult() {
            // Given
            PagedResponse<KuppiStudentResponse> paged = PagedResponse.<KuppiStudentResponse>builder()
                    .content(List.of(KuppiStudentResponse.builder().id(1L).fullName("John Doe").build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false)
                    .build();
            when(kuppiStudentService.getAllKuppiStudents(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiStudentResponse>> result =
                    controller.getAllKuppiStudents(0, 10, "kuppiRating", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
        }
    }

    // ============================================================
    // GET KUPPI STUDENT BY ID
    // ============================================================

    @Nested
    @DisplayName("GET /{studentId}")
    class GetKuppiStudentByIdTests {

        @Test
        @DisplayName("Should return detailed kuppi student profile")
        void getKuppiStudentById_returnsDetailedResponse() {
            // Given
            KuppiStudentDetailResponse detail = KuppiStudentDetailResponse.builder()
                    .id(5L).fullName("Jane Smith").totalSessionsHosted(10L).build();
            when(kuppiStudentService.getKuppiStudentById(5L)).thenReturn(detail);

            // When
            ApiResponse<KuppiStudentDetailResponse> result = controller.getKuppiStudentById(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getFullName()).isEqualTo("Jane Smith");
            assertThat(result.getData().getTotalSessionsHosted()).isEqualTo(10L);
        }
    }

    // ============================================================
    // SEARCH BY NAME
    // ============================================================

    @Nested
    @DisplayName("GET /search/name")
    class SearchByNameTests {

        @Test
        @DisplayName("Should search kuppi students by name")
        void searchByName_delegatesToService() {
            // Given
            PagedResponse<KuppiStudentResponse> paged = PagedResponse.<KuppiStudentResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(kuppiStudentService.searchKuppiStudentsByName(eq("John"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiStudentResponse>> result =
                    controller.searchByName("John", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(kuppiStudentService).searchKuppiStudentsByName(eq("John"), any());
        }
    }

    // ============================================================
    // SEARCH BY SUBJECT
    // ============================================================

    @Nested
    @DisplayName("GET /search/subject")
    class SearchBySubjectTests {

        @Test
        @DisplayName("Should search kuppi students by subject")
        void searchBySubject_delegatesToService() {
            // Given
            PagedResponse<KuppiStudentResponse> paged = PagedResponse.<KuppiStudentResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(kuppiStudentService.searchKuppiStudentsBySubject(eq("Math"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiStudentResponse>> result =
                    controller.searchBySubject("Math", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(kuppiStudentService).searchKuppiStudentsBySubject(eq("Math"), any());
        }
    }

    // ============================================================
    // GET BY FACULTY
    // ============================================================

    @Nested
    @DisplayName("GET /faculty/{faculty}")
    class GetByFacultyTests {

        @Test
        @DisplayName("Should return kuppi students by faculty")
        void getByFaculty_delegatesToService() {
            // Given
            PagedResponse<KuppiStudentResponse> paged = PagedResponse.<KuppiStudentResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(kuppiStudentService.getKuppiStudentsByFaculty(eq("COMPUTING"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiStudentResponse>> result =
                    controller.getByFaculty("COMPUTING", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(kuppiStudentService).getKuppiStudentsByFaculty(eq("COMPUTING"), any());
        }
    }
}