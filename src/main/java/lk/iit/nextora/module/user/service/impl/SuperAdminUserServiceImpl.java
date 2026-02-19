package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.config.redis.RedisConfig.CacheNames;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.AdminRepository;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import lk.iit.nextora.module.user.service.SuperAdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SuperAdminUserServiceImpl implements SuperAdminUserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserLookupService userLookupService;
    private final UserProfileMapper userProfileMapper;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    private final AdminRepository adminRepository;

    private final SecurityService securityService;
    private final S3Service s3Service;

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

    // ==================== Super Admin Permanent Delete Operations ====================

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
        admin.setStatus(UserStatus.ACTIVE);

        Admin createdAdmin = adminRepository.save(admin);
        log.info("Admin user created successfully: {}", StringUtils.maskEmail(request.getEmail()));

        // Evict users list cache
        cacheService.evictUsersList();

        return userProfileMapper.toFullProfileResponse(createdAdmin, userResponseMapper);
    }

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

    // ==================== Admin User Management by Super Admin ====================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getAllAdminUsers(Pageable pageable) {
        log.debug("Super Admin fetching all admin users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Validate super admin access
        validateSuperAdminAccess();

        // Query only ROLE_ADMIN users (exclude ROLE_SUPER_ADMIN)
        String jpql = "SELECT a FROM Admin a WHERE a.role = :role ORDER BY a.createdAt DESC";
        String countJpql = "SELECT COUNT(a) FROM Admin a WHERE a.role = :role";

        Long totalElements = entityManager
                .createQuery(countJpql, Long.class)
                .setParameter("role", UserRole.ROLE_ADMIN)
                .getSingleResult();

        List<Admin> admins = entityManager
                .createQuery(jpql, Admin.class)
                .setParameter("role", UserRole.ROLE_ADMIN)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<UserSummaryResponse> adminResponses = admins.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

        log.info("Retrieved {} admin users out of {} total", adminResponses.size(), totalElements);

        return PagedResponse.<UserSummaryResponse>builder()
                .content(adminResponses)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() >= totalPages - 1)
                .empty(adminResponses.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getAdminUserById(Long adminId) {
        ValidationUtils.requireNonNull(adminId, "Admin ID");
        log.debug("Super Admin fetching admin user with ID: {}", adminId);

        // Validate super admin access
        validateSuperAdminAccess();

        Admin admin = findAdminById(adminId);

        log.info("Retrieved admin user: {} (ID: {})",
                StringUtils.maskEmail(admin.getEmail()), adminId);

        return userProfileMapper.toFullProfileResponse(admin, userResponseMapper);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#adminId"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse updateAdminUser(Long adminId, UpdateAdminRequest request) {
        ValidationUtils.requireNonNull(adminId, "Admin ID");
        ValidationUtils.requireNonNull(request, "Update request");
        log.info("Super Admin updating admin user with ID: {}", adminId);

        // Validate super admin access
        validateSuperAdminAccess();

        // Validate that at least one field is being updated
        ValidationUtils.requireTrue(
                request.hasAnyUpdate(),
                "At least one field must be provided for update"
        );

        Admin admin = findAdminById(adminId);

        // Track original email for logging
        String originalEmail = admin.getEmail();

        // Update fields if provided
        updateAdminFields(admin, request);

        // Save changes
        Admin updatedAdmin = adminRepository.save(admin);

        // Evict caches
        cacheService.evictAllUserCaches(adminId);

        log.info("Admin user updated successfully: {} -> {} (ID: {})",
                StringUtils.maskEmail(originalEmail),
                StringUtils.maskEmail(updatedAdmin.getEmail()),
                adminId);

        return userProfileMapper.toFullProfileResponse(updatedAdmin, userResponseMapper);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#adminId"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void softDeleteAdminUser(Long adminId) {
        ValidationUtils.requireNonNull(adminId, "Admin ID");
        log.warn("Super Admin initiating SOFT DELETE for admin user ID: {}", adminId);

        // Validate super admin access
        validateSuperAdminAccess();

        Admin admin = findAdminById(adminId);

        // Prevent deleting yourself
        preventSelfAction(admin, "soft delete");

        // Check if already deleted
        if (admin.getIsDeleted() || UserStatus.DELETED.equals(admin.getStatus())) {
            throw new BadRequestException("Admin user is already deleted");
        }

        // Perform soft delete
        admin.setIsDeleted(true);
        admin.setIsActive(false);
        admin.setStatus(UserStatus.DELETED);

        // Delete profile picture from S3 if exists
        deleteExistingProfilePicture(admin);
        admin.setProfilePictureUrl(null);
        admin.setProfilePictureKey(null);

        adminRepository.save(admin);

        // Evict all caches for this admin
        cacheService.evictAllUserCaches(adminId);

        log.warn("Admin user SOFT DELETED: {} (ID: {}) by Super Admin: {}",
                StringUtils.maskEmail(admin.getEmail()), adminId,
                StringUtils.maskEmail(securityService.getCurrentUserEmail()));
    }

    // ==================== Admin Management Helper Methods ====================

    /**
     * Validates that the current user has Super Admin privileges
     * @throws BadRequestException if user is not Super Admin
     */
    private void validateSuperAdminAccess() {
        if (!SecurityUtils.isSuperAdmin()) {
            log.warn("Unauthorized access attempt to admin user management by user: {}",
                    StringUtils.maskEmail(securityService.getCurrentUserEmail()));
            throw new BadRequestException("Only Super Admin can perform this operation");
        }
    }

    /**
     * Find admin by ID with proper validation
     * @param adminId The admin user ID
     * @return Admin entity
     * @throws ResourceNotFoundException if admin not found
     * @throws BadRequestException if user is not an admin
     */
    private Admin findAdminById(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found", "id", adminId));

        // Verify it's actually an ADMIN (not SUPER_ADMIN)
        if (!UserRole.ROLE_ADMIN.equals(admin.getRole())) {
            throw new BadRequestException("User is not an admin or operation not permitted for this role");
        }

        return admin;
    }

    /**
     * Prevents self-targeted actions (delete, etc.)
     * @param admin Target admin
     * @param action Action description for error message
     */
    private void preventSelfAction(Admin admin, String action) {
        Long currentUserId = securityService.getCurrentUserId();
        if (admin.getId().equals(currentUserId)) {
            throw new BadRequestException("Cannot " + action + " your own account");
        }
    }

    /**
     * Updates admin fields from the request
     * @param admin Admin entity to update
     * @param request Update request with new values
     */
    private void updateAdminFields(Admin admin, UpdateAdminRequest request) {
        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(admin.getEmail())) {
            // Validate email format
            ValidationUtils.requireValidEmail(request.getEmail(), "Email");
            // Check if email is already in use
            if (userLookupService.emailExists(request.getEmail())) {
                throw new BadRequestException("Email is already registered to another user");
            }
            admin.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            log.debug("Password updated for admin: {}", StringUtils.maskEmail(admin.getEmail()));
        }

        // Update first name if provided
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            admin.setFirstName(request.getFirstName().trim());
        }

        // Update last name if provided
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            admin.setLastName(request.getLastName().trim());
        }

        // Update phone if provided
        if (request.getPhone() != null) {
            admin.setPhoneNumber(request.getPhone().trim());
        }

        // Update department if provided
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            admin.setDepartment(request.getDepartment().trim());
        }

        // Update permissions if provided (replaces existing)
        if (request.getPermissions() != null) {
            admin.setPermissions(request.getPermissions());
            log.debug("Permissions updated for admin: {} - New permissions: {}",
                    StringUtils.maskEmail(admin.getEmail()), request.getPermissions());
        }
    }

    // ==================== Profile Picture Helper Methods ====================
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
