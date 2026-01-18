package lk.iit.nextora.module.user.service;

import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;

import java.util.List;

/**
 * Service interface for user management operations
 */
public interface UserService {

    /**
     * Get current authenticated user's profile
     *
     * @return user profile response
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Update current authenticated user's profile
     *
     * @param request update profile request
     * @return updated user profile response
     */
    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);

    /**
     * Change current authenticated user's password
     *
     * @param request change password request
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Get user by ID (admin only)
     *
     * @param id user ID
     * @return user profile response
     */
    UserProfileResponse getUserById(Long id);

    /**
     * Get all users (admin only)
     *
     * @return list of user summaries
     */
    List<UserSummaryResponse> getAllUsers();

    /**
     * Create a new user (super admin only) - redirects to proper endpoints
     *
     * @param request user creation request
     * @return created user profile response
     */
    UserProfileResponse createUser(UpdateProfileRequest request);

    /**
     * Create Admin or Super Admin user (super admin only)
     * Only Super Admin can create Admin/SuperAdmin users.
     *
     * @param request admin creation request
     * @return created admin profile response
     */
    UserProfileResponse createAdminUser(CreateAdminRequest request);

    /**
     * Update user by ID (admin only)
     *
     * @param id user ID
     * @param request update profile request
     * @return updated user profile response
     */
    UserProfileResponse updateUserById(Long id, UpdateProfileRequest request);

    /**
     * Delete user by ID (super admin only)
     *
     * @param id user ID
     */
    void deleteUser(Long id);

    /**
     * Restore deleted user by ID (super admin only)
     *
     * @param id user ID
     */
    void restoreUser(Long id);

    /**
     * Activate user account (admin only)
     *
     * @param id user ID
     */
    void activateUser(Long id);

    /**
     * Deactivate user account (admin only)
     *
     * @param id user ID
     */
    void deactivateUser(Long id);

    /**
     * Reset user password (super admin only)
     *
     * @param id user ID
     */
    void resetUserPassword(Long id);

    /**
     * Unlock a suspended user account and reset failed login attempts (admin only).
     * Use this to reactivate accounts that were suspended due to multiple failed login attempts.
     *
     * @param id user ID
     */
    void unlockUser(Long id);
}

