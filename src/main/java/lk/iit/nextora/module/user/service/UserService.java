package lk.iit.nextora.module.user.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserStatsSummaryResponse getUserStatsSummary();

    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    void changePassword(ChangePasswordRequest request);

    UserProfileResponse getUserById(Long id);

    PagedResponse<UserSummaryResponse> getAllNormalUsers(Pageable pageable);

    PagedResponse<UserSummaryResponse> getAllUsers(Pageable pageable);

    PagedResponse<UserSummaryResponse> searchUsers(String keyword, Pageable pageable);

    PagedResponse<UserSummaryResponse> filterUsers(List<UserRole> roles, List<UserStatus> statuses, Pageable pageable);

    UserProfileResponse createAdminUser(CreateAdminRequest request);

    UserProfileResponse updateUserById(Long id, UpdateProfileRequest request, MultipartFile profilePicture, Boolean deleteProfilePicture);

    void deleteUser(Long id);

    void restoreUser(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

    void suspendUser(Long id, String reason);

    void resetUserPassword(Long id);

    void unlockUser(Long id);

    UserCreatedResponse createUser(AdminCreateUserRequest request);

    void permanentlyDeleteUser(Long id);

    // ==================== Admin User Management by Super Admin ====================

    /**
     * Get all admin users (Super Admin only)
     * @param pageable Pagination information
     * @return Paginated list of admin users
     */
    PagedResponse<UserSummaryResponse> getAllAdminUsers(Pageable pageable);

    /**
     * Get admin user by ID (Super Admin only)
     * @param adminId The ID of the admin user
     * @return Admin user profile
     */
    UserProfileResponse getAdminUserById(Long adminId);

    /**
     * Update admin user (Super Admin only)
     * @param adminId The ID of the admin user to update
     * @param request The update request containing new admin details
     * @return Updated admin profile
     */
    UserProfileResponse updateAdminUser(Long adminId, UpdateAdminRequest request);

    /**
     * Soft delete admin user (Super Admin only)
     * Sets status to DELETED and isActive to false
     * @param adminId The ID of the admin user to soft delete
     */
    void softDeleteAdminUser(Long adminId);

    /**
     * Permanently delete admin user from database (Super Admin only)
     * This action is IRREVERSIBLE
     * @param adminId The ID of the admin user to permanently delete
     */
    void permanentlyDeleteAdminUser(Long adminId);

    /**
     * Restore a soft-deleted admin user (Super Admin only)
     * @param adminId The ID of the admin user to restore
     */
    void restoreAdminUser(Long adminId);
}

