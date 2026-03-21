package lk.iit.nextora.module.kuppi.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiApplicationController Unit Tests")
class KuppiApplicationControllerTest {

    @Mock private KuppiApplicationService applicationService;

    @InjectMocks
    private KuppiApplicationController controller;

    // ============================================================
    // POST / - submitApplication
    // ============================================================

    @Nested
    @DisplayName("POST / - submitApplication")
    class SubmitApplicationTests {

        @Test
        @DisplayName("Should submit application and return response")
        void submitApplication_returnsCreated() {
            // Given
            KuppiApplicationRequest request = KuppiApplicationRequest.builder()
                    .motivation("I want to help other students")
                    .currentGpa(3.8)
                    .build();
            MockMultipartFile file = new MockMultipartFile(
                    "academicResults", "results.pdf", "application/pdf", "pdf content".getBytes());
            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(1L).motivation("I want to help other students").build();
            when(applicationService.submitApplication(any(), any())).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.submitApplication(request, file);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getMessage()).contains("submitted");
            verify(applicationService).submitApplication(request, file);
        }

        @Test
        @DisplayName("Should propagate BadRequestException from service")
        void submitApplication_alreadyKuppiStudent_propagatesException() {
            // Given
            KuppiApplicationRequest request = KuppiApplicationRequest.builder()
                    .motivation("motivation").currentGpa(3.5).build();
            MockMultipartFile file = new MockMultipartFile(
                    "academicResults", "results.pdf", "application/pdf", "pdf".getBytes());
            when(applicationService.submitApplication(any(), any()))
                    .thenThrow(new lk.iit.nextora.common.exception.custom.BadRequestException(
                            "You are already a Kuppi Student"));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.BadRequestException.class,
                    () -> controller.submitApplication(request, file));
        }
    }

    // ============================================================
    // GET MY APPLICATIONS
    // ============================================================

    @Nested
    @DisplayName("GET /my")
    class GetMyApplicationsTests {

        @Test
        @DisplayName("Should return list of current user's applications")
        void getMyApplications_returnsList() {
            // Given
            List<KuppiApplicationResponse> apps = List.of(
                    KuppiApplicationResponse.builder().id(1L).build());
            when(applicationService.getMyApplications()).thenReturn(apps);

            // When
            ApiResponse<List<KuppiApplicationResponse>> result = controller.getMyApplications();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).hasSize(1);
        }
    }

    // ============================================================
    // GET MY ACTIVE APPLICATION
    // ============================================================

    @Nested
    @DisplayName("GET /my/active")
    class GetMyActiveApplicationTests {

        @Test
        @DisplayName("Should return active application if exists")
        void getMyActiveApplication_returnsApplication() {
            // Given
            KuppiApplicationResponse response = KuppiApplicationResponse.builder().id(1L).build();
            when(applicationService.getMyActiveApplication()).thenReturn(response);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.getMyActiveApplication();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return success with null data when no active application")
        void getMyActiveApplication_noActive_returnsNull() {
            // Given
            when(applicationService.getMyActiveApplication()).thenReturn(null);

            // When
            ApiResponse<KuppiApplicationResponse> result = controller.getMyActiveApplication();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNull();
        }
    }

    // ============================================================
    // CANCEL MY APPLICATION
    // ============================================================

    @Nested
    @DisplayName("DELETE /{applicationId}")
    class CancelMyApplicationTests {

        @Test
        @DisplayName("Should cancel application and return success")
        void cancelMyApplication_returnsSuccess() {
            // Given
            doNothing().when(applicationService).cancelMyApplication(1L);

            // When
            ApiResponse<Void> result = controller.cancelMyApplication(1L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(applicationService).cancelMyApplication(1L);
        }
    }

    // ============================================================
    // CAN APPLY
    // ============================================================

    @Nested
    @DisplayName("GET /can-apply")
    class CanApplyTests {

        @Test
        @DisplayName("Should return true when student can apply")
        void canApply_eligible_returnsTrue() {
            // Given
            when(applicationService.canApply()).thenReturn(true);

            // When
            ApiResponse<Map<String, Boolean>> result = controller.canApply();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().get("canApply")).isTrue();
        }

        @Test
        @DisplayName("Should return false when student cannot apply")
        void canApply_notEligible_returnsFalse() {
            // Given
            when(applicationService.canApply()).thenReturn(false);

            // When
            ApiResponse<Map<String, Boolean>> result = controller.canApply();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().get("canApply")).isFalse();
        }
    }

    // ============================================================
    // IS KUPPI STUDENT
    // ============================================================

    @Nested
    @DisplayName("GET /is-kuppi-student")
    class IsKuppiStudentTests {

        @Test
        @DisplayName("Should return true when user is kuppi student")
        void isKuppiStudent_yes_returnsTrue() {
            // Given
            when(applicationService.isKuppiStudent()).thenReturn(true);

            // When
            ApiResponse<Map<String, Boolean>> result = controller.isKuppiStudent();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().get("isKuppiStudent")).isTrue();
        }
    }
}
