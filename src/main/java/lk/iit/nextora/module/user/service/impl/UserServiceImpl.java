package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.common.util.FileUtils;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.config.redis.RedisConfig.CacheNames;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.dto.request.AdminCreateAcademicStaffRequest;
import lk.iit.nextora.module.auth.dto.request.AdminCreateNonAcademicStaffRequest;
import lk.iit.nextora.module.auth.dto.request.AdminCreateStudentRequest;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.AdminRepository;
import lk.iit.nextora.module.auth.repository.NonAcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDate;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of UserService for user profile management.
 *
 * Caching Strategy:
 * - User profiles cached for 15 minutes
 * - Users list cached for 5 minutes
 * - Cache evicted on profile updates
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserLookupService userLookupService;
    private final UserProfileMapper userProfileMapper;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    private final AdminRepository adminRepository;

    private final SecurityService securityService;
    private final EmailService emailService;

    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final S3Service s3Service;

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final String PROFILE_PICTURES_FOLDER = "profile-pictures";
    private static final long MAX_PROFILE_PICTURE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        BaseUser currentUser = getCurrentAuthenticatedUser();
        log.debug("Fetching profile for user: {}", StringUtils.maskEmail(currentUser.getEmail()));

        return cacheService.getOrCompute(
                "user:profile:" + currentUser.getId(),
                UserProfileResponse.class,
                () -> userProfileMapper.toFullProfileResponse(currentUser, userResponseMapper),
                Duration.ofMinutes(15)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsSummaryResponse getUserStatsSummary() {
        log.debug("Fetching user statistics summary");

        // Total users count
        Long totalUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u", Long.class)
                .getSingleResult();

        // Count by status
        Long activeUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.status = :status", Long.class)
                .setParameter("status", UserStatus.ACTIVE)
                .getSingleResult();

        Long deactivatedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.status = :status", Long.class)
                .setParameter("status", UserStatus.DEACTIVATED)
                .getSingleResult();

        Long suspendedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.status = :status", Long.class)
                .setParameter("status", UserStatus.SUSPENDED)
                .getSingleResult();

        Long deletedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.status = :status", Long.class)
                .setParameter("status", UserStatus.DELETED)
                .getSingleResult();

        Long passwordChangeRequiredUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.status = :status", Long.class)
                .setParameter("status", UserStatus.PASSWORD_CHANGE_REQUIRED)
                .getSingleResult();

        // Count by role
        Long totalStudents = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_STUDENT)
                .getSingleResult();

        Long totalAdmins = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_ADMIN)
                .getSingleResult();

        Long totalSuperAdmins = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_SUPER_ADMIN)
                .getSingleResult();

        Long totalAcademicStaff = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_ACADEMIC_STAFF)
                .getSingleResult();

        Long totalNonAcademicStaff = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_NON_ACADEMIC_STAFF)
                .getSingleResult();

        return UserStatsSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .deactivatedUsers(deactivatedUsers)
                .suspendedUsers(suspendedUsers)
                .deletedUsers(deletedUsers)
                .passwordChangeRequiredUsers(passwordChangeRequiredUsers)
                .totalStudents(totalStudents)
                .totalAdmins(totalAdmins)
                .totalSuperAdmins(totalSuperAdmins)
                .totalAcademicStaff(totalAcademicStaff)
                .totalNonAcademicStaff(totalNonAcademicStaff)
                .build();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture) {
        BaseUser currentUser = getCurrentAuthenticatedUser();
        log.info("Updating profile for user: {}", StringUtils.maskEmail(currentUser.getEmail()));

        // Update common fields if provided
        updateCommonFields(currentUser, request);

        // Update role-specific fields based on user type
        updateRoleSpecificFields(currentUser, request);

        // Handle profile picture
        handleProfilePictureUpdate(currentUser, profilePicture, deleteProfilePicture);

        entityManager.merge(currentUser);
        entityManager.flush();

        // Evict user-specific caches
        cacheService.evictUserProfile(currentUser.getId());
        cacheService.evictUsersList();

        log.info("Profile updated successfully for user: {}", StringUtils.maskEmail(currentUser.getEmail()));
        return userProfileMapper.toFullProfileResponse(currentUser, userResponseMapper);
    }

    /**
     * Update common fields for all user types
     */
    private void updateCommonFields(BaseUser user, UpdateProfileRequest request) {
        if (StringUtils.isNotBlank(request.getFirstName())) {
            user.setFirstName(StringUtils.trim(request.getFirstName()));
        }
        if (StringUtils.isNotBlank(request.getLastName())) {
            user.setLastName(StringUtils.trim(request.getLastName()));
        }
        if (request.getPhone() != null) {
            user.setPhoneNumber(StringUtils.trim(request.getPhone()));
        }
    }

    /**
     * Update role-specific fields based on user type
     */
    private void updateRoleSpecificFields(BaseUser user, UpdateProfileRequest request) {
        if (user instanceof Student student) {
            updateStudentFields(student, request);
        } else if (user instanceof AcademicStaff staff) {
            updateAcademicStaffFields(staff, request);
        } else if (user instanceof NonAcademicStaff staff) {
            updateNonAcademicStaffFields(staff, request);
        } else {
            log.debug("No role-specific fields to update for user type: {}", user.getUserType());
        }
    }

    private void updateStudentFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        // Update common student fields
        if (request.getAddress() != null) {
            student.setAddress(StringUtils.trim(request.getAddress()));
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGuardianName() != null) {
            student.setGuardianName(StringUtils.trim(request.getGuardianName()));
        }
        if (request.getGuardianPhone() != null) {
            student.setGuardianPhone(StringUtils.trim(request.getGuardianPhone()));
        }

        // Update role-specific fields based on all studentRoleTypes
        if (student.getStudentRoleTypes() != null) {
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.CLUB_MEMBER)) {
                updateClubMemberFields(student, request);
            }
            // Handle both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
            if (student.hasKuppiCapability()) {
                updateKuppiStudentFields(student, request);
            }
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.BATCH_REP)) {
                updateBatchRepFields(student, request);
            }
        }
    }

    private void updateClubMemberFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getClubName() != null) {
            student.setClubName(StringUtils.trim(request.getClubName()));
        }
        if (request.getClubPosition() != null) {
            student.setClubPosition(request.getClubPosition());
        }
        if (request.getClubJoinDate() != null) {
            student.setClubJoinDate(request.getClubJoinDate());
        }
        if (request.getClubMembershipId() != null) {
            student.setClubMembershipId(StringUtils.trim(request.getClubMembershipId()));
        }
    }

    private void updateKuppiStudentFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getKuppiSubjects() != null && !request.getKuppiSubjects().isEmpty()) {
            student.setKuppiSubjects(request.getKuppiSubjects());
        }
        if (request.getKuppiExperienceLevel() != null) {
            student.setKuppiExperienceLevel(StringUtils.trim(request.getKuppiExperienceLevel()));
        }
        if (request.getKuppiAvailability() != null) {
            student.setKuppiAvailability(StringUtils.trim(request.getKuppiAvailability()));
        }
    }

    private void updateBatchRepFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getBatchRepYear() != null) {
            student.setBatchRepYear(StringUtils.trim(request.getBatchRepYear()));
        }
        if (request.getBatchRepSemester() != null) {
            student.setBatchRepSemester(StringUtils.trim(request.getBatchRepSemester()));
        }
        if (request.getBatchRepElectedDate() != null) {
            student.setBatchRepElectedDate(request.getBatchRepElectedDate());
        }
        if (request.getBatchRepResponsibilities() != null) {
            student.setBatchRepResponsibilities(StringUtils.trim(request.getBatchRepResponsibilities()));
        }
    }

    private void updateAcademicStaffFields(lk.iit.nextora.module.auth.entity.AcademicStaff staff, UpdateProfileRequest request) {
        if (request.getOfficeLocation() != null) {
            staff.setOfficeLocation(StringUtils.trim(request.getOfficeLocation()));
        }
        if (request.getResponsibilities() != null) {
            staff.setResponsibilities(StringUtils.trim(request.getResponsibilities()));
        }
        // Lecturer-specific fields (merged into AcademicStaff)
        if (request.getSpecialization() != null) {
            staff.setSpecialization(StringUtils.trim(request.getSpecialization()));
        }
        if (request.getBio() != null) {
            staff.setBio(StringUtils.trim(request.getBio()));
        }
        if (request.getAvailableForMeetings() != null) {
            staff.setAvailableForMeetings(request.getAvailableForMeetings());
        }
        if (request.getDesignation() != null) {
            staff.setDesignation(StringUtils.trim(request.getDesignation()));
        }
    }

    private void updateNonAcademicStaffFields(lk.iit.nextora.module.auth.entity.NonAcademicStaff staff, UpdateProfileRequest request) {
        if (request.getWorkLocation() != null) {
            staff.setWorkLocation(StringUtils.trim(request.getWorkLocation()));
        }
        if (request.getShift() != null) {
            staff.setShift(StringUtils.trim(request.getShift()));
        }
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {

        BaseUser currentUser = getCurrentAuthenticatedUser();

        log.info(
                "Password change requested for user: {}",
                StringUtils.maskEmail(currentUser.getEmail())
        );

        // Match new & confirm password
        ValidationUtils.requireEquals(
                request.getNewPassword(),
                request.getConfirmPassword(),
                "New passwords do not match"
        );

        // Validate current password
        ValidationUtils.requireTrue(
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        currentUser.getPassword()
                ),
                "Current password is incorrect"
        );

        // Prevent reuse
        ValidationUtils.requireFalse(
                passwordEncoder.matches(
                        request.getNewPassword(),
                        currentUser.getPassword()
                ),
                "New password must be different from current password"
        );

        // Password strength validation using ValidationUtils
        ValidationUtils.requireValidPassword(request.getNewPassword(), "New password");

        // Activate user after first login password change
        if (UserStatus.PASSWORD_CHANGE_REQUIRED.equals(currentUser.getStatus())) {
            currentUser.setStatus(UserStatus.ACTIVE);
        }

        currentUser.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        entityManager.merge(currentUser);
        entityManager.flush();

        // Security cleanup
        cacheService.evictAllUserCaches(currentUser.getId());

        log.info(
                "Password changed successfully for user: {}",
                StringUtils.maskEmail(currentUser.getEmail())
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER_PROFILE_CACHE, key = "#id", unless = "#result == null")
    public UserProfileResponse getUserById(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.debug("Fetching user by ID: {} (cache miss)", id);

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        return userProfileMapper.toFullProfileResponse(user, userResponseMapper);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USERS_LIST_CACHE, key = "'admins_page_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.empty")
    public PagedResponse<UserSummaryResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users (Admin and Super Admin) with pagination - page: {}, size: {} (cache miss)",
                pageable.getPageNumber(), pageable.getPageSize());

        List<UserRole> roles = List.of(UserRole.ROLE_STUDENT, UserRole.ROLE_ACADEMIC_STAFF, UserRole.ROLE_NON_ACADEMIC_STAFF);

        // Get total count for roles only
        Long totalElements = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles", Long.class)
                .setParameter("roles", roles)
                .getSingleResult();

        // Get paginated results for admin roles only
        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u WHERE u.role IN :roles ORDER BY u.createdAt DESC", BaseUser.class)
                .setParameter("roles", roles)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UserSummaryResponse> content = users.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<UserSummaryResponse> page = new PageImpl<>(content, pageable, totalElements);

        return PagedResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USERS_LIST_CACHE, key = "'all_admins_page_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.empty")
    public PagedResponse<UserSummaryResponse> getAllAdmins(Pageable pageable) {
        log.debug("Fetching all admins with pagination - page: {}, size: {} (cache miss)",
                pageable.getPageNumber(), pageable.getPageSize());

        List<UserRole> adminRoles = List.of(UserRole.ROLE_ADMIN, UserRole.ROLE_SUPER_ADMIN);

        // Get total count for admin roles only
        Long totalElements = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles", Long.class)
                .setParameter("roles", adminRoles)
                .getSingleResult();

        // Get paginated results for admin roles only
        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u WHERE u.role IN :roles ORDER BY u.createdAt DESC", BaseUser.class)
                .setParameter("roles", adminRoles)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UserSummaryResponse> content = users.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<UserSummaryResponse> page = new PageImpl<>(content, pageable, totalElements);

        return PagedResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> searchUsers(String keyword, Pageable pageable) {
        log.debug("Searching users with keyword: {} - page: {}, size: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        ValidationUtils.requireNonBlank(keyword, "Search keyword");

        String searchPattern = "%" + keyword.toLowerCase() + "%";

        // Get total count
        Long totalElements = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE " +
                        "LOWER(u.email) LIKE :keyword OR " +
                        "LOWER(u.firstName) LIKE :keyword OR " +
                        "LOWER(u.lastName) LIKE :keyword OR " +
                        "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE :keyword", Long.class)
                .setParameter("keyword", searchPattern)
                .getSingleResult();

        // Get paginated results
        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u WHERE " +
                        "LOWER(u.email) LIKE :keyword OR " +
                        "LOWER(u.firstName) LIKE :keyword OR " +
                        "LOWER(u.lastName) LIKE :keyword OR " +
                        "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE :keyword " +
                        "ORDER BY u.createdAt DESC", BaseUser.class)
                .setParameter("keyword", searchPattern)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UserSummaryResponse> content = users.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<UserSummaryResponse> page = new PageImpl<>(content, pageable, totalElements);

        return PagedResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> filterUsers(List<UserRole> roles, List<UserStatus> statuses, Pageable pageable) {
        log.debug("Filtering users - roles: {}, statuses: {}, page: {}, size: {}",
                roles, statuses, pageable.getPageNumber(), pageable.getPageSize());

        StringBuilder queryBuilder = new StringBuilder("SELECT u FROM BaseUser u WHERE 1=1");
        StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(u) FROM BaseUser u WHERE 1=1");

        // Build dynamic query based on filters
        if (roles != null && !roles.isEmpty()) {
            queryBuilder.append(" AND u.role IN :roles");
            countQueryBuilder.append(" AND u.role IN :roles");
        }
        if (statuses != null && !statuses.isEmpty()) {
            queryBuilder.append(" AND u.status IN :statuses");
            countQueryBuilder.append(" AND u.status IN :statuses");
        }

        queryBuilder.append(" ORDER BY u.createdAt DESC");

        // Get total count
        var countQuery = entityManager.createQuery(countQueryBuilder.toString(), Long.class);
        if (roles != null && !roles.isEmpty()) {
            countQuery.setParameter("roles", roles);
        }
        if (statuses != null && !statuses.isEmpty()) {
            countQuery.setParameter("statuses", statuses);
        }
        Long totalElements = countQuery.getSingleResult();

        // Get paginated results
        var dataQuery = entityManager.createQuery(queryBuilder.toString(), BaseUser.class);
        if (roles != null && !roles.isEmpty()) {
            dataQuery.setParameter("roles", roles);
        }
        if (statuses != null && !statuses.isEmpty()) {
            dataQuery.setParameter("statuses", statuses);
        }
        List<BaseUser> users = dataQuery
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UserSummaryResponse> content = users.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<UserSummaryResponse> page = new PageImpl<>(content, pageable, totalElements);

        return PagedResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void deleteUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Soft deleting user with ID: {}", id);

        // Check if current user is admin or super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can delete users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Prevent deleting yourself
        Long currentUserId = securityService.getCurrentUserId();
        if (user.getId().equals(currentUserId)) {
            throw new BadRequestException("Cannot delete your own account using admin endpoint. Use the user self-delete endpoint instead.");
        }

        // Delete profile picture from S3 if exists
        deleteExistingProfilePicture(user);
        user.setProfilePictureUrl(null);
        user.setProfilePictureKey(null);

        // Soft delete - disable the account
        user.setIsActive(false);
        user.setIsDeleted(true);
        user.setStatus(UserStatus.DELETED);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User soft deleted successfully: {} (ID: {})", StringUtils.maskEmail(user.getEmail()), id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void restoreUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Restoring user with ID: {}", id);

        // Check if current user is admin or super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can delete users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Check if user is actually deleted
        ValidationUtils.requireTrue(
                user.getIsDeleted() != null && user.getIsDeleted(),
                "User is not deleted and cannot be restored"
        );

        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setStatus(UserStatus.ACTIVE);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User restored successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    public UserCreatedResponse createUser(AdminCreateUserRequest request) {
        validateAdminAccess();
        validateAllowedRole(request.getRole());

        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireFalse(userLookupService.emailExists(request.getEmail()), "Email already registered");

        log.info("Admin {} creating user: {} with role: {}",
                securityService.getCurrentUserEmail(), StringUtils.maskEmail(request.getEmail()), request.getRole());

        String tempPassword = generateTemporaryPassword();
        BaseUser user = mapToEntity(request);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);

        entityManager.persist(user);
        entityManager.flush();

        sendCredentialsEmail(user, tempPassword);

        log.info("User created successfully: {} - {}", StringUtils.maskEmail(user.getEmail()), user.getUserType());

        return UserCreatedResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .message("User created successfully. Credentials sent to " + user.getEmail())
                .roleSpecificData(userResponseMapper.extractRoleSpecificData(user))
                .build();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse createAdminUser(CreateAdminRequest request) {
        log.info("Creating admin user with email: {}", StringUtils.maskEmail(request.getEmail()));

        // Check if current user is super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isSuperAdmin(),
                "Only super admin can create admin users"
        );

        // Check if email already exists
        ValidationUtils.requireFalse(
                userLookupService.findUserByEmail(request.getEmail()).isPresent(),
                "Email already registered"
        );

        // Check if adminId already exists
        ValidationUtils.requireFalse(
                adminRepository.existsByAdminId(request.getAdminId()),
                "Admin ID '" + request.getAdminId() + "' already exists. Please use a different Admin ID."
        );

        // Create Admin user (only Admin can be created, not Super Admin)
        // Super Admin is unique and created only via DataInitializer
        Admin admin = new Admin();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhoneNumber(request.getPhone());
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setAdminId(request.getAdminId());
        admin.setDepartment(request.getDepartment());
        admin.setAssignedDate(LocalDate.now());
        if (request.getPermissions() != null) {
            admin.setPermissions(request.getPermissions());
        }
        admin.setStatus(UserStatus.ACTIVE);

        Admin createdAdmin = adminRepository.save(admin);
        log.info("Admin user created successfully: {}", StringUtils.maskEmail(request.getEmail()));

        // Evict users list cache
        cacheService.evictUsersList();

        return userProfileMapper.toFullProfileResponse(createdAdmin, userResponseMapper);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse updateUserById(Long id, UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Updating user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can update other users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Update common fields if provided
        updateCommonFields(user, request);

        // Update role-specific fields based on user type
        updateRoleSpecificFields(user, request);

        // Handle profile picture
        handleProfilePictureUpdate(user, profilePicture, deleteProfilePicture);

        entityManager.merge(user);
        entityManager.flush();

        // Evict user-specific caches
        cacheService.evictUserProfile(id);
        cacheService.evictUsersList();

        log.info("User updated successfully: {}", StringUtils.maskEmail(user.getEmail()));
        return userProfileMapper.toFullProfileResponse(user, userResponseMapper);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void activateUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Activating user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can activate users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        user.setIsActive(true);
        user.setStatus(UserStatus.ACTIVE);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User activated successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void deactivateUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Deactivating user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can deactivate users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Prevent deactivating yourself
        String currentEmail = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        ValidationUtils.requireFalse(
                user.getEmail().equals(currentEmail),
                "Cannot deactivate your own account"
        );

        user.setIsActive(false);
        user.setStatus(UserStatus.DEACTIVATED);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User deactivated successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void suspendUser(Long id, String reason) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Suspending user with ID: {}", id);

        // Check if current user is admin or super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin or super admin can suspend users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Prevent suspending yourself
        String currentEmail = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        ValidationUtils.requireFalse(
                user.getEmail().equals(currentEmail),
                "Cannot suspend your own account"
        );

        // Check if user is already suspended
        if (UserStatus.SUSPENDED.equals(user.getStatus())) {
            throw new BadRequestException("User is already suspended");
        }

        // Check if user is deleted
        if (user.getIsDeleted()) {
            throw new BadRequestException("Cannot suspend a deleted user");
        }

        // Suspend the user
        user.setIsActive(false);
        user.setStatus(UserStatus.SUSPENDED);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User suspended successfully: {}. Reason: {}",
                StringUtils.maskEmail(user.getEmail()),
                reason != null ? reason : "No reason provided");
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id")
    })
    public void resetUserPassword(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Resetting password for user with ID: {}", id);

        // Check if current user is super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isSuperAdmin(),
                "Only super admin can reset user passwords"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Generate a temporary password or send password reset email
        // For now, we'll set a temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        // TODO: Send email with temporary password or password reset link
        log.info("Password reset for user: {}. Temporary password generated.", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void unlockUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Unlocking user account with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can unlock user accounts"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Check if user is actually suspended
        if (!UserStatus.SUSPENDED.equals(user.getStatus())) {
            log.info("User {} is not suspended, current status: {}",
                    StringUtils.maskEmail(user.getEmail()), user.getStatus());
            throw new BadRequestException("User is not suspended. Current status: " + user.getStatus());
        }

        // Unlock the account
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginAt(null);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User account unlocked successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    // Helper Methods

    /**
     * Generate a temporary password for password reset
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        return password.toString();
    }

    /**
     * Get current authenticated user from security context using SecurityUtils
     */
    private BaseUser getCurrentAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        return cacheService.getCachedUserByEmail(email, BaseUser.class)
                .orElseGet(() -> {
                    BaseUser user = userLookupService.findUserByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found", "email", email));
                    cacheService.cacheUserByEmail(email, user);
                    return user;
                });
    }

    private void validateAdminAccess() {
        if (!securityService.isAdmin() && !securityService.isSuperAdmin()) {
            throw new BadRequestException("Only Admin or Super Admin can create users");
        }
    }

    private void validateAllowedRole(UserRole role) {
        Set<UserRole> allowedRoles = Set.of(UserRole.ROLE_STUDENT, UserRole.ROLE_ACADEMIC_STAFF, UserRole.ROLE_NON_ACADEMIC_STAFF);
        if (!allowedRoles.contains(role)) {
            throw new BadRequestException("Cannot create users with role: " + role);
        }
    }

    private BaseUser mapToEntity(AdminCreateUserRequest request) {
        return switch (request.getRole()) {
            case ROLE_STUDENT -> mapToStudent((AdminCreateStudentRequest) request);
            case ROLE_ACADEMIC_STAFF -> mapToAcademicStaff((AdminCreateAcademicStaffRequest) request);
            case ROLE_NON_ACADEMIC_STAFF -> mapToNonAcademicStaff((AdminCreateNonAcademicStaffRequest) request);
            default -> throw new BadRequestException("Invalid role for user creation");
        };
    }

    private Student mapToStudent(AdminCreateStudentRequest request) {
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }

        Student student = new Student();
        student.setEmail(request.getEmail());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhone());
        student.setStudentId(request.getStudentId());
        student.setBatch(request.getBatch());
        student.setProgram(request.getProgram());
        student.setFaculty(request.getFaculty());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());
        student.setGuardianName(request.getGuardianName());
        student.setGuardianPhone(request.getGuardianPhone());

        Set<StudentRoleType> roleTypes = request.getEffectiveRoleTypes();
        student.setStudentRoleTypes(EnumSet.copyOf(roleTypes));

        if (roleTypes.contains(StudentRoleType.CLUB_MEMBER)) {
            student.setClubName(request.getClubName());
            student.setClubJoinDate(request.getClubJoinDate());
            student.setClubMembershipId(request.getClubMembershipId());
        }
        // Handle both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
        if (roleTypes.contains(StudentRoleType.KUPPI_STUDENT) || roleTypes.contains(StudentRoleType.SENIOR_KUPPI)) {
            student.setKuppiSubjects(request.getKuppiSubjects());
            student.setKuppiExperienceLevel(request.getKuppiExperienceLevel());
            student.setKuppiAvailability(request.getKuppiAvailability());
            student.setKuppiSessionsCompleted(0);
            student.setKuppiRating(0.0);
        }
        if (roleTypes.contains(StudentRoleType.BATCH_REP)) {
            student.setBatchRepYear(request.getBatchRepYear());
            student.setBatchRepSemester(request.getBatchRepSemester());
            student.setBatchRepElectedDate(request.getBatchRepElectedDate());
            student.setBatchRepResponsibilities(request.getBatchRepResponsibilities());
        }

        return student;
    }

    private AcademicStaff mapToAcademicStaff(AdminCreateAcademicStaffRequest request) {
        if (academicStaffRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        AcademicStaff staff = new AcademicStaff();
        staff.setEmail(request.getEmail());
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhone());
        staff.setEmployeeId(request.getEmployeeId());
        staff.setDepartment(request.getDepartment());
        staff.setFaculty(request.getFaculty());
        staff.setPosition(request.getPosition());
        staff.setOfficeLocation(request.getOfficeLocation());
        staff.setJoinDate(request.getJoinDate());
        staff.setResponsibilities(request.getResponsibilities());
        staff.setDesignation(request.getDesignation());
        staff.setSpecialization(request.getSpecialization());
        staff.setQualifications(request.getQualifications());
        staff.setBio(request.getBio());

        return staff;
    }

    private NonAcademicStaff mapToNonAcademicStaff(AdminCreateNonAcademicStaffRequest request) {
        if (nonAcademicStaffRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        NonAcademicStaff staff = new NonAcademicStaff();
        staff.setEmail(request.getEmail());
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhone());
        staff.setEmployeeId(request.getEmployeeId());
        staff.setDepartment(request.getDepartment());
        staff.setPosition(request.getPosition());
        staff.setWorkLocation(request.getOfficeLocation());
        staff.setJoinDate(request.getJoinDate());

        return staff;
    }

    private void sendCredentialsEmail(BaseUser user, String tempPassword) {
        try {
            emailService.sendAccountCredentialsEmail(user.getEmail(), user.getFirstName(), user.getEmail(), tempPassword);
            log.info("Credentials email sent to: {}", StringUtils.maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send credentials email to: {}", StringUtils.maskEmail(user.getEmail()), e);
        }
    }

    // ==================== Profile Picture Helper Methods ====================

    /**
     * Handle profile picture update - upload new or delete existing
     *
     * @param user                 The user entity to update
     * @param profilePicture       Optional new profile picture file
     * @param deleteProfilePicture If true, delete existing profile picture
     */
    private void handleProfilePictureUpdate(BaseUser user, MultipartFile profilePicture, Boolean deleteProfilePicture) {
        // Handle delete request
        if (Boolean.TRUE.equals(deleteProfilePicture)) {
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                deleteExistingProfilePicture(user);
                user.setProfilePictureUrl(null);
                user.setProfilePictureKey(null);
                log.info("Profile picture deleted for user: {}", StringUtils.maskEmail(user.getEmail()));
            }
            return;
        }

        // Handle upload request
        if (profilePicture != null && !profilePicture.isEmpty()) {
            // Validate file
            validateProfilePictureFile(profilePicture);

            // Delete existing profile picture from S3 if exists
            deleteExistingProfilePicture(user);

            // Upload new profile picture to S3
            String s3Key = s3Service.uploadFile(profilePicture, PROFILE_PICTURES_FOLDER);
            String fileUrl = s3Service.getPublicUrl(s3Key);

            // Update user entity
            user.setProfilePictureUrl(fileUrl);
            user.setProfilePictureKey(s3Key);

            log.info("Profile picture uploaded for user: {}", StringUtils.maskEmail(user.getEmail()));
        }
    }

    /**
     * Validate profile picture file
     */
    private void validateProfilePictureFile(MultipartFile file) {
        // Validate file size
        if (file.getSize() > MAX_PROFILE_PICTURE_SIZE) {
            throw new BadRequestException("Profile picture size must not exceed 5 MB");
        }

        // Validate file type - only images allowed
        String contentType = file.getContentType();
        if (contentType == null || !FileUtils.isImageFile(file)) {
            throw new BadRequestException("Invalid file type. Only image files (JPEG, PNG, GIF, WebP) are allowed");
        }

        // Additional validation for file extension
        String extension = FileUtils.getExtension(file);
        if (!FileUtils.IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file extension. Allowed: " + String.join(", ", FileUtils.IMAGE_EXTENSIONS));
        }

        log.debug("Profile picture validation passed - Size: {} bytes, Type: {}", file.getSize(), contentType);
    }

    /**
     * Delete existing profile picture from S3 if exists
     */
    private void deleteExistingProfilePicture(BaseUser user) {
        if (user.getProfilePictureKey() != null && !user.getProfilePictureKey().isEmpty()) {
            try {
                if (s3Service.fileExists(user.getProfilePictureKey())) {
                    s3Service.deleteFile(user.getProfilePictureKey());
                    log.debug("Deleted existing profile picture from S3: {}", user.getProfilePictureKey());
                }
            } catch (Exception e) {
                log.warn("Failed to delete existing profile picture from S3: {}", e.getMessage());
                // Continue even if delete fails
            }
        }
    }


    // ==================== Super Admin Permanent Delete Operations ====================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void permanentlyDeleteUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.warn("PERMANENT DELETE requested for user ID: {}", id);

        // Only super admin can permanently delete
        ValidationUtils.requireTrue(
                SecurityUtils.isSuperAdmin(),
                "Only Super Admin can permanently delete users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Prevent deleting yourself
        Long currentUserId = securityService.getCurrentUserId();
        if (user.getId().equals(currentUserId)) {
            throw new BadRequestException("Cannot permanently delete your own account");
        }

        // Prevent deleting other super admins
        if (user.getRole() != null && user.getRole().name().equals("ROLE_SUPER_ADMIN")) {
            throw new BadRequestException("Cannot permanently delete Super Admin accounts");
        }

        String userEmail = user.getEmail();
        Long userId = user.getId();

        // Delete profile picture from S3 if exists
        deleteExistingProfilePicture(user);

        // Permanently delete from database
        entityManager.remove(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(userId);

        log.warn("User PERMANENTLY DELETED: {} (ID: {}) by Super Admin: {}",
                StringUtils.maskEmail(userEmail), userId,
                StringUtils.maskEmail(securityService.getCurrentUserEmail()));
    }
}

