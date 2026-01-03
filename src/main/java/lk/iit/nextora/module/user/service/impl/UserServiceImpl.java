package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.config.redis.RedisConfig.CacheNames;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
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

    private final AuthenticationService authenticationService;
    private final UserProfileMapper userProfileMapper;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

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
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        BaseUser currentUser = getCurrentAuthenticatedUser();
        log.info("Updating profile for user: {}", StringUtils.maskEmail(currentUser.getEmail()));

        // Update common fields if provided
        updateCommonFields(currentUser, request);

        // Update role-specific fields based on user type
        updateRoleSpecificFields(currentUser, request);

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
        } else if (user instanceof Lecturer lecturer) {
            updateLecturerFields(lecturer, request);
        } else if (user instanceof AcademicStaff staff) {
            updateAcademicStaffFields(staff, request);
        } else if (user instanceof NonAcademicStaff staff) {
            updateNonAcademicStaffFields(staff, request);
        } else {
            log.debug("No role-specific fields to update for user type: {}", user.getUserType());
        }
    }

    private void updateStudentFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
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
    }

    private void updateLecturerFields(lk.iit.nextora.module.auth.entity.Lecturer lecturer, UpdateProfileRequest request) {
        if (request.getSpecialization() != null) {
            lecturer.setSpecialization(StringUtils.trim(request.getSpecialization()));
        }
        if (request.getOfficeLocation() != null) {
            lecturer.setOfficeLocation(StringUtils.trim(request.getOfficeLocation()));
        }
        if (request.getBio() != null) {
            lecturer.setBio(StringUtils.trim(request.getBio()));
        }
        if (request.getAvailableForMeetings() != null) {
            lecturer.setAvailableForMeetings(request.getAvailableForMeetings());
        }
    }

    private void updateAcademicStaffFields(lk.iit.nextora.module.auth.entity.AcademicStaff staff, UpdateProfileRequest request) {
        if (request.getOfficeLocation() != null) {
            staff.setOfficeLocation(StringUtils.trim(request.getOfficeLocation()));
        }
        if (request.getResponsibilities() != null) {
            staff.setResponsibilities(StringUtils.trim(request.getResponsibilities()));
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

    @Override
    public void changePassword(ChangePasswordRequest request) {
        BaseUser currentUser = getCurrentAuthenticatedUser();
        log.info("Password change requested for user: {}", StringUtils.maskEmail(currentUser.getEmail()));

        // Validate using ValidationUtils
        ValidationUtils.requireEquals(
                request.getNewPassword(),
                request.getConfirmPassword(),
                "New passwords do not match"
        );

        ValidationUtils.requireTrue(
                passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword()),
                "Current password is incorrect"
        );

        ValidationUtils.requireFalse(
                passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword()),
                "New password must be different from current password"
        );

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        entityManager.merge(currentUser);
        entityManager.flush();

        // Evict all user caches (security-sensitive change)
        cacheService.evictAllUserCaches(currentUser.getId());

        log.info("Password changed successfully for user: {}", StringUtils.maskEmail(currentUser.getEmail()));
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
    @Cacheable(value = CacheNames.USERS_LIST_CACHE, key = "'all'", unless = "#result.isEmpty()")
    public List<UserSummaryResponse> getAllUsers() {
        log.debug("Fetching all users (cache miss)");

        List<BaseUser> users = entityManager
                .createQuery("SELECT u FROM BaseUser u ORDER BY u.createdAt DESC", BaseUser.class)
                .getResultList();

        return users.stream()
                .map(userProfileMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void deleteUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Deleting user with ID: {}", id);

        // Check if current user is super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isSuperAdmin(),
                "Only super admin can delete users"
        );

        BaseUser user = entityManager.find(BaseUser.class, id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found", "id", id);
        }

        // Soft delete - just disable the account
//        user.setEnabled(false);
        user.setIsActive(false);
        user.setIsDeleted(true);
        user.setStatus(UserStatus.DELETED);
        entityManager.merge(user);
        entityManager.flush();

        // Evict all caches for this user
        cacheService.evictAllUserCaches(id);

        log.info("User deleted (disabled) successfully: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_PROFILE_CACHE, key = "#id"),
            @CacheEvict(value = CacheNames.USERS_LIST_CACHE, allEntries = true)
    })
    public void restoreUser(Long id) {
        ValidationUtils.requireNonNull(id, "User ID");
        log.info("Restoring user with ID: {}", id);

        // Check if current user is super admin
        ValidationUtils.requireTrue(
                SecurityUtils.isSuperAdmin(),
                "Only super admin can restore users"
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

    /**
     * Get current authenticated user from security context using SecurityUtils
     */
    private BaseUser getCurrentAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        return cacheService.getCachedUserByEmail(email, BaseUser.class)
                .orElseGet(() -> {
                    BaseUser user = authenticationService.findUserByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found", "email", email));
                    cacheService.cacheUserByEmail(email, user);
                    return user;
                });
    }
}

