package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Admin;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.entity.SuperAdmin;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.AdminRepository;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuperAdminUserServiceImpl Unit Tests")
class SuperAdminUserServiceImplTest {

    @Mock private EntityManager entityManager;
    @Mock private UserLookupService userLookupService;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private UserResponseMapper userResponseMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CacheService cacheService;
    @Mock private AdminRepository adminRepository;
    @Mock private SecurityService securityService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private SuperAdminUserServiceImpl superAdminUserService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Admin testAdmin;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(superAdminUserService, "entityManager", entityManager);
        securityUtilsMock = mockStatic(SecurityUtils.class);

        testAdmin = new Admin();
        testAdmin.setId(5L);
        testAdmin.setEmail("admin@iit.ac.lk");
        testAdmin.setPassword("encoded");
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setRole(UserRole.ROLE_ADMIN);
        testAdmin.setStatus(UserStatus.ACTIVE);
        testAdmin.setAdminId("ADM001");
        testAdmin.setIsActive(true);
        testAdmin.setIsDeleted(false);

        testStudent = new Student();
        testStudent.setId(10L);
        testStudent.setEmail("student@iit.ac.lk");
        testStudent.setRole(UserRole.ROLE_STUDENT);
        testStudent.setStatus(UserStatus.DELETED);
        testStudent.setIsDeleted(true);
        testStudent.setIsActive(false);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    private void mockSuperAdminAccess() {
        securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
    }

    // ============================================================
    // RESTORE USER
    // ============================================================

    @Nested
    @DisplayName("restoreUser")
    class RestoreUserTests {

        @Test
        @DisplayName("Should restore deleted user to ACTIVE status")
        void restoreUser_deletedUser_restoresSuccessfully() {
            // Given
            mockSuperAdminAccess();
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            superAdminUserService.restoreUser(10L);

            // Then
            assertThat(testStudent.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(testStudent.getIsActive()).isTrue();
            assertThat(testStudent.getIsDeleted()).isFalse();
            verify(entityManager).merge(testStudent);
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw when user is not deleted")
        void restoreUser_notDeleted_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            testStudent.setIsDeleted(false);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.restoreUser(10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not deleted");
        }

        @Test
        @DisplayName("Should throw when user not found")
        void restoreUser_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(entityManager.find(BaseUser.class, 99L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.restoreUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when non-admin tries to restore")
        void restoreUser_nonAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.restoreUser(10L))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // ============================================================
    // CREATE ADMIN USER
    // ============================================================

    @Nested
    @DisplayName("createAdminUser")
    class CreateAdminUserTests {

        @Test
        @DisplayName("Should create admin with correct fields and ACTIVE status")
        void createAdminUser_validRequest_createsAdmin() {
            // Given
            mockSuperAdminAccess();
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("newadmin@iit.ac.lk")
                    .password("Secure@123")
                    .firstName("New")
                    .lastName("Admin")
                    .phone("+94771234567")
                    .adminId("ADM002")
                    .department("IT")
                    .build();

            when(userLookupService.findUserByEmail("newadmin@iit.ac.lk")).thenReturn(Optional.empty());
            when(adminRepository.existsByAdminId("ADM002")).thenReturn(false);
            when(passwordEncoder.encode("Secure@123")).thenReturn("$2a$12$encoded");
            when(adminRepository.save(any(Admin.class))).thenAnswer(inv -> {
                Admin a = inv.getArgument(0);
                a.setId(100L);
                return a;
            });
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(100L).build());

            // When
            UserProfileResponse result = superAdminUserService.createAdminUser(request);

            // Then
            assertThat(result).isNotNull();
            ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
            verify(adminRepository).save(captor.capture());
            Admin saved = captor.getValue();
            assertThat(saved.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(saved.getDepartment()).isEqualTo("IT");
            assertThat(saved.getAdminId()).isEqualTo("ADM002");
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void createAdminUser_duplicateEmail_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("existing@iit.ac.lk")
                    .password("Pass@123")
                    .firstName("A").lastName("B")
                    .adminId("ADM003").department("IT")
                    .build();
            when(userLookupService.findUserByEmail("existing@iit.ac.lk"))
                    .thenReturn(Optional.of(testAdmin));

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.createAdminUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("Should throw when adminId already exists")
        void createAdminUser_duplicateAdminId_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("new@iit.ac.lk")
                    .password("Pass@123")
                    .firstName("A").lastName("B")
                    .adminId("ADM001").department("IT")
                    .build();
            when(userLookupService.findUserByEmail("new@iit.ac.lk")).thenReturn(Optional.empty());
            when(adminRepository.existsByAdminId("ADM001")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.createAdminUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw when non-super-admin tries to create admin")
        void createAdminUser_nonSuperAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("a@b.com").password("P@ss1234")
                    .firstName("A").lastName("B")
                    .adminId("X").department("Y")
                    .build();

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.createAdminUser(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("super admin");
        }
    }

    // ============================================================
    // PERMANENTLY DELETE USER
    // ============================================================

    @Nested
    @DisplayName("permanentlyDeleteUser")
    class PermanentlyDeleteUserTests {

        @Test
        @DisplayName("Should permanently remove user from database")
        void permanentlyDeleteUser_validUser_removesFromDB() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(securityService.getCurrentUserEmail()).thenReturn("superadmin@iit.ac.lk");
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When
            superAdminUserService.permanentlyDeleteUser(10L);

            // Then
            verify(entityManager).remove(testStudent);
            verify(entityManager).flush();
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw when trying to delete own account")
        void permanentlyDeleteUser_selfDelete_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(10L);
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.permanentlyDeleteUser(10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot permanently delete your own");
        }

        @Test
        @DisplayName("Should throw when trying to delete super admin")
        void permanentlyDeleteUser_superAdmin_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            SuperAdmin sa = new SuperAdmin();
            sa.setId(2L);
            sa.setRole(UserRole.ROLE_SUPER_ADMIN);
            sa.setEmail("sa@iit.ac.lk");
            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(entityManager.find(BaseUser.class, 2L)).thenReturn(sa);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.permanentlyDeleteUser(2L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Super Admin");
        }
    }

    // ============================================================
    // SOFT DELETE ADMIN USER
    // ============================================================

    @Nested
    @DisplayName("softDeleteAdminUser")
    class SoftDeleteAdminUserTests {

        @Test
        @DisplayName("Should soft delete admin with correct status changes")
        void softDeleteAdminUser_validAdmin_softDeletes() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(securityService.getCurrentUserEmail()).thenReturn("sa@iit.ac.lk");
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));

            // When
            superAdminUserService.softDeleteAdminUser(5L);

            // Then
            assertThat(testAdmin.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(testAdmin.getIsDeleted()).isTrue();
            assertThat(testAdmin.getIsActive()).isFalse();
            verify(adminRepository).save(testAdmin);
            verify(cacheService).evictAllUserCaches(5L);
        }

        @Test
        @DisplayName("Should throw when admin is already deleted")
        void softDeleteAdminUser_alreadyDeleted_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L);
            testAdmin.setIsDeleted(true);
            testAdmin.setStatus(UserStatus.DELETED);
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.softDeleteAdminUser(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already deleted");
        }

        @Test
        @DisplayName("Should throw when trying to soft-delete own account")
        void softDeleteAdminUser_selfDelete_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(5L);
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.softDeleteAdminUser(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot soft delete your own");
        }
    }

    // ============================================================
    // GET ADMIN USER BY ID
    // ============================================================

    @Nested
    @DisplayName("getAdminUserById")
    class GetAdminUserByIdTests {

        @Test
        @DisplayName("Should return admin profile")
        void getAdminUserById_existingAdmin_returnsProfile() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UserProfileResponse expected = UserProfileResponse.builder().id(5L).build();
            when(userProfileMapper.toFullProfileResponse(any(), any())).thenReturn(expected);

            // When
            UserProfileResponse result = superAdminUserService.getAdminUserById(5L);

            // Then
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should throw when admin not found")
        void getAdminUserById_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.getAdminUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}