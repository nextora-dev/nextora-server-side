package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.entity.Admin;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.NonAcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserServiceImpl Unit Tests")
class AdminUserServiceImplTest {

    @Mock private EntityManager entityManager;
    @Mock private UserLookupService userLookupService;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private UserResponseMapper userResponseMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CacheService cacheService;
    @Mock private SecurityService securityService;
    @Mock private EmailService emailService;
    @Mock private StudentRepository studentRepository;
    @Mock private AcademicStaffRepository academicStaffRepository;
    @Mock private NonAcademicStaffRepository nonAcademicStaffRepository;
    @Mock private S3Service s3Service;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminUserService, "entityManager", entityManager);
        securityUtilsMock = mockStatic(SecurityUtils.class);

        testStudent = new Student();
        testStudent.setId(10L);
        testStudent.setEmail("student@iit.ac.lk");
        testStudent.setPassword("encoded");
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setRole(UserRole.ROLE_STUDENT);
        testStudent.setStatus(UserStatus.ACTIVE);
        testStudent.setIsActive(true);
        testStudent.setIsDeleted(false);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private void mockAdminAccess() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of("admin@iit.ac.lk"));
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<Long> mockCountQuery(Long count) {
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(count);
        return query;
    }

    // ============================================================
    // GET USER STATS SUMMARY
    // ============================================================

    @Nested
    @DisplayName("getUserStatsSummary")
    class GetUserStatsSummaryTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return correct statistics from database queries")
        void getUserStatsSummary_returnsCorrectCounts() {
            // Given
            TypedQuery<Long> query = mock(TypedQuery.class);
            when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            when(query.getSingleResult())
                    .thenReturn(100L)  // total
                    .thenReturn(80L)   // active
                    .thenReturn(5L)    // deactivated
                    .thenReturn(3L)    // suspended
                    .thenReturn(2L)    // deleted
                    .thenReturn(10L)   // password change required
                    .thenReturn(60L)   // students
                    .thenReturn(25L)   // academic staff
                    .thenReturn(15L);  // non-academic staff

            // When
            UserStatsSummaryResponse result = adminUserService.getUserStatsSummary();

            // Then
            assertThat(result.getTotalUsers()).isEqualTo(100L);
            assertThat(result.getActiveUsers()).isEqualTo(80L);
            assertThat(result.getSuspendedUsers()).isEqualTo(3L);
            assertThat(result.getTotalStudents()).isEqualTo(60L);
        }
    }

    // ============================================================
    // GET NORMAL USER BY ID
    // ============================================================

    @Nested
    @DisplayName("getNormalUserById")
    class GetNormalUserByIdTests {

        @Test
        @DisplayName("Should return profile for existing normal user")
        void getNormalUserById_existingStudent_returnsProfile() {
            // Given
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);
            UserProfileResponse expected = UserProfileResponse.builder()
                    .id(10L).email("student@iit.ac.lk").build();
            when(userProfileMapper.toFullProfileResponse(any(), any())).thenReturn(expected);

            // When
            UserProfileResponse result = adminUserService.getNormalUserById(10L);

            // Then
            assertThat(result.getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent user")
        void getNormalUserById_notFound_throwsNotFound() {
            // Given
            when(entityManager.find(BaseUser.class, 99L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> adminUserService.getNormalUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException for admin user")
        void getNormalUserById_adminUser_throwsBadRequest() {
            // Given
            Admin admin = new Admin();
            admin.setId(5L);
            admin.setRole(UserRole.ROLE_ADMIN);
            when(entityManager.find(BaseUser.class, 5L)).thenReturn(admin);

            // When & Then
            assertThatThrownBy(() -> adminUserService.getNormalUserById(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("normal users");
        }
    }

    // ============================================================
    // ACTIVATE NORMAL USER
    // ============================================================

    @Nested
    @DisplayName("activateNormalUser")
    class ActivateNormalUserTests {

        @Test
        @DisplayName("Should activate user and set status to ACTIVE")
        void activateNormalUser_validUser_activates() {
            // Given
            mockAdminAccess();
            testStudent.setStatus(UserStatus.DEACTIVATED);
            testStudent.setIsActive(false);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            adminUserService.activateNormalUser(10L);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(testStudent.getIsActive()).isTrue();
            verify(entityManager).merge(testStudent);
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void activateNormalUser_notFound_throwsNotFound() {
            // Given
            mockAdminAccess();
            when(entityManager.find(BaseUser.class, 99L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> adminUserService.activateNormalUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // DEACTIVATE NORMAL USER
    // ============================================================

    @Nested
    @DisplayName("deactivateNormalUser")
    class DeactivateNormalUserTests {

        @Test
        @DisplayName("Should deactivate user and set status to DEACTIVATED")
        void deactivateNormalUser_validUser_deactivates() {
            // Given
            mockAdminAccess();
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            adminUserService.deactivateNormalUser(10L);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.DEACTIVATED);
            assertThat(testStudent.getIsActive()).isFalse();
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw when trying to deactivate own account")
        void deactivateNormalUser_selfDeactivation_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.of("student@iit.ac.lk"));
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.deactivateNormalUser(10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot deactivate your own account");
        }
    }

    // ============================================================
    // SUSPEND NORMAL USER
    // ============================================================

    @Nested
    @DisplayName("suspendNormalUser")
    class SuspendNormalUserTests {

        @Test
        @DisplayName("Should suspend active user with reason")
        void suspendNormalUser_activeUser_suspends() {
            // Given
            mockAdminAccess();
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            adminUserService.suspendNormalUser(10L, "Policy violation");

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(testStudent.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw when user is already suspended")
        void suspendNormalUser_alreadySuspended_throwsBadRequest() {
            // Given
            mockAdminAccess();
            testStudent.setStatus(UserStatus.SUSPENDED);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.suspendNormalUser(10L, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already suspended");
        }

        @Test
        @DisplayName("Should throw when user is deleted")
        void suspendNormalUser_deletedUser_throwsBadRequest() {
            // Given
            mockAdminAccess();
            testStudent.setIsDeleted(true);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.suspendNormalUser(10L, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("deleted");
        }

        @Test
        @DisplayName("Should throw when trying to suspend own account")
        void suspendNormalUser_selfSuspend_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.of("student@iit.ac.lk"));
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.suspendNormalUser(10L, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot suspend your own account");
        }
    }

    // ============================================================
    // UNLOCK NORMAL USER
    // ============================================================

    @Nested
    @DisplayName("unlockNormalUser")
    class UnlockNormalUserTests {

        @Test
        @DisplayName("Should unlock suspended user, reset attempts, and set ACTIVE")
        void unlockNormalUser_suspendedUser_unlocksAndResets() {
            // Given
            mockAdminAccess();
            testStudent.setStatus(UserStatus.SUSPENDED);
            testStudent.setFailedLoginAttempts(5);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            adminUserService.unlockNormalUser(10L);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(testStudent.getFailedLoginAttempts()).isZero();
            assertThat(testStudent.getLastFailedLoginAt()).isNull();
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw when user is not suspended")
        void unlockNormalUser_notSuspended_throwsBadRequest() {
            // Given
            mockAdminAccess();
            testStudent.setStatus(UserStatus.ACTIVE);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.unlockNormalUser(10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not suspended");
        }
    }

    // ============================================================
    // DELETE NORMAL USER (SOFT DELETE)
    // ============================================================

    @Nested
    @DisplayName("deleteNormalUser")
    class DeleteNormalUserTests {

        @Test
        @DisplayName("Should soft delete user, clear profile pic, set DELETED status")
        void deleteNormalUser_validUser_softDeletes() {
            // Given
            mockAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L); // different user
            testStudent.setProfilePictureKey("profile-pictures/pic.jpg");
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);
            when(s3Service.fileExists(anyString())).thenReturn(true);

            // When
            adminUserService.deleteNormalUser(10L);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(testStudent.getIsActive()).isFalse();
            assertThat(testStudent.getIsDeleted()).isTrue();
            assertThat(testStudent.getProfilePictureUrl()).isNull();
            verify(s3Service).deleteFile("profile-pictures/pic.jpg");
        }

        @Test
        @DisplayName("Should throw when trying to delete own account")
        void deleteNormalUser_selfDelete_throwsBadRequest() {
            // Given
            mockAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(10L); // same user
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> adminUserService.deleteNormalUser(10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot delete your own account");
        }
    }

    // ============================================================
    // RESET NORMAL USER PASSWORD
    // ============================================================

    @Nested
    @DisplayName("resetNormalUserPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should generate temp password and encode it")
        void resetPassword_validUser_generatesNewPassword() {
            // Given
            mockAdminAccess();
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$tempEncoded");

            // When
            adminUserService.resetNormalUserPassword(10L);

            // Then
            verify(passwordEncoder).encode(argThat(pw -> pw.length() == 12));
            verify(entityManager).merge(testStudent);
            verify(cacheService).evictAllUserCaches(10L);
        }
    }

    // ============================================================
    // VALIDATE ACCESS TESTS
    // ============================================================

    @Nested
    @DisplayName("Access Validation")
    class AccessValidationTests {

        @Test
        @DisplayName("Should throw when non-admin tries to activate user")
        void activateNormalUser_nonAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> adminUserService.activateNormalUser(10L))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should reject admin role in createNormalUser")
        void createNormalUser_adminRole_throwsBadRequest() {
            // Given
            when(securityService.isAdmin()).thenReturn(true);

            lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest request =
                    mock(lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest.class);
            when(request.getRole()).thenReturn(UserRole.ROLE_ADMIN);

            // When & Then
            assertThatThrownBy(() -> adminUserService.createNormalUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot create users with role");
        }
    }
}
