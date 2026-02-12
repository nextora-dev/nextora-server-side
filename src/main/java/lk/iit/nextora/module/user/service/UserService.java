package lk.iit.nextora.module.user.service;

import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    /**
     * Update current user profile with optional profile picture
     *
     * @param request              Profile update request
     * @param profilePicture       Optional profile picture file (JPEG, PNG, GIF, WebP, max 5MB)
     * @param deleteProfilePicture If true, delete existing profile picture
     * @return Updated user profile response with profile picture URL
     */
    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    void changePassword(ChangePasswordRequest request);


    UserProfileResponse getUserById(Long id);

    List<UserSummaryResponse> getAllUsers();

    UserProfileResponse createAdminUser(CreateAdminRequest request);

    /**
     * Update user by ID with optional profile picture (Admin operation)
     *
     * @param id                   User ID
     * @param request              Profile update request
     * @param profilePicture       Optional profile picture file
     * @param deleteProfilePicture If true, delete existing profile picture
     * @return Updated user profile response
     */
    UserProfileResponse updateUserById(Long id, UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    /**
     * Soft delete user by ID (Admin operation).
     * Removes profile picture from S3 and marks user as deleted.
     *
     * @param id User ID to delete
     */
    void deleteUser(Long id);

    void restoreUser(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

    void resetUserPassword(Long id);

    void unlockUser(Long id);

    UserCreatedResponse createUser(AdminCreateUserRequest request);

    /**
     * Permanently delete user from database (Super Admin operation).
     * This action is irreversible - removes all user data including profile picture.
     *
     * @param id User ID to permanently delete
     */
    void permanentlyDeleteUser(Long id);
}

