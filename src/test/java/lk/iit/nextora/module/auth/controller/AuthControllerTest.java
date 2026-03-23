package lk.iit.nextora.module.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.config.security.jwt.JwtBlacklistService;
import lk.iit.nextora.module.auth.dto.request.ForgotPasswordRequest;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.request.ResetPasswordRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.ForgotPasswordResponse;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import lk.iit.nextora.common.dto.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private JwtBlacklistService jwtBlacklistService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ============================================================
    // LOGIN ENDPOINT TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return success ApiResponse with AuthResponse data")
        void login_validRequest_returnsSuccess() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("user@iit.ac.lk");
            request.setPassword("Pass@1234");
            request.setRole(UserRole.ROLE_STUDENT);

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .tokenType("Bearer")
                    .userId(1L)
                    .email("user@iit.ac.lk")
                    .role(UserRole.ROLE_STUDENT)
                    .build();

            when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

            // When
            ApiResponse<AuthResponse> result = authController.login(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Login successful");
            assertThat(result.getData().getAccessToken()).isEqualTo("access-token");
            verify(authenticationService).login(request);
        }
    }

    // ============================================================
    // LOGOUT ENDPOINT TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /logout")
    class LogoutEndpointTests {

        @Test
        @DisplayName("Should blacklist token and return success")
        void logout_validBearerHeader_blacklistsToken() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer jwt-token-to-invalidate");

            // When
            ResponseEntity<ApiResponse<Void>> result = authController.logout(request);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).contains("Logged out");
            verify(jwtBlacklistService).blacklistToken("jwt-token-to-invalidate");
        }

        @Test
        @DisplayName("Should return 400 when Authorization header is missing")
        void logout_missingAuthHeader_returnsBadRequest() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();

            // When
            ResponseEntity<ApiResponse<Void>> result = authController.logout(request);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody().isSuccess()).isFalse();
            verify(jwtBlacklistService, never()).blacklistToken(anyString());
        }

        @Test
        @DisplayName("Should return 400 when Authorization header is not Bearer type")
        void logout_nonBearerHeader_returnsBadRequest() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            // When
            ResponseEntity<ApiResponse<Void>> result = authController.logout(request);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(jwtBlacklistService, never()).blacklistToken(anyString());
        }
    }

    // ============================================================
    // FORGOT PASSWORD ENDPOINT TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /forgot-password")
    class ForgotPasswordEndpointTests {

        @Test
        @DisplayName("Should return success with ForgotPasswordResponse")
        void forgotPassword_validEmail_returnsSuccess() {
            // Given
            ForgotPasswordRequest request = new ForgotPasswordRequest("user@iit.ac.lk", null);
            ForgotPasswordResponse fpResponse = ForgotPasswordResponse.builder()
                    .message("Password reset link sent to your email.")
                    .maskedEmail("u***r@iit.ac.lk")
                    .expiryMinutes(60)
                    .build();

            when(authenticationService.initiatePasswordReset(any())).thenReturn(fpResponse);

            // When
            ApiResponse<ForgotPasswordResponse> result = authController.forgotPassword(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getExpiryMinutes()).isEqualTo(60);
            verify(authenticationService).initiatePasswordReset(request);
        }
    }

    // ============================================================
    // RESET PASSWORD ENDPOINT TESTS
    // ============================================================

    @Nested
    @DisplayName("POST /reset-password")
    class ResetPasswordEndpointTests {

        @Test
        @DisplayName("Should return success message on successful reset")
        void resetPassword_validRequest_returnsSuccess() {
            // Given
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "valid-token-uuid", "NewSecure@123", "NewSecure@123"
            );
            doNothing().when(authenticationService).resetPassword(any());

            // When
            ApiResponse<Void> result = authController.resetPassword(request);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("reset successfully");
            verify(authenticationService).resetPassword(request);
        }
    }
}
