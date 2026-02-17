package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin user management
 */
@RestController
@RequestMapping(ApiConstants.USER_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserManagementController {

    private final UserService userService;

    // ==================== Admin Endpoints ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER:CREATE')")
    @Operation(
            summary = "Create a new user",
            description = "Admin/Super Admin creates a new user (Student, Academic Staff, or Non-Academic Staff). " +
                    "System generates a temporary password and sends credentials via email. " +
                    "User must change password on first login."
    )
    public ApiResponse<UserCreatedResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserCreatedResponse response = userService.createUser(request);
        return ApiResponse.success("User created successfully", response);
    }

    @GetMapping(ApiConstants.ADMIN_USER_STATS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Get user statistics summary",
            description = "Retrieve user statistics including total users, counts by status (Active, Deactivated, Suspended, Deleted, Password Change Required) " +
                    "and counts by role (Students, Admins, Super Admins, Academic Staff, Non-Academic Staff). Admin/Super Admin only."
    )
    public ApiResponse<UserStatsSummaryResponse> getUserStatsSummary() {
        UserStatsSummaryResponse stats = userService.getUserStatsSummary();
        return ApiResponse.success("User statistics retrieved successfully", stats);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve all users with pagination (Admin only)"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = userService.getAllUsers(pageable);
        return ApiResponse.success("Users retrieved successfully", users);
    }

    @GetMapping(ApiConstants.USER_GET_ADMINS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:SUPER_ADMIN_READ')")
    @Operation(
            summary = "Get all admins",
            description = "Retrieve all admins with pagination (Admin only)"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = userService.getAllAdmins(pageable);
        return ApiResponse.success("Admins retrieved successfully", users);
    }

    @GetMapping(ApiConstants.ADMIN_USER_SEARCH)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Search users",
            description = "Search users by email, first name, last name, or full name (Admin/Super Admin only)"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> searchUsers(
            @Parameter(description = "Search keyword (email, first name, last name, or full name)")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = userService.searchUsers(keyword, pageable);
        return ApiResponse.success("Users search completed successfully", users);
    }

    @GetMapping(ApiConstants.ADMIN_USER_FILTER)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Filter users",
            description = "Filter users by role and/or status (Admin/Super Admin only). " +
                    "Available roles: ROLE_STUDENT, ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_ACADEMIC_STAFF, ROLE_NON_ACADEMIC_STAFF. " +
                    "Available statuses: ACTIVE, DEACTIVATED, SUSPENDED, DELETED, PASSWORD_CHANGE_REQUIRED"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> filterUsers(
            @Parameter(description = "Filter by roles (can specify multiple)")
            @RequestParam(required = false) List<UserRole> roles,
            @Parameter(description = "Filter by statuses (can specify multiple)")
            @RequestParam(required = false) List<UserStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = userService.filterUsers(roles, statuses, pageable);
        return ApiResponse.success("Users filtered successfully", users);
    }

    @GetMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by ID including profile picture URL (Admin only)"
    )
    public ApiResponse<UserProfileResponse> getUserById(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserById(id);
        return ApiResponse.success("User retrieved successfully", profile);
    }

    @PutMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_UPDATE')")
    @Operation(
            summary = "Update user by ID",
            description = "Update a specific user's details by ID (Admin/Super Admin only). " +
                    "Only user profile information can be updated. Profile picture must be updated by the user themselves."
    )
    public ApiResponse<UserProfileResponse> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse profile = userService.updateUserById(id, request, null, false);
        return ApiResponse.success("User updated successfully", profile);
    }

    @PutMapping(ApiConstants.USER_ACTIVATE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ACTIVATE')")
    @Operation(
            summary = "Activate user",
            description = "Activate a user account (Admin only)"
    )
    public ApiResponse<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ApiResponse.success("User activated successfully", null);
    }

    @PutMapping(ApiConstants.USER_DEACTIVATE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:DEACTIVATE')")
    @Operation(
            summary = "Deactivate user",
            description = "Deactivate a user account (Admin/Super Admin only)"
    )
    public ApiResponse<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ApiResponse.success("User deactivated successfully", null);
    }

    @PutMapping(ApiConstants.USER_SUSPEND)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:SUSPEND')")
    @Operation(
            summary = "Suspend user",
            description = "Suspend a user account (Admin/Super Admin only). " +
                    "Suspended users cannot log in until unlocked by an admin. " +
                    "Use this for temporary account restrictions due to policy violations or security concerns."
    )
    public ApiResponse<Void> suspendUser(
            @PathVariable Long id,
            @Parameter(description = "Reason for suspension (optional)")
            @RequestParam(required = false) String reason) {
        userService.suspendUser(id, reason);
        return ApiResponse.success("User suspended successfully", null);
    }

    @PutMapping(ApiConstants.USER_UNLOCK)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:UNLOCK')")
    @Operation(
            summary = "Unlock suspended user",
            description = "Unlock a suspended user account and reset failed login attempts (Admin only). " +
                    "Use this to reactivate accounts that were suspended due to multiple failed login attempts."
    )
    public ApiResponse<Void> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ApiResponse.success("User account unlocked successfully", null);
    }

    // ==================== Super Admin Endpoints ====================

    @PostMapping(ApiConstants.ADMIN_USER)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER:ADMIN_CREATE')")
    @Operation(
            summary = "Create Admin user",
            description = "Create a new Admin user (Super Admin only). " +
                    "Note: Super Admin cannot be created via API - only one Super Admin exists in the system (created via DataInitializer)."
    )
    public ApiResponse<UserProfileResponse> createAdminUser(@Valid @RequestBody CreateAdminRequest request) {
        UserProfileResponse profile = userService.createAdminUser(request);
        return ApiResponse.success("Admin user created successfully", profile);
    }

    @DeleteMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_DELETE')")
    @Operation(
            summary = "Soft delete user",
            description = "Soft delete a user by ID (Admin/Super Admin). " +
                    "This will disable the account, remove profile picture from storage, and mark user as deleted. " +
                    "The user can be restored later using the restore endpoint."
    )
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User soft deleted successfully", null);
    }

    @PutMapping(ApiConstants.USER_RESTORE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:RESTORE')")
    @Operation(
            summary = "Restore user",
            description = "Restore a soft-deleted user by ID (Super Admin only). " +
                    "Note: Profile picture will need to be re-uploaded by the user."
    )
    public ApiResponse<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ApiResponse.success("User restored successfully", null);
    }

    @PutMapping(ApiConstants.USER_RESET_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:RESET_PASSWORD')")
    @Operation(
            summary = "Reset user password",
            description = "Reset a user's password and send new credentials via email (Super Admin only)"
    )
    public ApiResponse<Void> resetUserPassword(@PathVariable Long id) {
        userService.resetUserPassword(id);
        return ApiResponse.success("Password reset email sent successfully", null);
    }

    @DeleteMapping(ApiConstants.USER_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:PERMANENT_DELETE')")
    @Operation(
            summary = "Permanently delete user",
            description = "DANGER: Permanently delete a user from the database (Super Admin only). " +
                    "This action is IRREVERSIBLE and will remove all user data including profile picture. " +
                    "Super Admin accounts cannot be permanently deleted."
    )
    public ApiResponse<Void> permanentlyDeleteUser(@PathVariable Long id) {
        userService.permanentlyDeleteUser(id);
        return ApiResponse.success("User permanently deleted", null);
    }
}

