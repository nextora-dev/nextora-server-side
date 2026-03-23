package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.dto.request.ForgotPasswordRequest;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.request.ResetPasswordRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.ForgotPasswordResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.PasswordResetToken;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.PasswordResetTokenRepository;
import lk.iit.nextora.module.auth.service.LoginAttemptService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Unit Tests")
class AuthenticationServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UserLookupService userLookupService;
    @Mock private AuthMapper authMapper;
    @Mock private UserResponseMapper userResponseMapper;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private EmailService emailService;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    // ===== Test Fixtures =====

    private LoginRequest validLoginRequest;
    private Student activeStudent;
    private AuthResponse expectedAuthResponse;

    @BeforeEach
    void setUp() {
        // Inject @PersistenceContext field that @InjectMocks doesn't handle
        ReflectionTestUtils.setField(authenticationService, "entityManager", entityManager);
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john.doe@iit.ac.lk");
        validLoginRequest.setPassword("SecurePass@123");
        validLoginRequest.setRole(UserRole.ROLE_STUDENT);

        activeStudent = new Student();
        activeStudent.setId(1L);
        activeStudent.setEmail("john.doe@iit.ac.lk");
        activeStudent.setPassword("$2a$12$encodedPassword");
        activeStudent.setFirstName("John");
        activeStudent.setLastName("Doe");
        activeStudent.setRole(UserRole.ROLE_STUDENT);
        activeStudent.setStatus(UserStatus.ACTIVE);
        activeStudent.setFailedLoginAttempts(0);

        expectedAuthResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .userId(1L)
                .email("john.doe@iit.ac.lk")
                .role(UserRole.ROLE_STUDENT)
                .build();
    }

    // ============================================================
    // LOGIN TESTS
    // ============================================================

    @Nested
    @DisplayName("Login - Successful Scenarios")
    class LoginSuccessTests {

        @Test
        @DisplayName("Should return AuthResponse with tokens for valid active user")
        void login_withValidCredentials_returnsAuthResponse() {
            // Given
            when(userLookupService.findUserByEmailAndRole(anyString(), any(UserRole.class)))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenProvider.generateAccessToken(any())).thenReturn("access-token");
            when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
            when(tokenProvider.getAccessTokenExpiryDate()).thenReturn(new Date());
            when(userResponseMapper.extractRoleSpecificData(any())).thenReturn(Map.of());
            when(authMapper.toAuthResponseWithRoleData(any(), anyString(), anyString(), any(), any()))
                    .thenReturn(expectedAuthResponse);

            // When
            AuthResponse result = authenticationService.login(validLoginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(loginAttemptService).resetFailedAttempts(1L);
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void login_withValidCredentials_resetsFailedAttempts() {
            // Given
            activeStudent.setFailedLoginAttempts(3);
            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenProvider.generateAccessToken(any())).thenReturn("token");
            when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh");
            when(tokenProvider.getAccessTokenExpiryDate()).thenReturn(new Date());
            when(userResponseMapper.extractRoleSpecificData(any())).thenReturn(Map.of());
            when(authMapper.toAuthResponseWithRoleData(any(), any(), any(), any(), any()))
                    .thenReturn(expectedAuthResponse);

            // When
            authenticationService.login(validLoginRequest);

            // Then
            verify(loginAttemptService).resetFailedAttempts(1L);
        }

        @Test
        @DisplayName("Should return password change required response for PASSWORD_CHANGE_REQUIRED status")
        void login_withPasswordChangeRequired_returnsLimitedResponse() {
            // Given
            activeStudent.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
            AuthResponse pcResponse = AuthResponse.builder()
                    .accessToken("limited-token")
                    .passwordChangeRequired(true)
                    .message("Password change required.")
                    .build();

            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenProvider.generateAccessToken(any())).thenReturn("limited-token");
            when(authMapper.toPasswordChangeRequiredResponse(any(), anyString(), anyString()))
                    .thenReturn(pcResponse);

            // When
            AuthResponse result = authenticationService.login(validLoginRequest);

            // Then
            assertThat(result.getPasswordChangeRequired()).isTrue();
            assertThat(result.getRefreshToken()).isNull();
            verify(tokenProvider, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("Should auto-unlock suspended account on new day and allow login")
        void login_suspendedAccountNewDay_autoUnlocksAndLogins() {
            // Given
            activeStudent.setStatus(UserStatus.SUSPENDED);
            activeStudent.setLastFailedLoginAt(LocalDateTime.now().minusDays(1));

            // Simulate what LoginAttemptServiceImpl.resetFailedAttempts does in production
            doAnswer(inv -> {
                activeStudent.setStatus(UserStatus.ACTIVE);
                activeStudent.setFailedLoginAttempts(0);
                activeStudent.setLastFailedLoginAt(null);
                return null;
            }).when(loginAttemptService).resetFailedAttempts(1L);

            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenProvider.generateAccessToken(any())).thenReturn("token");
            when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh");
            when(tokenProvider.getAccessTokenExpiryDate()).thenReturn(new Date());
            when(userResponseMapper.extractRoleSpecificData(any())).thenReturn(Map.of());
            when(authMapper.toAuthResponseWithRoleData(any(), any(), any(), any(), any()))
                    .thenReturn(expectedAuthResponse);

            // When
            AuthResponse result = authenticationService.login(validLoginRequest);

            // Then
            assertThat(result).isNotNull();
            // Called twice: once for auto-unlock, once after successful authentication
            verify(loginAttemptService, times(2)).resetFailedAttempts(1L);
        }
    }

    @Nested
    @DisplayName("Login - Validation Failure Scenarios")
    class LoginValidationTests {

        @Test
        @DisplayName("Should throw BadRequestException when user not found by email and role")
        void login_userNotFound_throwsBadRequest() {
            // Given
            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("No account found with email");
        }

        @Test
        @DisplayName("Should throw BadRequestException for suspended account same day")
        void login_suspendedSameDay_throwsBadRequest() {
            // Given
            activeStudent.setStatus(UserStatus.SUSPENDED);
            activeStudent.setLastFailedLoginAt(LocalDateTime.now());

            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("suspended");
        }

        @Test
        @DisplayName("Should throw BadRequestException for inactive (non-PASSWORD_CHANGE_REQUIRED) account")
        void login_inactiveAccount_throwsBadRequest() {
            // Given
            activeStudent.setStatus(UserStatus.DEACTIVATED);

            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        @DisplayName("Should throw BadRequestException for deleted account")
        void login_deletedAccount_throwsBadRequest() {
            // Given
            activeStudent.setStatus(UserStatus.DELETED);

            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("inactive");
        }
    }

    @Nested
    @DisplayName("Login - Authentication Failure Scenarios")
    class LoginAuthFailureTests {

        @Test
        @DisplayName("Should record failed attempt on bad credentials")
        void login_badCredentials_recordsFailedAttempt() {
            // Given
            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginAttemptService.recordFailedAttempt(anyLong(), any()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid email or password");

            verify(loginAttemptService).recordFailedAttempt(1L, UserRole.ROLE_STUDENT);
        }

        @Test
        @DisplayName("Should throw suspension message when 5th failed attempt triggers suspension")
        void login_fifthFailedAttempt_throwsSuspensionMessage() {
            // Given
            when(userLookupService.findUserByEmailAndRole(anyString(), any()))
                    .thenReturn(Optional.of(activeStudent));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginAttemptService.recordFailedAttempt(anyLong(), any()))
                    .thenReturn(true); // suspended

            // When & Then
            assertThatThrownBy(() -> authenticationService.login(validLoginRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("suspended due to 5 failed login attempts");
        }
    }

    // ============================================================
    // FORGOT PASSWORD TESTS
    // ============================================================

    @Nested
    @DisplayName("Forgot Password - initiatePasswordReset")
    class ForgotPasswordTests {

        private ForgotPasswordRequest forgotRequest;

        @BeforeEach
        void setUp() {
            forgotRequest = new ForgotPasswordRequest();
            forgotRequest.setEmail("john.doe@iit.ac.lk");
            forgotRequest.setRole(null);
        }

        @Test
        @DisplayName("Should create token and send email for valid user without role")
        void initiatePasswordReset_validEmailNoRole_sendsEmail() {
            // Given
            forgotRequest.setRole(null);
            when(userLookupService.findUserByEmail(anyString()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenRepository.save(any(PasswordResetToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(authMapper.toForgotPasswordResponse(any(), eq(60)))
                    .thenReturn(ForgotPasswordResponse.builder()
                            .message("Password reset link sent to your email.")
                            .maskedEmail("j***e@iit.ac.lk")
                            .expiryMinutes(60)
                            .build());

            // When
            ForgotPasswordResponse result = authenticationService.initiatePasswordReset(forgotRequest);

            // Then
            assertThat(result.getMessage()).contains("Password reset link sent");
            assertThat(result.getExpiryMinutes()).isEqualTo(60);
            verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any());
            verify(tokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq(activeStudent), anyString());
        }

        @Test
        @DisplayName("Should create token and send email for valid user with role")
        void initiatePasswordReset_validEmailWithRole_sendsEmail() {
            // Given
            forgotRequest.setRole(UserRole.ROLE_STUDENT);
            when(userLookupService.findUserByEmailAndRole(anyString(), eq(UserRole.ROLE_STUDENT)))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(authMapper.toForgotPasswordResponse(any(), eq(60)))
                    .thenReturn(ForgotPasswordResponse.builder().message("sent").build());

            // When
            authenticationService.initiatePasswordReset(forgotRequest);

            // Then
            verify(userLookupService).findUserByEmailAndRole("john.doe@iit.ac.lk", UserRole.ROLE_STUDENT);
            verify(emailService).sendPasswordResetEmail(eq(activeStudent), anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent email")
        void initiatePasswordReset_emailNotFound_throwsNotFound() {
            // Given
            when(userLookupService.findUserByEmail(anyString()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authenticationService.initiatePasswordReset(forgotRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException for inactive account")
        void initiatePasswordReset_inactiveUser_throwsBadRequest() {
            // Given
            activeStudent.setStatus(UserStatus.DEACTIVATED);
            when(userLookupService.findUserByEmail(anyString()))
                    .thenReturn(Optional.of(activeStudent));

            // When & Then
            assertThatThrownBy(() -> authenticationService.initiatePasswordReset(forgotRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        @DisplayName("Should allow password reset for PASSWORD_CHANGE_REQUIRED status")
        void initiatePasswordReset_passwordChangeRequired_succeeds() {
            // Given
            activeStudent.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
            when(userLookupService.findUserByEmail(anyString()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(authMapper.toForgotPasswordResponse(any(), anyInt()))
                    .thenReturn(ForgotPasswordResponse.builder().message("sent").build());

            // When
            ForgotPasswordResponse result = authenticationService.initiatePasswordReset(forgotRequest);

            // Then
            assertThat(result).isNotNull();
            verify(emailService).sendPasswordResetEmail(any(), anyString());
        }

        @Test
        @DisplayName("Should invalidate all existing tokens before creating new one")
        void initiatePasswordReset_invalidatesPreviousTokens() {
            // Given
            when(userLookupService.findUserByEmail(anyString()))
                    .thenReturn(Optional.of(activeStudent));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(authMapper.toForgotPasswordResponse(any(), anyInt()))
                    .thenReturn(ForgotPasswordResponse.builder().message("sent").build());

            // When
            authenticationService.initiatePasswordReset(forgotRequest);

            // Then — invalidation happens BEFORE save
            var inOrder = inOrder(tokenRepository);
            inOrder.verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any());
            inOrder.verify(tokenRepository).save(any(PasswordResetToken.class));
        }
    }

    // ============================================================
    // RESET PASSWORD TESTS
    // ============================================================

    @Nested
    @DisplayName("Reset Password")
    class ResetPasswordTests {

        private ResetPasswordRequest resetRequest;
        private PasswordResetToken validToken;

        @BeforeEach
        void setUp() {
            resetRequest = new ResetPasswordRequest();
            resetRequest.setToken("valid-uuid-token");
            resetRequest.setNewPassword("NewSecure@123");
            resetRequest.setConfirmPassword("NewSecure@123");

            validToken = new PasswordResetToken(activeStudent);
            validToken.setToken("valid-uuid-token");
            validToken.setUsed(false);
            validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        }

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void resetPassword_validToken_updatesPasswordAndMarksTokenUsed() {
            // Given
            when(tokenRepository.findByToken("valid-uuid-token")).thenReturn(Optional.of(validToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(passwordEncoder.encode("NewSecure@123")).thenReturn("$2a$12$newEncodedPassword");

            // When
            authenticationService.resetPassword(resetRequest);

            // Then
            assertThat(activeStudent.getPassword()).isEqualTo("$2a$12$newEncodedPassword");
            assertThat(validToken.isUsed()).isTrue();
            verify(entityManager).merge(activeStudent);
            verify(tokenRepository).save(validToken);
            verify(tokenRepository).invalidateAllTokensForUser(eq(1L), any());
        }

        @Test
        @DisplayName("Should activate user if status was PASSWORD_CHANGE_REQUIRED")
        void resetPassword_passwordChangeRequiredStatus_activatesUser() {
            // Given
            activeStudent.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(validToken));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            // When
            authenticationService.resetPassword(resetRequest);

            // Then
            assertThat(activeStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw BadRequestException when passwords don't match")
        void resetPassword_passwordsMismatch_throwsBadRequest() {
            // Given
            resetRequest.setConfirmPassword("DifferentPassword@123");

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("do not match");
        }

        @Test
        @DisplayName("Should throw BadRequestException for blank new password")
        void resetPassword_blankNewPassword_throwsBadRequest() {
            // Given
            resetRequest.setNewPassword("");

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("New password is required");
        }

        @Test
        @DisplayName("Should throw BadRequestException for blank confirm password")
        void resetPassword_blankConfirmPassword_throwsBadRequest() {
            // Given
            resetRequest.setConfirmPassword("");

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Confirm password is required");
        }

        @Test
        @DisplayName("Should throw BadRequestException for invalid token")
        void resetPassword_invalidToken_throwsBadRequest() {
            // Given
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid or expired token");
        }

        @Test
        @DisplayName("Should throw BadRequestException for already used token")
        void resetPassword_usedToken_throwsBadRequest() {
            // Given
            validToken.markAsUsed();
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(validToken));

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("Should throw BadRequestException for expired token")
        void resetPassword_expiredToken_throwsBadRequest() {
            // Given
            validToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(validToken));

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should throw BadRequestException when new password is same as old password")
        void resetPassword_sameAsOldPassword_throwsBadRequest() {
            // Given
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(validToken));
            when(passwordEncoder.matches("NewSecure@123", activeStudent.getPassword())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authenticationService.resetPassword(resetRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot be the same as your current password");
        }
    }

    // ============================================================
    // CLEANUP EXPIRED TOKENS TESTS
    // ============================================================

    @Nested
    @DisplayName("Cleanup Expired Tokens")
    class CleanupTests {

        @Test
        @DisplayName("Should delegate to repository for cleanup")
        void cleanupExpiredTokens_delegatesToRepository() {
            // When
            authenticationService.cleanupExpiredTokens();

            // Then
            verify(tokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
        }
    }
}
