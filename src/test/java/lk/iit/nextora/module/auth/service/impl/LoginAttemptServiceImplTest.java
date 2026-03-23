package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptServiceImpl Unit Tests")
class LoginAttemptServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private LoginAttemptServiceImpl loginAttemptService;

    private Student testUser;

    @BeforeEach
    void setUp() {
        testUser = new Student();
        testUser.setId(1L);
        testUser.setEmail("student@iit.ac.lk");
        testUser.setRole(UserRole.ROLE_STUDENT);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setFailedLoginAttempts(0);
        testUser.setLastFailedLoginAt(null);
    }

    // ============================================================
    // RECORD FAILED ATTEMPT TESTS
    // ============================================================

    @Nested
    @DisplayName("recordFailedAttempt")
    class RecordFailedAttemptTests {

        @ParameterizedTest
        @EnumSource(value = UserRole.class, names = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
        @DisplayName("Should not track failed attempts for admin/super-admin roles")
        void recordFailedAttempt_adminRoles_returnsFalseWithoutTracking(UserRole adminRole) {
            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, adminRole);

            // Then
            assertThat(suspended).isFalse();
            verifyNoInteractions(entityManager);
        }

        @Test
        @DisplayName("Should return false when user not found")
        void recordFailedAttempt_userNotFound_returnsFalse() {
            // Given
            when(entityManager.find(any(), eq(1L))).thenReturn(null);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isFalse();
        }

        @Test
        @DisplayName("Should increment attempt count on first failure of the day")
        void recordFailedAttempt_firstFailureOfDay_incrementsToOne() {
            // Given
            testUser.setFailedLoginAttempts(0);
            testUser.setLastFailedLoginAt(null);
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isFalse();
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
            assertThat(testUser.getLastFailedLoginAt()).isNotNull();
            verify(entityManager).merge(testUser);
            verify(entityManager).flush();
        }

        @Test
        @DisplayName("Should increment from existing count within same day")
        void recordFailedAttempt_subsequentFailureSameDay_increments() {
            // Given
            testUser.setFailedLoginAttempts(3);
            testUser.setLastFailedLoginAt(LocalDateTime.now().minusMinutes(5));
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isFalse();
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should suspend account on 5th failed attempt")
        void recordFailedAttempt_fifthAttempt_suspendsAccount() {
            // Given
            testUser.setFailedLoginAttempts(4);
            testUser.setLastFailedLoginAt(LocalDateTime.now().minusMinutes(1));
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isTrue();
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reset counter and start fresh on new day")
        void recordFailedAttempt_newDay_resetsCounterAndStartsFresh() {
            // Given
            testUser.setFailedLoginAttempts(4);
            testUser.setLastFailedLoginAt(LocalDateTime.now().minusDays(1));
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isFalse();
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle null failedLoginAttempts gracefully")
        void recordFailedAttempt_nullAttempts_treatsAsZero() {
            // Given
            testUser.setFailedLoginAttempts(null);
            testUser.setLastFailedLoginAt(LocalDateTime.now());
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            boolean suspended = loginAttemptService.recordFailedAttempt(1L, UserRole.ROLE_STUDENT);

            // Then
            assertThat(suspended).isFalse();
            assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
        }
    }

    // ============================================================
    // RESET FAILED ATTEMPTS TESTS
    // ============================================================

    @Nested
    @DisplayName("resetFailedAttempts")
    class ResetFailedAttemptsTests {

        @Test
        @DisplayName("Should reset attempts and clear last failed timestamp")
        void resetFailedAttempts_existingUser_resetsAll() {
            // Given
            testUser.setFailedLoginAttempts(4);
            testUser.setLastFailedLoginAt(LocalDateTime.now());
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            loginAttemptService.resetFailedAttempts(1L);

            // Then
            assertThat(testUser.getFailedLoginAttempts()).isZero();
            assertThat(testUser.getLastFailedLoginAt()).isNull();
            verify(entityManager).merge(testUser);
            verify(entityManager).flush();
        }

        @Test
        @DisplayName("Should reactivate suspended user on reset")
        void resetFailedAttempts_suspendedUser_reactivates() {
            // Given
            testUser.setStatus(UserStatus.SUSPENDED);
            testUser.setFailedLoginAttempts(5);
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            loginAttemptService.resetFailedAttempts(1L);

            // Then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should do nothing when user not found")
        void resetFailedAttempts_userNotFound_doesNothing() {
            // Given
            when(entityManager.find(any(), eq(1L))).thenReturn(null);

            // When
            loginAttemptService.resetFailedAttempts(1L);

            // Then
            verify(entityManager, never()).merge(any());
        }

        @Test
        @DisplayName("Should not change status if user was not suspended")
        void resetFailedAttempts_activeUser_keepsActiveStatus() {
            // Given
            testUser.setStatus(UserStatus.ACTIVE);
            testUser.setFailedLoginAttempts(3);
            when(entityManager.find(any(), eq(1L))).thenReturn(testUser);

            // When
            loginAttemptService.resetFailedAttempts(1L);

            // Then
            assertThat(testUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}
