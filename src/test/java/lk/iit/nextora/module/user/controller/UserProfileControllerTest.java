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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileController Unit Tests")
class UserProfileControllerTest {

    @Mock private UserService userService;

    @InjectMocks
    private UserProfileController controller;

    @Nested
    @DisplayName("GET /me")
    class GetCurrentProfileTests {

        @Test
        @DisplayName("Should return success with profile data")
        void getCurrentUserProfile_returnsSuccess() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder()
                    .id(1L).email("user@iit.ac.lk").firstName("John").lastName("Doe")
                    .role(UserRole.ROLE_STUDENT).build();
            when(userService.getCurrentUserProfile()).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.getCurrentUserProfile();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getEmail()).isEqualTo("user@iit.ac.lk");
        }
    }

    @Nested
    @DisplayName("PUT /me")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should call service with constructed UpdateProfileRequest")
        void updateProfile_callsServiceCorrectly() {
            // Given
            UserProfileResponse profile = UserProfileResponse.builder().id(1L).build();
            when(userService.updateCurrentUserProfile(any(), any(), any())).thenReturn(profile);

            // When
            ApiResponse<UserProfileResponse> result = controller.updateCurrentUserProfile(
                    "Jane", "Smith", "+94771234567",
                    "Address", null, null, null, null, false);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(userService).updateCurrentUserProfile(
                    argThat(req ->
                            "Jane".equals(req.getFirstName()) &&
                            "Smith".equals(req.getLastName()) &&
                            "+94771234567".equals(req.getPhone()) &&
                            "Address".equals(req.getAddress())
                    ), isNull(), eq(false));
        }
    }

    @Nested
    @DisplayName("PUT /me/password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should call service and return success")
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
            verify(userService).changePassword(request);
        }
    }
}