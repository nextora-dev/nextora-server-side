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
import lk.iit.nextora.module.auth.repository.NonAcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import lk.iit.nextora.module.user.service.AdminUserService;
import lk.iit.nextora.module.user.service.helper.UserUpdateHelper;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserLookupService userLookupService;
    private final UserProfileMapper userProfileMapper;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

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

    // Define roles for normal users (exclude ADMIN and SUPER_ADMIN)
    private static final List<UserRole> NORMAL_USER_ROLES = List.of(
            UserRole.ROLE_STUDENT,
            UserRole.ROLE_ACADEMIC_STAFF,
            UserRole.ROLE_NON_ACADEMIC_STAFF
    );

    @Override
    @Transactional(readOnly = true)
    public UserStatsSummaryResponse getUserStatsSummary() {
        log.debug("Fetching user statistics summary");

        // Total users count (excluding admins)
        Long totalUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .getSingleResult();

        // Count by status (excluding admins)
        Long activeUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND u.status = :status", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("status", UserStatus.ACTIVE)
                .getSingleResult();

        Long deactivatedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND u.status = :status", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("status", UserStatus.DEACTIVATED)
                .getSingleResult();

        Long suspendedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND u.status = :status", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("status", UserStatus.SUSPENDED)
                .getSingleResult();

        Long deletedUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND u.status = :status", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("status", UserStatus.DELETED)
                .getSingleResult();

        Long passwordChangeRequiredUsers = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND u.status = :status", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("status", UserStatus.PASSWORD_CHANGE_REQUIRED)
                .getSingleResult();

        // Count by role
        Long totalStudents = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role = :role", Long.class)
                .setParameter("role", UserRole.ROLE_STUDENT)
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
                .totalAcademicStaff(totalAcademicStaff)
                .totalNonAcademicStaff(totalNonAcademicStaff)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER_PROFILE_CACHE, key = "#id", unless = "#result == null")
    public UserProfileResponse getNormalUserById(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.debug("Fetching normal user by ID: {} (cache miss)", id);

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

        return userProfileMapper.toFullProfileResponse(user, userResponseMapper);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USERS_LIST_CACHE, key = "'normal_users_page_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result.empty")
    public PagedResponse<UserSummaryResponse> getAllNormalUsers(Pageable pageable) {
        log.debug("Fetching all normal users with pagination - page: {}, size: {} (cache miss)",
                pageable.getPageNumber(), pageable.getPageSize());

        // Get total count for normal user roles only
        Long totalElements = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .getSingleResult();

        // Get paginated results for normal user roles only
        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u WHERE u.role IN :roles ORDER BY u.createdAt DESC", BaseUser.class)
                .setParameter("roles", NORMAL_USER_ROLES)
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
    public PagedResponse<UserSummaryResponse> searchNormalUsers(String keyword, Pageable pageable) {
        log.debug("Searching normal users with keyword: {} - page: {}, size: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        ValidationUtils.requireNonBlank(keyword, "Search keyword");

        String searchPattern = "%" + keyword.toLowerCase() + "%";

        // Get total count (only normal users)
        Long totalElements = entityManager
                .createQuery("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles AND (" +
                        "LOWER(u.email) LIKE :keyword OR " +
                        "LOWER(u.firstName) LIKE :keyword OR " +
                        "LOWER(u.lastName) LIKE :keyword OR " +
                        "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE :keyword)", Long.class)
                .setParameter("roles", NORMAL_USER_ROLES)
                .setParameter("keyword", searchPattern)
                .getSingleResult();

        // Get paginated results (only normal users)
        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u WHERE u.role IN :roles AND (" +
                        "LOWER(u.email) LIKE :keyword OR " +
                        "LOWER(u.firstName) LIKE :keyword OR " +
                        "LOWER(u.lastName) LIKE :keyword OR " +
                        "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE :keyword) " +
                        "ORDER BY u.createdAt DESC", BaseUser.class)
                .setParameter("roles", NORMAL_USER_ROLES)
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
    public PagedResponse<UserSummaryResponse> filterNormalUsers(List<UserRole> roles, List<UserStatus> statuses, Pageable pageable) {
        log.debug("Filtering normal users - roles: {}, statuses: {}, page: {}, size: {}",
                roles, statuses, pageable.getPageNumber(), pageable.getPageSize());

        // If roles provided, validate they are all normal user roles
        List<UserRole> effectiveRoles;
        if (roles != null && !roles.isEmpty()) {
            // Filter out any admin roles that might be passed
            effectiveRoles = roles.stream()
                    .filter(NORMAL_USER_ROLES::contains)
                    .collect(Collectors.toList());
            if (effectiveRoles.isEmpty()) {
                throw new BadRequestException("Invalid roles. Only normal user roles are allowed: " + NORMAL_USER_ROLES);
            }
        } else {
            // Default to all normal user roles
            effectiveRoles = NORMAL_USER_ROLES;
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT u FROM BaseUser u WHERE u.role IN :roles");
        StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(u) FROM BaseUser u WHERE u.role IN :roles");

        // Build dynamic query based on filters
        if (statuses != null && !statuses.isEmpty()) {
            queryBuilder.append(" AND u.status IN :statuses");
            countQueryBuilder.append(" AND u.status IN :statuses");
        }

        queryBuilder.append(" ORDER BY u.createdAt DESC");

        // Get total count
        var countQuery = entityManager.createQuery(countQueryBuilder.toString(), Long.class);
        countQuery.setParameter("roles", effectiveRoles);
        if (statuses != null && !statuses.isEmpty()) {
            countQuery.setParameter("statuses", statuses);
        }
        Long totalElements = countQuery.getSingleResult();

        // Get paginated results
        var dataQuery = entityManager.createQuery(queryBuilder.toString(), BaseUser.class);
        dataQuery.setParameter("roles", effectiveRoles);
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
    public void deleteNormalUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Soft deleting normal user with ID: {}", id);

        // Check if current user is admin or super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can delete users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

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
    public UserCreatedResponse createNormalUser(AdminCreateUserRequest request) {
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
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse updateNormalUserById(Long id, UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Updating normal user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can update other users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

        UserUpdateHelper userUpdateHelper = new UserUpdateHelper();

        // Update common fields if provided
        userUpdateHelper.updateCommonFields(user, request);

        // Update role-specific fields based on user type
        userUpdateHelper.updateRoleSpecificFields(user, request);

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
    public void activateNormalUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Activating normal user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can activate users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

        user.setIsActive(true);
        user.setStatus(UserStatus.ACTIVE);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("Normal user activated successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void deactivateNormalUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Deactivating normal user with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can deactivate users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

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

        log.info("Normal user deactivated successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void suspendNormalUser(Long id, String reason) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Suspending normal user with ID: {}", id);

        // Check if current user is admin or super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin or super admin can suspend users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

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

        log.info("Normal user suspended successfully: {}. Reason: {}",
                StringUtils.maskEmail(user.getEmail()),
                reason != null ? reason : "No reason provided");
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id")
    })
    public void resetNormalUserPassword(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Resetting password for normal user with ID: {}", id);

        // Check if current user is super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() ||SecurityUtils.isSuperAdmin(),
                "Only super admin can reset user passwords"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

        // Generate a temporary password or send password reset email
        // For now, we'll set a temporary password
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        // TODO: Send email with temporary password or password reset link
        log.info("Password reset for normal user: {}. Temporary password generated.", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void unlockNormalUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Unlocking normal user account with ID: {}", id);

        // Check if current user is admin
        ValidationUtils.requireTrue(
                SecurityUtils.isAdmin() || SecurityUtils.isSuperAdmin(),
                "Only admin can unlock user accounts"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Validate user is a normal user (not admin/super admin)
        validateNormalUserRole(user);

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

        log.info("Normal user account unlocked successfully: {}", StringUtils.maskEmail(user.getEmail()));
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

    /**
     * Validate that the user is a normal user (not admin/super admin)
     * This ensures admin endpoints only operate on normal users
     */
    private void validateNormalUserRole(BaseUser user) {
        if (!NORMAL_USER_ROLES.contains(user.getRole())) {
            throw new BadRequestException("This operation is only allowed for normal users (Student, Academic Staff, Non-Academic Staff). " +
                    "Use the appropriate admin management endpoint for admin users.");
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
}


