package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock private EntityManager entityManager;
    @Mock private UserLookupService userLookupService;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private UserResponseMapper userResponseMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CacheService cacheService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Student testStudent;
    private UserProfileResponse expectedProfile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "entityManager", entityManager);
        securityUtilsMock = mockStatic(SecurityUtils.class);

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setEmail("student@iit.ac.lk");
        testStudent.setPassword("$2a$12$encodedOldPassword");
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setRole(UserRole.ROLE_STUDENT);
        testStudent.setStatus(UserStatus.ACTIVE);

        expectedProfile = UserProfileResponse.builder()
                .id(1L)
                .email("student@iit.ac.lk")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ROLE_STUDENT)
                .build();
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private void mockAuthenticatedUser() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                .thenReturn(Optional.of("student@iit.ac.lk"));
        when(cacheService.getCachedUserByEmail(anyString(), any()))
                .thenReturn(Optional.empty());
        when(userLookupService.findUserByEmail("student@iit.ac.lk"))
                .thenReturn(Optional.of(testStudent));
    }

    // ============================================================
    // GET CURRENT USER PROFILE
    // ============================================================

    @Nested
    @DisplayName("getCurrentUserProfile")
    class GetCurrentUserProfileTests {

        @Test
        @DisplayName("Should return profile for authenticated user from cache computation")
        void getCurrentUserProfile_authenticatedUser_returnsProfile() {
            // Given
            mockAuthenticatedUser();
            when(cacheService.getOrCompute(anyString(), any(), any(), any()))
                    .thenReturn(expectedProfile);

            // When
            UserProfileResponse result = userService.getCurrentUserProfile();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("student@iit.ac.lk");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getRole()).isEqualTo(UserRole.ROLE_STUDENT);
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when security context has no email")
        void getCurrentUserProfile_notAuthenticated_throwsUnauthorized() {
            // Given
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getCurrentUserProfile())
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authenticated");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user email not found in DB")
        void getCurrentUserProfile_userNotFound_throwsNotFound() {
            // Given
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.of("nonexistent@iit.ac.lk"));
            when(cacheService.getCachedUserByEmail(anyString(), any()))
                    .thenReturn(Optional.empty());
            when(userLookupService.findUserByEmail("nonexistent@iit.ac.lk"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getCurrentUserProfile())
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should use cached user when available instead of DB lookup")
        void getCurrentUserProfile_cachedUser_skipsDbLookup() {
            // Given
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.of("student@iit.ac.lk"));
            when(cacheService.getCachedUserByEmail(anyString(), any()))
                    .thenReturn(Optional.of(testStudent));
            when(cacheService.getOrCompute(anyString(), any(), any(), any()))
                    .thenReturn(expectedProfile);

            // When
            UserProfileResponse result = userService.getCurrentUserProfile();

            // Then
            assertThat(result).isNotNull();
            verify(userLookupService, never()).findUserByEmail(anyString());
        }

        @Test
        @DisplayName("Should cache user by email when fetched from DB")
        void getCurrentUserProfile_fetchedFromDb_cachesUser() {
            // Given
            mockAuthenticatedUser();
            when(cacheService.getOrCompute(anyString(), any(), any(), any()))
                    .thenReturn(expectedProfile);

            // When
            userService.getCurrentUserProfile();

            // Then
            verify(cacheService).cacheUserByEmail(eq("student@iit.ac.lk"), eq(testStudent));
        }
    }

    // ============================================================
    // UPDATE CURRENT USER PROFILE
    // ============================================================

    @Nested
    @DisplayName("updateCurrentUserProfile")
    class UpdateCurrentUserProfileTests {

        @Test
        @DisplayName("Should update profile fields, persist, and evict caches")
        void updateProfile_validRequest_updatesFieldsAndEvictsCache() {
            // Given
            mockAuthenticatedUser();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phone("+94771234567")
                    .build();
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(expectedProfile);

            // When
            UserProfileResponse result = userService.updateCurrentUserProfile(request, null, false);

            // Then
            assertThat(result).isNotNull();
            verify(entityManager).merge(testStudent);
            verify(entityManager).flush();
            verify(cacheService).evictUserProfile(1L);
            verify(cacheService).evictUsersList();
        }

        @Test
        @DisplayName("Should delete profile picture from S3 when deleteProfilePicture is true")
        void updateProfile_deleteProfilePicture_removesFromS3() {
            // Given
            mockAuthenticatedUser();
            testStudent.setProfilePictureUrl("https://s3.example.com/pic.jpg");
            testStudent.setProfilePictureKey("profile-pictures/pic.jpg");

            UpdateProfileRequest request = UpdateProfileRequest.builder().build();
            when(s3Service.fileExists("profile-pictures/pic.jpg")).thenReturn(true);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(expectedProfile);

            // When
            userService.updateCurrentUserProfile(request, null, true);

            // Then
            verify(s3Service).deleteFile("profile-pictures/pic.jpg");
            assertThat(testStudent.getProfilePictureUrl()).isNull();
            assertThat(testStudent.getProfilePictureKey()).isNull();
        }

        @Test
        @DisplayName("Should not attempt S3 delete when no profile picture exists")
        void updateProfile_deleteProfilePicture_noExistingPic_skipsS3() {
            // Given
            mockAuthenticatedUser();
            testStudent.setProfilePictureUrl(null);
            testStudent.setProfilePictureKey(null);

            UpdateProfileRequest request = UpdateProfileRequest.builder().build();
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(expectedProfile);

            // When
            userService.updateCurrentUserProfile(request, null, true);

            // Then
            verify(s3Service, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("Should not call S3 upload when profile picture is null")
        void updateProfile_nullProfilePicture_skipsUpload() {
            // Given
            mockAuthenticatedUser();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane").build();
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(expectedProfile);

            // When
            userService.updateCurrentUserProfile(request, null, false);

            // Then
            verify(s3Service, never()).uploadFile(any(), anyString());
        }

        @Test
        @DisplayName("Should not call S3 upload when profile picture is empty")
        void updateProfile_emptyProfilePicture_skipsUpload() {
            // Given
            mockAuthenticatedUser();
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(true);

            UpdateProfileRequest request = UpdateProfileRequest.builder().build();
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(expectedProfile);

            // When
            userService.updateCurrentUserProfile(request, mockFile, false);

            // Then
            verify(s3Service, never()).uploadFile(any(), anyString());
        }

        @Test
        @DisplayName("Should handle different user types (AcademicStaff)")
        void updateProfile_academicStaff_updatesRoleSpecificFields() {
            // Given
            AcademicStaff staff = new AcademicStaff();
            staff.setId(2L);
            staff.setEmail("staff@iit.ac.lk");
            staff.setPassword("encoded");
            staff.setFirstName("Prof");
            staff.setLastName("Smith");
            staff.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            staff.setStatus(UserStatus.ACTIVE);

            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.of("staff@iit.ac.lk"));
            when(cacheService.getCachedUserByEmail(anyString(), any()))
                    .thenReturn(Optional.empty());
            when(userLookupService.findUserByEmail("staff@iit.ac.lk"))
                    .thenReturn(Optional.of(staff));

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .officeLocation("Room 301")
                    .specialization("AI/ML")
                    .build();
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(2L).build());

            // When
            UserProfileResponse result = userService.updateCurrentUserProfile(request, null, false);

            // Then
            assertThat(result).isNotNull();
            assertThat(staff.getOfficeLocation()).isEqualTo("Room 301");
            assertThat(staff.getSpecialization()).isEqualTo("AI/ML");
            verify(entityManager).merge(staff);
        }
    }

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully with valid request")
        void changePassword_validRequest_updatesPassword() {
            // Given
            mockAuthenticatedUser();
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("OldPass@123")
                    .newPassword("NewPass@123")
                    .confirmPassword("NewPass@123")
                    .build();
            when(passwordEncoder.matches("OldPass@123", testStudent.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("NewPass@123", testStudent.getPassword())).thenReturn(false);
            when(passwordEncoder.encode("NewPass@123")).thenReturn("$2a$12$newEncoded");

            // When
            userService.changePassword(request);

            // Then
            assertThat(testStudent.getPassword()).isEqualTo("$2a$12$newEncoded");
            verify(entityManager).merge(testStudent);
            verify(entityManager).flush();
            verify(cacheService).evictAllUserCaches(1L);
        }

        @Test
        @DisplayName("Should activate user if status was PASSWORD_CHANGE_REQUIRED")
        void changePassword_passwordChangeRequired_activatesUser() {
            // Given
            mockAuthenticatedUser();
            testStudent.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("OldPass@123")
                    .newPassword("NewPass@123")
                    .confirmPassword("NewPass@123")
                    .build();
            when(passwordEncoder.matches("OldPass@123", testStudent.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("NewPass@123", testStudent.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            // When
            userService.changePassword(request);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should not change status if already ACTIVE")
        void changePassword_activeUser_statusRemainsActive() {
            // Given
            mockAuthenticatedUser();
            testStudent.setStatus(UserStatus.ACTIVE);
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("OldPass@123")
                    .newPassword("NewPass@123")
                    .confirmPassword("NewPass@123")
                    .build();
            when(passwordEncoder.matches("OldPass@123", testStudent.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("NewPass@123", testStudent.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            // When
            userService.changePassword(request);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw BadRequestException when new passwords don't match")
        void changePassword_mismatchedPasswords_throwsBadRequest() {
            // Given
            mockAuthenticatedUser();
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("OldPass@123")
                    .newPassword("NewPass@123")
                    .confirmPassword("DifferentPass@123")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when current password is incorrect")
        void changePassword_wrongCurrentPassword_throwsBadRequest() {
            // Given
            mockAuthenticatedUser();
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("WrongPass@123")
                    .newPassword("NewPass@123")
                    .confirmPassword("NewPass@123")
                    .build();
            when(passwordEncoder.matches("WrongPass@123", testStudent.getPassword())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("incorrect");
        }

        @Test
        @DisplayName("Should throw BadRequestException when new password same as current")
        void changePassword_sameAsCurrentPassword_throwsBadRequest() {
            // Given
            mockAuthenticatedUser();
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("SamePass@123")
                    .newPassword("SamePass@123")
                    .confirmPassword("SamePass@123")
                    .build();
            when(passwordEncoder.matches("SamePass@123", testStudent.getPassword())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("different");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when not authenticated")
        void changePassword_notAuthenticated_throwsUnauthorized() {
            // Given
            securityUtilsMock.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(Optional.empty());
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Old@123")
                    .newPassword("New@123")
                    .confirmPassword("New@123")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Should not persist changes when validation fails")
        void changePassword_validationFails_noDbWrites() {
            // Given
            mockAuthenticatedUser();
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("Old@123")
                    .newPassword("New@123")
                    .confirmPassword("Mismatch@123")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(BadRequestException.class);

            verify(entityManager, never()).merge(any());
            verify(entityManager, never()).flush();
            verify(cacheService, never()).evictAllUserCaches(anyLong());
        }
    }
}
