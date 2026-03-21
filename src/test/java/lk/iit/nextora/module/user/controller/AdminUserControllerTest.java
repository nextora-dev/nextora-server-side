package lk.iit.nextora.module.user.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.AdminUserService;
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
@DisplayName("AdminUserController Unit Tests")
class AdminUserControllerTest {

    @Mock private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController controller;

    @Nested
    @DisplayName("GET /stats")
    class GetStatsTests {

        @Test
        @DisplayName("Should return stats summary")
        void getUserStats_returnsSuccess() {
            // Given
            UserStatsSummaryResponse stats = UserStatsSummaryResponse.builder()
                    .totalUsers(100).activeUsers(80).totalStudents(60).build();
            when(adminUserService.getUserStatsSummary()).thenReturn(stats);

            // When
            ApiResponse<UserStatsSummaryResponse> result = controller.getUserStatsSummary();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTotalUsers()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("GET /")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users")
        void getAllNormalUsers_returnsPaginatedResult() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of(UserSummaryResponse.builder().id(1L).build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false)
                    .build();
            when(adminUserService.getAllNormalUsers(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.getAllNormalUsers(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("GET /{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user profile by ID")
        void getNormalUserById_returnsProfile() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(10L).email("s@iit.ac.lk").build();
            when(adminUserService.getNormalUserById(10L)).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.getNormalUserById(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("PUT /{id}/activate")
    class ActivateTests {

        @Test
        @DisplayName("Should call service and return success")
        void activateUser_returnsSuccess() {
            doNothing().when(adminUserService).activateNormalUser(10L);

            ApiResponse<Void> result = controller.activateNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).activateNormalUser(10L);
        }
    }

    @Nested
    @DisplayName("PUT /{id}/deactivate")
    class DeactivateTests {

        @Test
        @DisplayName("Should call service and return success")
        void deactivateUser_returnsSuccess() {
            doNothing().when(adminUserService).deactivateNormalUser(10L);

            ApiResponse<Void> result = controller.deactivateNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).deactivateNormalUser(10L);
        }
    }

    @Nested
    @DisplayName("PUT /{id}/suspend")
    class SuspendTests {

        @Test
        @DisplayName("Should pass reason to service")
        void suspendUser_withReason_callsService() {
            doNothing().when(adminUserService).suspendNormalUser(anyLong(), anyString());

            ApiResponse<Void> result = controller.suspendNormalUser(10L, "Policy violation");

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).suspendNormalUser(10L, "Policy violation");
        }
    }

    @Nested
    @DisplayName("PUT /{id}/unlock")
    class UnlockTests {

        @Test
        @DisplayName("Should call service and return success")
        void unlockUser_returnsSuccess() {
            doNothing().when(adminUserService).unlockNormalUser(10L);

            ApiResponse<Void> result = controller.unlockNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).unlockNormalUser(10L);
        }
    }

    @Nested
    @DisplayName("DELETE /{id}")
    class DeleteTests {

        @Test
        @DisplayName("Should call service and return success")
        void deleteUser_returnsSuccess() {
            doNothing().when(adminUserService).deleteNormalUser(10L);

            ApiResponse<Void> result = controller.deleteNormalUser(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).deleteNormalUser(10L);
        }
    }

    @Nested
    @DisplayName("PUT /{id}/reset-password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should call service and return success")
        void resetPassword_returnsSuccess() {
            doNothing().when(adminUserService).resetNormalUserPassword(10L);

            ApiResponse<Void> result = controller.resetNormalUserPassword(10L);

            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).resetNormalUserPassword(10L);
        }
    }
}