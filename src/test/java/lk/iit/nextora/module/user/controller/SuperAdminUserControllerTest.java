package lk.iit.nextora.module.user.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminUserController Unit Tests")
class SuperAdminUserControllerTest {

    @Mock private SuperAdminUserService superAdminUserService;

    @InjectMocks
    private SuperAdminUserController controller;

    // ============================================================
    // POST /admin - createAdminUser
    // ============================================================

    @Nested
    @DisplayName("POST /admin - createAdminUser")
    class CreateAdminTests {

        @Test
        @DisplayName("Should return created admin profile with all fields")
        void createAdmin_validRequest_returnsProfile() {
            // Given
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("admin@iit.ac.lk").password("Pass@1234")
                    .firstName("Admin").lastName("User")
                    .adminId("ADM001").department("IT").build();
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(1L).email("admin@iit.ac.lk")
                    .firstName("Admin").lastName("User")
                    .role(UserRole.ROLE_ADMIN).build();
            when(superAdminUserService.createAdminUser(any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.createAdminUser(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Admin user created");
            assertThat(result.getData().getId()).isEqualTo(1L);
            assertThat(result.getData().getEmail()).isEqualTo("admin@iit.ac.lk");
            verify(superAdminUserService).createAdminUser(request);
        }

        @Test
        @DisplayName("Should propagate duplicate email exception")
        void createAdmin_duplicateEmail_propagatesException() {
            // Given
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("existing@iit.ac.lk").password("Pass@1234")
                    .firstName("A").lastName("B")
                    .adminId("ADM002").department("IT").build();
            when(superAdminUserService.createAdminUser(any()))
                    .thenThrow(new lk.iit.nextora.common.exception.custom.BadRequestException("Email already registered"));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.BadRequestException.class,
                    () -> controller.createAdminUser(request));
        }
    }

    // ============================================================
    // GET / - getAllAdminUsers
    // ============================================================

    @Nested
    @DisplayName("GET / - getAllAdminUsers")
    class GetAllAdminsTests {

        @Test
        @DisplayName("Should return paginated admin users")
        void getAllAdmins_returnsPaginatedResult() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of(
                            UserSummaryResponse.builder().id(1L).email("admin1@iit.ac.lk").role(UserRole.ROLE_ADMIN).build(),
                            UserSummaryResponse.builder().id(2L).email("admin2@iit.ac.lk").role(UserRole.ROLE_ADMIN).build()
                    ))
                    .totalElements(2L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false).build();
            when(superAdminUserService.getAllAdminUsers(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<UserSummaryResponse>> result =
                    controller.getAllAdminUsers(0, 10, "createdAt", "DESC");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(2);
            assertThat(result.getData().getTotalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle empty admin list")
        void getAllAdmins_emptyResult_returnsEmptyPage() {
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
            assertThat(result.getData().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should pass correct sort parameters")
        void getAllAdmins_customSort_passesCorrectPageable() {
            // Given
            PagedResponse<UserSummaryResponse> paged = PagedResponse.<UserSummaryResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(1).pageSize(5)
                    .totalPages(0).first(false).last(true).empty(true).build();
            when(superAdminUserService.getAllAdminUsers(any())).thenReturn(paged);

            // When
            controller.getAllAdminUsers(1, 5, "email", "ASC");

            // Then
            verify(superAdminUserService).getAllAdminUsers(argThat(pageable ->
                    pageable.getPageNumber() == 1 &&
                    pageable.getPageSize() == 5
            ));
        }
    }

    // ============================================================
    // GET /admin/{id}
    // ============================================================

    @Nested
    @DisplayName("GET /admin/{id} - getAdminUserById")
    class GetAdminByIdTests {

        @Test
        @DisplayName("Should return admin profile")
        void getAdminById_returnsProfile() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(5L).email("admin@iit.ac.lk")
                    .role(UserRole.ROLE_ADMIN).build();
            when(superAdminUserService.getAdminUserById(5L)).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.getAdminUserById(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getId()).isEqualTo(5L);
            assertThat(result.getData().getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        }

        @Test
        @DisplayName("Should propagate ResourceNotFoundException for non-existent admin")
        void getAdminById_notFound_propagatesException() {
            // Given
            when(superAdminUserService.getAdminUserById(999L))
                    .thenThrow(new lk.iit.nextora.common.exception.custom.ResourceNotFoundException("Admin user not found", "id", 999L));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.ResourceNotFoundException.class,
                    () -> controller.getAdminUserById(999L));
        }
    }

    // ============================================================
    // PUT /admin/{id}
    // ============================================================

    @Nested
    @DisplayName("PUT /admin/{id} - updateAdminUser")
    class UpdateAdminTests {

        @Test
        @DisplayName("Should call service and return updated profile")
        void updateAdmin_returnsUpdatedProfile() {
            // Given
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .firstName("Updated").lastName("Admin")
                    .department("Engineering").build();
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(5L).firstName("Updated").lastName("Admin").build();
            when(superAdminUserService.updateAdminUser(eq(5L), any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.updateAdminUser(5L, request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getFirstName()).isEqualTo("Updated");
            verify(superAdminUserService).updateAdminUser(5L, request);
        }

        @Test
        @DisplayName("Should pass permissions in update request")
        void updateAdmin_withPermissions_delegatesToService() {
            // Given
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .permissions(Set.of("USER:READ", "USER:CREATE")).build();
            UserProfileResponse profile = UserProfileResponse.builder().id(5L).build();
            when(superAdminUserService.updateAdminUser(eq(5L), any())).thenReturn(profile);

            // When
            controller.updateAdminUser(5L, request);

            // Then
            verify(superAdminUserService).updateAdminUser(eq(5L), argThat(req ->
                    req.getPermissions() != null &&
                    req.getPermissions().contains("USER:READ") &&
                    req.getPermissions().contains("USER:CREATE")
            ));
        }
    }

    // ============================================================
    // DELETE /admin/{id}
    // ============================================================

    @Nested
    @DisplayName("DELETE /admin/{id} - softDeleteAdminUser")
    class SoftDeleteAdminTests {

        @Test
        @DisplayName("Should call service and return success")
        void softDeleteAdmin_returnsSuccess() {
            // Given
            doNothing().when(superAdminUserService).softDeleteAdminUser(5L);

            // When
            ApiResponse<Void> result = controller.softDeleteAdminUser(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("soft deleted");
            verify(superAdminUserService).softDeleteAdminUser(5L);
        }
    }

    // ============================================================
    // DELETE /admin/{id}/permanent
    // ============================================================

    @Nested
    @DisplayName("DELETE /admin/{id}/permanent - permanentlyDeleteAdminUser")
    class PermanentDeleteTests {

        @Test
        @DisplayName("Should call service and return success")
        void permanentDelete_returnsSuccess() {
            // Given
            doNothing().when(superAdminUserService).permanentlyDeleteUser(5L);

            // When
            ApiResponse<Void> result = controller.permanentlyDeleteAdminUser(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("permanently deleted");
            verify(superAdminUserService).permanentlyDeleteUser(5L);
        }
    }

    // ============================================================
    // PUT /admin/{id}/restore
    // ============================================================

    @Nested
    @DisplayName("PUT /admin/{id}/restore - restoreAdminUser")
    class RestoreAdminTests {

        @Test
        @DisplayName("Should call service and return success")
        void restoreAdmin_returnsSuccess() {
            // Given
            doNothing().when(superAdminUserService).restoreUser(5L);

            // When
            ApiResponse<Void> result = controller.restoreAdminUser(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("restored");
            verify(superAdminUserService).restoreUser(5L);
        }
    }

    // ============================================================
    // PUT /normal/{id}/restore
    // ============================================================

    @Nested
    @DisplayName("PUT /normal/{id}/restore - restoreNormalUser")
    class RestoreNormalUserTests {

        @Test
        @DisplayName("Should delegate to restoreUser service with correct ID")
        void restoreNormalUser_delegatesToService() {
            // Given
            doNothing().when(superAdminUserService).restoreUser(10L);

            // When
            ApiResponse<Void> result = controller.restoreNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("restored");
            verify(superAdminUserService).restoreUser(10L);
        }
    }

    // ============================================================
    // DELETE /normal/{id}/permanent
    // ============================================================

    @Nested
    @DisplayName("DELETE /normal/{id}/permanent - permanentlyDeleteNormalUser")
    class PermanentDeleteNormalTests {

        @Test
        @DisplayName("Should delegate to permanentlyDeleteUser service with correct ID")
        void permanentDeleteNormal_delegatesToService() {
            // Given
            doNothing().when(superAdminUserService).permanentlyDeleteUser(10L);

            // When
            ApiResponse<Void> result = controller.permanentlyDeleteNormalUser(10L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("permanently deleted");
            verify(superAdminUserService).permanentlyDeleteUser(10L);
        }
    }
}
