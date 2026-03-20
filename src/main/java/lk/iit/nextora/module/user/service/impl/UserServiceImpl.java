package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.mapper.UserProfileMapper;
import lk.iit.nextora.module.user.service.UserService;
import lk.iit.nextora.module.user.service.helper.UserUpdateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

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
    private final S3Service s3Service;


    private static final String PROFILE_PICTURES_FOLDER = "profile-pictures";
    private static final long MAX_PROFILE_PICTURE_SIZE = 5 * 1024 * 1024; // 5 MB

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
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture) {
        BaseUser currentUser = getCurrentAuthenticatedUser();
        log.info("Updating profile for user: {}", StringUtils.maskEmail(currentUser.getEmail()));

        UserUpdateHelper userUpdateHelper = new UserUpdateHelper();

        // Update common fields if provided
        userUpdateHelper.updateCommonFields(currentUser, request);

        // Update role-specific fields based on user type
        userUpdateHelper.updateRoleSpecificFields(currentUser, request);

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

    // Helper Methods

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

    // ==================== Profile Picture Helper Methods ====================

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
