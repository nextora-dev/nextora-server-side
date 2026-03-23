package lk.iit.nextora.module.user.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
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

    // ============================================================
    // POST / - createNormalUser
    // ============================================================

    @Nested
    @DisplayName("POST / - createNormalUser")
    class CreateNormalUserTests {

        @Test
        @DisplayName("Should delegate to service and return created user response")
        void createNormalUser_validRequest_returnsCreatedResponse() {
            // Given
            AdminCreateUserRequest request = mock(AdminCreateUserRequest.class);
            UserCreatedResponse response = UserCreatedResponse.builder()
                    .id(1L).email("student@iit.ac.lk")
                    .firstName("John").lastName("Doe")
                    .role(UserRole.ROLE_STUDENT)
                    .status(UserStatus.PASSWORD_CHANGE_REQUIRED)
                    .message("User created successfully")
                    .build();
            when(adminUserService.createNormalUser(any())).thenReturn(response);

            // When
            ApiResponse<UserCreatedResponse> result = controller.createNormalUser(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getEmail()).isEqualTo("student@iit.ac.lk");
            assertThat(result.getData().getRole()).isEqualTo(UserRole.ROLE_STUDENT);
            assertThat(result.getData().getStatus()).isEqualTo(UserStatus.PASSWORD_CHANGE_REQUIRED);
            verify(adminUserService).createNormalUser(request);
        }
    }

    // ============================================================
    // GET /stats
    // ============================================================

    @Nested
    @DisplayName("GET /stats - getUserStatsSummary")
    class GetStatsTests {

        @Test
        @DisplayName("Should return stats summary with all counts populated")
        void getUserStats_returnsCompleteStats() {
            // Given
            UserStatsSummaryResponse stats = UserStatsSummaryResponse.builder()
                    .totalUsers(100).activeUsers(80).deactivatedUsers(5)
                    .suspendedUsers(3).deletedUsers(2).passwordChangeRequiredUsers(10)
                    .totalStudents(60).totalAcademicStaff(25).totalNonAcademicStaff(15)
                    .build();
            when(adminUserService.getUserStatsSummary()).thenReturn(stats);

            // When
            ApiResponse<UserStatsSummaryResponse> result = controller.getUserStatsSummary();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTotalUsers()).isEqualTo(100);
            assertThat(result.getData().getActiveUsers()).isEqualTo(80);
            assertThat(result.getData().getDeactivatedUsers()).isEqualTo(5);
            assertThat(result.getData().getSuspendedUsers()).isEqualTo(3);
            assertThat(result.getData().getDeletedUsers()).isEqualTo(2);
            assertThat(result.getData().getTotalStudents()).isEqualTo(60);
            verify(adminUserService, times(1)).getUserStatsSummary();
        }
    }

    // ============================================================
    // GET / - getAllNormalUsers
    // ============================================================

    @Nested
    @DisplayName("GET / - getAllNormalUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users with correct metadata")
        void getAllNormalUsers_returnsPaginatedResult() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of(
                            UserSummaryResponse.builder().id(1L).email("a@iit.ac.lk").build(),
                            UserSummaryResponse.builder().id(2L).email("b@iit.ac.lk").build()
                    ))
                    .totalElements(2L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false)
                    .build();
            when(adminUserService.getAllNormalUsers(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.getAllNormalUsers(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(2);
            assertThat(result.getData().getTotalElements()).isEqualTo(2L);
            assertThat(result.getData().isFirst()).isTrue();
            assertThat(result.getData().isLast()).isTrue();
        }

        @Test
        @DisplayName("Should pass correct Pageable with sort parameters to service")
        void getAllNormalUsers_passesCorrectPageable() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(2).pageSize(5)
                    .totalPages(0).first(false).last(true).empty(true).build();
            when(adminUserService.getAllNormalUsers(any())).thenReturn(paged);

            // When
            controller.getAllNormalUsers(2, 5, "email", "ASC");

            // Then
            verify(adminUserService).getAllNormalUsers(argThat(pageable ->
                    pageable.getPageNumber() == 2 &&
                    pageable.getPageSize() == 5 &&
                    pageable.getSort().getOrderFor("email") != null &&
                    pageable.getSort().getOrderFor("email").isAscending()
            ));
        }

        @Test
        @DisplayName("Should handle empty result set correctly")
        void getAllNormalUsers_emptyResult_returnsEmptyPage() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(adminUserService.getAllNormalUsers(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.getAllNormalUsers(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).isEmpty();
            assertThat(result.getData().isEmpty()).isTrue();
        }
    }

    // ============================================================
    // GET /search - searchNormalUsers
    // ============================================================

    @Nested
    @DisplayName("GET /search - searchNormalUsers")
    class SearchUsersTests {

        @Test
        @DisplayName("Should pass keyword and pageable to service")
        void searchNormalUsers_passesKeywordAndPageable() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of(UserSummaryResponse.builder().id(1L).email("john@iit.ac.lk").build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false).build();
            when(adminUserService.searchNormalUsers(anyString(), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.searchNormalUsers("john", 0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
            verify(adminUserService).searchNormalUsers(eq("john"), any());
        }
    }

    // ============================================================
    // GET /filter - filterNormalUsers
    // ============================================================

    @Nested
    @DisplayName("GET /filter - filterNormalUsers")
    class FilterUsersTests {

        @Test
        @DisplayName("Should pass roles and statuses to service")
        void filterNormalUsers_withRolesAndStatuses_delegatesToService() {
            // Given
            List<UserRole> roles = List.of(UserRole.ROLE_STUDENT);
            List<UserStatus> statuses = List.of(UserStatus.ACTIVE, UserStatus.SUSPENDED);
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(adminUserService.filterNormalUsers(any(), any(), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.filterNormalUsers(roles, statuses, 0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).filterNormalUsers(eq(roles), eq(statuses), any());
        }

        @Test
        @DisplayName("Should allow null roles and statuses")
        void filterNormalUsers_nullFilters_delegatesToService() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(adminUserService.filterNormalUsers(any(), any(), any())).thenReturn(paged);

            // When
            controller.filterNormalUsers(null, null, 0, 10, "createdAt", "DESC");

            // Then
            verify(adminUserService).filterNormalUsers(isNull(), isNull(), any());
        }
    }

    // ============================================================
    // GET /{id}
    // ============================================================

    @Nested
    @DisplayName("GET /{id} - getNormalUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user profile by ID")
        void getNormalUserById_returnsProfile() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(10L).email("student@iit.ac.lk")
                    .firstName("John").lastName("Doe").build();
            when(adminUserService.getNormalUserById(10L)).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.getNormalUserById(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(10L);
            assertThat(result.getData().getEmail()).isEqualTo("student@iit.ac.lk");
        }

        @Test
        @DisplayName("Should propagate ResourceNotFoundException for non-existent user")
        void getNormalUserById_notFound_propagatesException() {
            // Given
            when(adminUserService.getNormalUserById(999L))
                    .thenThrow(new lk.iit.nextora.common.exception.custom.ResourceNotFoundException("User not found", "id", 999L));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.ResourceNotFoundException.class,
                    () -> controller.getNormalUserById(999L));
        }
    }

    // ============================================================
    // PUT /{id} - updateNormalUserById
    // ============================================================

    @Nested
    @DisplayName("PUT /{id} - updateNormalUserById")
    class UpdateUserByIdTests {

        @Test
        @DisplayName("Should delegate to service with request and return updated profile")
        void updateNormalUserById_validRequest_returnsUpdatedProfile() {
            // Given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Updated").lastName("User").build();
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(10L).firstName("Updated").lastName("User").build();
            when(adminUserService.updateNormalUserById(eq(10L), any(), isNull(), eq(false)))
                    .thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.updateNormalUserById(10L, request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getFirstName()).isEqualTo("Updated");
            verify(adminUserService).updateNormalUserById(eq(10L), eq(request), isNull(), eq(false));
        }
    }

    // ============================================================
    // PUT /{id}/activate
    // ============================================================

    @Nested
    @DisplayName("PUT /{id}/activate - activateNormalUser")
    class ActivateTests {

        @Test
        @DisplayName("Should call service and return success")
        void activateUser_returnsSuccess() {
            // Given
            doNothing().when(adminUserService).activateNormalUser(10L);

            // When
            ApiResponse<Void> result = controller.activateNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("activated");
            verify(adminUserService).activateNormalUser(10L);
        }
    }

    // ============================================================
    // PUT /{id}/deactivate
    // ============================================================

    @Nested
    @DisplayName("PUT /{id}/deactivate - deactivateNormalUser")
    class DeactivateTests {

        @Test
        @DisplayName("Should call service and return success")
        void deactivateUser_returnsSuccess() {
            // Given
            doNothing().when(adminUserService).deactivateNormalUser(10L);

            // When
            ApiResponse<Void> result = controller.deactivateNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("deactivated");
            verify(adminUserService).deactivateNormalUser(10L);
        }
    }

    // ============================================================
    // PUT /{id}/suspend
    // ============================================================

    @Nested
    @DisplayName("PUT /{id}/suspend - suspendNormalUser")
    class SuspendTests {

        @Test
        @DisplayName("Should pass reason to service")
        void suspendUser_withReason_callsService() {
            // Given
            doNothing().when(adminUserService).suspendNormalUser(anyLong(), anyString());

            // When
            ApiResponse<Void> result = controller.suspendNormalUser(10L, "Policy violation");

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).suspendNormalUser(10L, "Policy violation");
        }

        @Test
        @DisplayName("Should allow null reason")
        void suspendUser_nullReason_callsService() {
            // Given
            doNothing().when(adminUserService).suspendNormalUser(anyLong(), isNull());

            // When
            ApiResponse<Void> result = controller.suspendNormalUser(10L, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(adminUserService).suspendNormalUser(10L, null);
        }
    }

    // ============================================================
    // PUT /{id}/unlock
    // ============================================================

    @Nested
    @DisplayName("PUT /{id}/unlock - unlockNormalUser")
    class UnlockTests {

        @Test
        @DisplayName("Should call service and return success")
        void unlockUser_returnsSuccess() {
            // Given
            doNothing().when(adminUserService).unlockNormalUser(10L);

            // When
            ApiResponse<Void> result = controller.unlockNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("unlocked");
            verify(adminUserService).unlockNormalUser(10L);
        }
    }

    // ============================================================
    // DELETE /{id}
    // ============================================================

    @Nested
    @DisplayName("DELETE /{id} - deleteNormalUser")
    class DeleteTests {

        @Test
        @DisplayName("Should call service and return success")
        void deleteUser_returnsSuccess() {
            // Given
            doNothing().when(adminUserService).deleteNormalUser(10L);

            // When
            ApiResponse<Void> result = controller.deleteNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("deleted");
            verify(adminUserService).deleteNormalUser(10L);
        }
    }

    // ============================================================
    // PUT /{id}/reset-password
    // ============================================================

    @Nested
    @DisplayName("PUT /{id}/reset-password - resetNormalUserPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should call service and return success")
        void resetPassword_returnsSuccess() {
            // Given
            doNothing().when(adminUserService).resetNormalUserPassword(10L);

            // When
            ApiResponse<Void> result = controller.resetNormalUserPassword(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Password reset");
            verify(adminUserService).resetNormalUserPassword(10L);
        }
    }
}
