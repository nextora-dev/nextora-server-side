package lk.iit.nextora.module.user.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.SuperAdminUserService;
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
@DisplayName("SuperAdminUserController Unit Tests")
class SuperAdminUserControllerTest {

    @Mock private SuperAdminUserService superAdminUserService;

    @InjectMocks
    private SuperAdminUserController controller;

    @Nested
    @DisplayName("POST /admin")
    class CreateAdminTests {

        @Test
        @DisplayName("Should return created admin profile")
        void createAdmin_validRequest_returnsProfile() {
            // Given
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("admin@iit.ac.lk").password("Pass@1234")
                    .firstName("A").lastName("B")
                    .adminId("ADM001").department("IT").build();
            UserProfileResponse profile = UserProfileResponse.builder().id(1L).build();
            when(superAdminUserService.createAdminUser(any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.createAdminUser(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("GET /")
    class GetAllAdminsTests {

        @Test
        @DisplayName("Should return paginated admin users")
        void getAllAdmins_returnsPaginatedResult() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(superAdminUserService.getAllAdminUsers(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.getAllAdminUsers(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /admin/{id}")
    class GetAdminByIdTests {

        @Test
        @DisplayName("Should return admin profile")
        void getAdminById_returnsProfile() {
            UserProfileResponse profile = UserProfileResponse.builder().id(5L).build();
            when(superAdminUserService.getAdminUserById(5L)).thenReturn(profile);

            ApiResponse<UserProfileResponse> result = controller.getAdminUserById(5L);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("PUT /admin/{id}")
    class UpdateAdminTests {

        @Test
        @DisplayName("Should call service and return updated profile")
        void updateAdmin_returnsUpdatedProfile() {
            UpdateAdminRequest request = UpdateAdminRequest.builder().firstName("Updated").build();
            UserProfileResponse profile = UserProfileResponse.builder().id(5L).build();
            when(superAdminUserService.updateAdminUser(eq(5L), any())).thenReturn(profile);

            ApiResponse<UserProfileResponse> result = controller.updateAdminUser(5L, request);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("DELETE /admin/{id}")
    class SoftDeleteAdminTests {

        @Test
        @DisplayName("Should call service and return success")
        void softDeleteAdmin_returnsSuccess() {
            doNothing().when(superAdminUserService).softDeleteAdminUser(5L);

            ApiResponse<Void> result = controller.softDeleteAdminUser(5L);

            assertThat(result.isSuccess()).isTrue();
            verify(superAdminUserService).softDeleteAdminUser(5L);
        }
    }

    @Nested
    @DisplayName("DELETE /admin/{id}/permanent")
    class PermanentDeleteTests {

        @Test
        @DisplayName("Should call service and return success")
        void permanentDelete_returnsSuccess() {
            doNothing().when(superAdminUserService).permanentlyDeleteUser(5L);

            ApiResponse<Void> result = controller.permanentlyDeleteAdminUser(5L);

            assertThat(result.isSuccess()).isTrue();
            verify(superAdminUserService).permanentlyDeleteUser(5L);
        }
    }

    @Nested
    @DisplayName("PUT /admin/{id}/restore")
    class RestoreAdminTests {

        @Test
        @DisplayName("Should call service and return success")
        void restoreAdmin_returnsSuccess() {
            doNothing().when(superAdminUserService).restoreUser(5L);

            ApiResponse<Void> result = controller.restoreAdminUser(5L);

            assertThat(result.isSuccess()).isTrue();
            verify(superAdminUserService).restoreUser(5L);
        }
    }

    @Nested
    @DisplayName("PUT /normal/{id}/restore")
    class RestoreNormalUserTests {

        @Test
        @DisplayName("Should delegate to restoreUser service")
        void restoreNormalUser_delegatesToService() {
            doNothing().when(superAdminUserService).restoreUser(10L);

            ApiResponse<Void> result = controller.restoreNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(superAdminUserService).restoreUser(10L);
        }
    }

    @Nested
    @DisplayName("DELETE /normal/{id}/permanent")
    class PermanentDeleteNormalTests {

        @Test
        @DisplayName("Should delegate to permanentlyDeleteUser service")
        void permanentDeleteNormal_delegatesToService() {
            doNothing().when(superAdminUserService).permanentlyDeleteUser(10L);

            ApiResponse<Void> result = controller.permanentlyDeleteNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(superAdminUserService).permanentlyDeleteUser(10L);
        }
    }
}