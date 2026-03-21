package lk.iit.nextora.module.user.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileController Unit Tests")
class UserProfileControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private UserProfileController controller;

    // ============================================================
    // GET /me
    // ============================================================

    @Nested
    @DisplayName("GET /me - getCurrentUserProfile")
    class GetCurrentProfileTests {

        @Test
        @DisplayName("Should return success response with complete profile data")
        void getCurrentUserProfile_returnsSuccessWithProfileData() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(1L).email("user@iit.ac.lk").firstName("John").lastName("Doe")
                    .role(UserRole.ROLE_STUDENT).build();
            when(userService.getCurrentUserProfile()).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.getCurrentUserProfile();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Profile retrieved");
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData().getEmail()).isEqualTo("user@iit.ac.lk");
            assertThat(result.getData().getFirstName()).isEqualTo("John");
            assertThat(result.getData().getLastName()).isEqualTo("Doe");
            assertThat(result.getData().getRole()).isEqualTo(UserRole.ROLE_STUDENT);
            verify(userService, times(1)).getCurrentUserProfile();
        }

        @Test
        @DisplayName("Should propagate service exceptions to caller")
        void getCurrentUserProfile_serviceThrows_propagatesException() {
            // Given
            when(userService.getCurrentUserProfile()).thenThrow(new RuntimeException("Unauthorized"));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> controller.getCurrentUserProfile());
        }
    }

    // ============================================================
    // PUT /me
    // ============================================================

    @Nested
    @DisplayName("PUT /me - updateCurrentUserProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should construct UpdateProfileRequest from individual params and delegate to service")
        void updateProfile_allParams_constructsRequestCorrectly() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(1L).firstName("Jane").lastName("Smith").build();
            when(userService.updateCurrentUserProfile(any(), any(), any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.updateCurrentUserProfile(
                    "Jane", "Smith", "+94771234567",
                    "123 Main St", "Guardian", "+94777654321",
                    LocalDate.of(2000, 1, 15), null, false);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Profile updated");
            verify(userService).updateCurrentUserProfile(
                    argThat(req ->
                            "Jane".equals(req.getFirstName()) &&
                            "Smith".equals(req.getLastName()) &&
                            "+94771234567".equals(req.getPhone()) &&
                            "123 Main St".equals(req.getAddress()) &&
                            "Guardian".equals(req.getGuardianName()) &&
                            "+94777654321".equals(req.getGuardianPhone()) &&
                            LocalDate.of(2000, 1, 15).equals(req.getDateOfBirth())
                    ), isNull(), eq(false));
        }

        @Test
        @DisplayName("Should pass null fields when optional params are omitted")
        void updateProfile_nullParams_passesNullsInRequest() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder().id(1L).build();
            when(userService.updateCurrentUserProfile(any(), any(), any())).thenReturn(profile);

            // When
            controller.updateCurrentUserProfile(
                    null, null, null, null, null, null, null, null, false);

            // Then
            verify(userService).updateCurrentUserProfile(
                    argThat(req ->
                            req.getFirstName() == null &&
                            req.getLastName() == null &&
                            req.getPhone() == null &&
                            req.getAddress() == null
                    ), isNull(), eq(false));
        }

        @Test
        @DisplayName("Should pass deleteProfilePicture=true to service")
        void updateProfile_deleteProfilePicture_passesFlag() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder().id(1L).build();
            when(userService.updateCurrentUserProfile(any(), any(), any())).thenReturn(profile);

            // When
            controller.updateCurrentUserProfile(
                    null, null, null, null, null, null, null, null, true);

            // Then
            verify(userService).updateCurrentUserProfile(any(), isNull(), eq(true));
        }

        @Test
        @DisplayName("Should return updated profile data in response")
        void updateProfile_returnsUpdatedProfile() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(1L).firstName("Updated").lastName("Name")
                    .email("user@iit.ac.lk").role(UserRole.ROLE_STUDENT).build();
            when(userService.updateCurrentUserProfile(any(), any(), any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.updateCurrentUserProfile(
                    "Updated", "Name", null, null, null, null, null, null, false);

            // Then
            assertThat(result.getData().getFirstName()).isEqualTo("Updated");
            assertThat(result.getData().getLastName()).isEqualTo("Name");
        }
    }

    // ============================================================
    // PUT /me/password
    // ============================================================

    @Nested
    @DisplayName("PUT /me/password - changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should delegate to service and return success with message")
        void changePassword_validRequest_returnsSuccess() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Old@123")
                    .newPassword("New@123")
                    .confirmPassword("New@123")
                    .build();
            doNothing().when(userService).changePassword(any());

            // When
            ApiResponse<Void> result = controller.changePassword(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("Password changed");
            assertThat(result.getData()).isNull();
            verify(userService, times(1)).changePassword(request);
        }

        @Test
        @DisplayName("Should pass exact request object to service without modification")
        void changePassword_passesExactRequest() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Current@Pass1")
                    .newPassword("Brand@NewPass1")
                    .confirmPassword("Brand@NewPass1")
                    .build();
            doNothing().when(userService).changePassword(any());

            // When
            controller.changePassword(request);

            // Then
            verify(userService).changePassword(argThat(req ->
                    "Current@Pass1".equals(req.getCurrentPassword()) &&
                    "Brand@NewPass1".equals(req.getNewPassword()) &&
                    "Brand@NewPass1".equals(req.getConfirmPassword())
            ));
        }

        @Test
        @DisplayName("Should propagate BadRequestException from service")
        void changePassword_serviceThrowsBadRequest_propagates() {
            // Given
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Wrong@123")
                    .newPassword("New@123")
                    .confirmPassword("New@123")
                    .build();
            doThrow(new lk.iit.nextora.common.exception.custom.BadRequestException("Current password is incorrect"))
                    .when(userService).changePassword(any());

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    lk.iit.nextora.common.exception.custom.BadRequestException.class,
                    () -> controller.changePassword(request));
        }
    }
}
