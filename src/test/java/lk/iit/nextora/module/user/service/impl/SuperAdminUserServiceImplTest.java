package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.iit.nextora.common.dto.PagedResponse;
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
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        testAdmin.setDepartment("IT");
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
            verify(entityManager).flush();
            verify(cacheService).evictAllUserCaches(10L);
        }

        @Test
        @DisplayName("Should throw BadRequestException when user is not deleted")
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
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void restoreUser_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(entityManager.find(BaseUser.class, 99L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.restoreUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when non-admin tries to restore")
        void restoreUser_nonAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.restoreUser(10L))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should restore deleted admin user")
        void restoreUser_deletedAdmin_restoresSuccessfully() {
            // Given
            mockSuperAdminAccess();
            testAdmin.setIsDeleted(true);
            testAdmin.setStatus(UserStatus.DELETED);
            testAdmin.setIsActive(false);
            when(entityManager.find(BaseUser.class, 5L)).thenReturn(testAdmin);

            // When
            superAdminUserService.restoreUser(5L);

            // Then
            assertThat(testAdmin.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(testAdmin.getIsActive()).isTrue();
            assertThat(testAdmin.getIsDeleted()).isFalse();
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
            assertThat(result.getId()).isEqualTo(100L);

            ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
            verify(adminRepository).save(captor.capture());
            Admin saved = captor.getValue();
            assertThat(saved.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(saved.getDepartment()).isEqualTo("IT");
            assertThat(saved.getAdminId()).isEqualTo("ADM002");
            assertThat(saved.getEmail()).isEqualTo("newadmin@iit.ac.lk");
            assertThat(saved.getFirstName()).isEqualTo("New");
            assertThat(saved.getLastName()).isEqualTo("Admin");
            assertThat(saved.getPhoneNumber()).isEqualTo("+94771234567");
            assertThat(saved.getAssignedDate()).isNotNull();
            verify(cacheService).evictUsersList();
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already exists")
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
        @DisplayName("Should throw BadRequestException when adminId already exists")
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
        @DisplayName("Should throw BadRequestException when non-super-admin tries to create admin")
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

        @Test
        @DisplayName("Should encode password before saving")
        void createAdminUser_encodesPassword() {
            // Given
            mockSuperAdminAccess();
            CreateAdminRequest request = CreateAdminRequest.builder()
                    .email("new@iit.ac.lk").password("Raw@Pass123")
                    .firstName("A").lastName("B")
                    .adminId("ADM100").department("IT").build();
            when(userLookupService.findUserByEmail(anyString())).thenReturn(Optional.empty());
            when(adminRepository.existsByAdminId(anyString())).thenReturn(false);
            when(passwordEncoder.encode("Raw@Pass123")).thenReturn("$2a$12$hashed");
            when(adminRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().build());

            // When
            superAdminUserService.createAdminUser(request);

            // Then
            verify(passwordEncoder).encode("Raw@Pass123");
            verify(adminRepository).save(argThat(admin -> "$2a$12$hashed".equals(admin.getPassword())));
        }
    }

    // ============================================================
    // GET ALL ADMIN USERS
    // ============================================================

    @Nested
    @DisplayName("getAllAdminUsers")
    class GetAllAdminUsersTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should return paginated admin users excluding super admins")
        void getAllAdminUsers_returnsPagedResponse() {
            // Given
            mockSuperAdminAccess();
            Pageable pageable = PageRequest.of(0, 10);

            TypedQuery<Long> countQuery = mock(TypedQuery.class);
            TypedQuery<Admin> dataQuery = mock(TypedQuery.class);

            when(entityManager.createQuery(startsWith("SELECT COUNT"), eq(Long.class))).thenReturn(countQuery);
            when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(2L);

            when(entityManager.createQuery(startsWith("SELECT a"), eq(Admin.class))).thenReturn(dataQuery);
            when(dataQuery.setParameter(anyString(), any())).thenReturn(dataQuery);
            when(dataQuery.setFirstResult(anyInt())).thenReturn(dataQuery);
            when(dataQuery.setMaxResults(anyInt())).thenReturn(dataQuery);
            when(dataQuery.getResultList()).thenReturn(List.of(testAdmin));

            UserSummaryResponse summary = UserSummaryResponse.builder()
                    .id(5L).email("admin@iit.ac.lk").role(UserRole.ROLE_ADMIN).build();
            when(userProfileMapper.toSummaryResponse(any())).thenReturn(summary);

            // When
            PagedResponse<UserSummaryResponse> result = superAdminUserService.getAllAdminUsers(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getPageNumber()).isZero();
            assertThat(result.isFirst()).isTrue();
        }

        @Test
        @DisplayName("Should throw BadRequestException when non-super-admin tries to get admins")
        void getAllAdminUsers_nonSuperAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.getAllAdminUsers(pageable))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Super Admin");
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
            UserProfileResponse expected = UserProfileResponse.builder()
                    .id(5L).email("admin@iit.ac.lk").build();
            when(userProfileMapper.toFullProfileResponse(any(), any())).thenReturn(expected);

            // When
            UserProfileResponse result = superAdminUserService.getAdminUserById(5L);

            // Then
            assertThat(result.getId()).isEqualTo(5L);
            assertThat(result.getEmail()).isEqualTo("admin@iit.ac.lk");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when admin not found")
        void getAdminUserById_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.getAdminUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when user is SUPER_ADMIN role")
        void getAdminUserById_superAdminRole_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            Admin superAdminAsAdmin = new Admin();
            superAdminAsAdmin.setId(1L);
            superAdminAsAdmin.setRole(UserRole.ROLE_SUPER_ADMIN);
            when(adminRepository.findById(1L)).thenReturn(Optional.of(superAdminAsAdmin));

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.getAdminUserById(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not an admin");
        }

        @Test
        @DisplayName("Should throw BadRequestException when non-super-admin accesses")
        void getAdminUserById_nonSuperAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.getAdminUserById(5L))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // ============================================================
    // UPDATE ADMIN USER
    // ============================================================

    @Nested
    @DisplayName("updateAdminUser")
    class UpdateAdminUserTests {

        @Test
        @DisplayName("Should update admin fields and return updated profile")
        void updateAdminUser_validRequest_updatesFields() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .firstName("Updated")
                    .lastName("Admin")
                    .department("Engineering")
                    .build();
            when(adminRepository.save(any())).thenReturn(testAdmin);
            UserProfileResponse expected = UserProfileResponse.builder()
                    .id(5L).firstName("Updated").build();
            when(userProfileMapper.toFullProfileResponse(any(), any())).thenReturn(expected);

            // When
            UserProfileResponse result = superAdminUserService.updateAdminUser(5L, request);

            // Then
            assertThat(result.getFirstName()).isEqualTo("Updated");
            assertThat(testAdmin.getFirstName()).isEqualTo("Updated");
            assertThat(testAdmin.getLastName()).isEqualTo("Admin");
            assertThat(testAdmin.getDepartment()).isEqualTo("Engineering");
            verify(adminRepository).save(testAdmin);
            verify(cacheService).evictAllUserCaches(5L);
        }

        @Test
        @DisplayName("Should update admin email when valid and unique")
        void updateAdminUser_newEmail_updatesEmail() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .email("newemail@iit.ac.lk").build();
            when(userLookupService.emailExists("newemail@iit.ac.lk")).thenReturn(false);
            when(adminRepository.save(any())).thenReturn(testAdmin);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(5L).build());

            // When
            superAdminUserService.updateAdminUser(5L, request);

            // Then
            assertThat(testAdmin.getEmail()).isEqualTo("newemail@iit.ac.lk");
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already in use")
        void updateAdminUser_duplicateEmail_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .email("existing@iit.ac.lk").build();
            when(userLookupService.emailExists("existing@iit.ac.lk")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.updateAdminUser(5L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("Should update password with encoding")
        void updateAdminUser_passwordUpdate_encodesAndSaves() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .password("NewSecure@123").build();
            when(passwordEncoder.encode("NewSecure@123")).thenReturn("$2a$12$newHash");
            when(adminRepository.save(any())).thenReturn(testAdmin);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(5L).build());

            // When
            superAdminUserService.updateAdminUser(5L, request);

            // Then
            assertThat(testAdmin.getPassword()).isEqualTo("$2a$12$newHash");
            verify(passwordEncoder).encode("NewSecure@123");
        }

        @Test
        @DisplayName("Should update permissions replacing existing ones")
        void updateAdminUser_permissions_replacesExisting() {
            // Given
            mockSuperAdminAccess();
            testAdmin.setPermissions(Set.of("OLD:PERM"));
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .permissions(Set.of("NEW:PERM1", "NEW:PERM2")).build();
            when(adminRepository.save(any())).thenReturn(testAdmin);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(5L).build());

            // When
            superAdminUserService.updateAdminUser(5L, request);

            // Then
            assertThat(testAdmin.getPermissions()).containsExactlyInAnyOrder("NEW:PERM1", "NEW:PERM2");
        }

        @Test
        @DisplayName("Should throw BadRequestException when no fields to update")
        void updateAdminUser_emptyRequest_throwsBadRequest() {
            // Given
            mockSuperAdminAccess();
            UpdateAdminRequest request = UpdateAdminRequest.builder().build();

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.updateAdminUser(5L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("At least one field");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when admin not found")
        void updateAdminUser_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(99L)).thenReturn(Optional.empty());
            UpdateAdminRequest request = UpdateAdminRequest.builder().firstName("A").build();

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.updateAdminUser(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should not update email when same as current")
        void updateAdminUser_sameEmail_noEmailChange() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .email("admin@iit.ac.lk") // same as current
                    .firstName("Updated").build();
            when(adminRepository.save(any())).thenReturn(testAdmin);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(5L).build());

            // When
            superAdminUserService.updateAdminUser(5L, request);

            // Then
            verify(userLookupService, never()).emailExists(anyString());
            assertThat(testAdmin.getEmail()).isEqualTo("admin@iit.ac.lk");
        }

        @Test
        @DisplayName("Should not update blank first name")
        void updateAdminUser_blankFirstName_noChange() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            UpdateAdminRequest request = UpdateAdminRequest.builder()
                    .firstName("   ")
                    .department("New Dept").build();
            when(adminRepository.save(any())).thenReturn(testAdmin);
            when(userProfileMapper.toFullProfileResponse(any(), any()))
                    .thenReturn(UserProfileResponse.builder().id(5L).build());

            // When
            superAdminUserService.updateAdminUser(5L, request);

            // Then
            assertThat(testAdmin.getFirstName()).isEqualTo("Admin"); // unchanged
            assertThat(testAdmin.getDepartment()).isEqualTo("New Dept"); // updated
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
        @DisplayName("Should delete profile picture from S3 before permanent delete")
        void permanentlyDeleteUser_withProfilePicture_deletesFromS3() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(securityService.getCurrentUserEmail()).thenReturn("superadmin@iit.ac.lk");
            testStudent.setProfilePictureKey("profile-pictures/pic.jpg");
            when(entityManager.find(BaseUser.class, 10L)).thenReturn(testStudent);
            when(s3Service.fileExists("profile-pictures/pic.jpg")).thenReturn(true);

            // When
            superAdminUserService.permanentlyDeleteUser(10L);

            // Then
            verify(s3Service).deleteFile("profile-pictures/pic.jpg");
            verify(entityManager).remove(testStudent);
        }

        @Test
        @DisplayName("Should throw BadRequestException when trying to delete own account")
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
        @DisplayName("Should throw BadRequestException when trying to delete super admin")
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

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void permanentlyDeleteUser_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(entityManager.find(BaseUser.class, 99L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.permanentlyDeleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when non-super-admin tries to delete")
        void permanentlyDeleteUser_nonSuperAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.permanentlyDeleteUser(10L))
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
            assertThat(testAdmin.getProfilePictureUrl()).isNull();
            assertThat(testAdmin.getProfilePictureKey()).isNull();
            verify(adminRepository).save(testAdmin);
            verify(cacheService).evictAllUserCaches(5L);
        }

        @Test
        @DisplayName("Should delete profile picture on soft delete")
        void softDeleteAdminUser_withProfilePic_deletesFromS3() {
            // Given
            mockSuperAdminAccess();
            when(securityService.getCurrentUserId()).thenReturn(99L);
            when(securityService.getCurrentUserEmail()).thenReturn("sa@iit.ac.lk");
            testAdmin.setProfilePictureKey("profile-pictures/admin-pic.jpg");
            when(adminRepository.findById(5L)).thenReturn(Optional.of(testAdmin));
            when(s3Service.fileExists("profile-pictures/admin-pic.jpg")).thenReturn(true);

            // When
            superAdminUserService.softDeleteAdminUser(5L);

            // Then
            verify(s3Service).deleteFile("profile-pictures/admin-pic.jpg");
        }

        @Test
        @DisplayName("Should throw BadRequestException when admin is already deleted")
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
        @DisplayName("Should throw BadRequestException when trying to soft-delete own account")
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

        @Test
        @DisplayName("Should throw ResourceNotFoundException when admin not found")
        void softDeleteAdminUser_notFound_throwsNotFound() {
            // Given
            mockSuperAdminAccess();
            when(adminRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.softDeleteAdminUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when non-super-admin tries to delete")
        void softDeleteAdminUser_nonSuperAdmin_throwsBadRequest() {
            // Given
            securityUtilsMock.when(SecurityUtils::isSuperAdmin).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> superAdminUserService.softDeleteAdminUser(5L))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}
