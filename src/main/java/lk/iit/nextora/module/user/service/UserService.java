package lk.iit.nextora.module.user.service;

import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
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
}

