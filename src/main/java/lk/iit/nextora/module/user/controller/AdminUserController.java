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
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserStatsSummaryResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.AdminUserService;
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
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ==================== Admin Endpoints ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('NORMAL_USER:CREATE')")
    @Operation(
            summary = "Create a new user",
            description = "Admin/Super Admin creates a new user (Student, Academic Staff, or Non-Academic Staff). " +
                    "System generates a temporary password and sends credentials via email. " +
                    "User must change password on first login."
    )
    public ApiResponse<UserCreatedResponse> createNormalUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserCreatedResponse response = adminUserService.createNormalUser(request);
        return ApiResponse.success("User created successfully", response);
    }

    @GetMapping(ApiConstants.ADMIN_USER_STATS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:READ')")
    @Operation(
            summary = "Get user statistics summary",
            description = "Retrieve user statistics including total users, counts by status (Active, Deactivated, Suspended, Deleted, Password Change Required) " +
                    "and counts by role (Students, Academic Staff, Non-Academic Staff). Admin/Super Admin only."
    )
    public ApiResponse<UserStatsSummaryResponse> getUserStatsSummary() {
        UserStatsSummaryResponse stats = adminUserService.getUserStatsSummary();
        return ApiResponse.success("User statistics retrieved successfully", stats);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:READ')")
    @Operation(
            summary = "Get all normal users",
            description = "Retrieve all normal users with pagination (Admin only)"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> getAllNormalUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = adminUserService.getAllNormalUsers(pageable);
        return ApiResponse.success("Normal users retrieved successfully", users);
    }

    @GetMapping(ApiConstants.ADMIN_USER_SEARCH)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:READ')")
    @Operation(
            summary = "Search users",
            description = "Search users by email, first name, last name, or full name (Admin/Super Admin only)"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> searchNormalUsers(
            @Parameter(description = "Search keyword (email, first name, last name, or full name)")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> users = adminUserService.searchNormalUsers(keyword, pageable);
        return ApiResponse.success("Users search completed successfully", users);
    }

    @GetMapping(ApiConstants.ADMIN_USER_FILTER)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:READ')")
    @Operation(
            summary = "Filter users",
            description = "Filter normal users by role and/or status (Admin/Super Admin only). " +
                    "Available roles: ROLE_STUDENT, ROLE_ACADEMIC_STAFF, ROLE_NON_ACADEMIC_STAFF. " +
                    "Available statuses: ACTIVE, DEACTIVATED, SUSPENDED, DELETED, PASSWORD_CHANGE_REQUIRED"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> filterNormalUsers(
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
        PagedResponse<UserSummaryResponse> users = adminUserService.filterNormalUsers(roles, statuses, pageable);
        return ApiResponse.success("Users filtered successfully", users);
    }

    @GetMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:READ')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by ID including profile picture URL (Admin only)"
    )
    public ApiResponse<UserProfileResponse> getNormalUserById(@PathVariable Long id) {
        UserProfileResponse profile = adminUserService.getNormalUserById(id);
        return ApiResponse.success("User retrieved successfully", profile);
    }

    @PutMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:UPDATE')")
    @Operation(
            summary = "Update user by ID",
            description = "Update a specific user's details by ID (Admin/Super Admin only). " +
                    "Only user profile information can be updated. Profile picture must be updated by the user themselves."
    )
    public ApiResponse<UserProfileResponse> updateNormalUserById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse profile = adminUserService.updateNormalUserById(id, request, null, false);
        return ApiResponse.success("User updated successfully", profile);
    }

    @PutMapping(ApiConstants.USER_ACTIVATE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:ACTIVATE')")
    @Operation(
            summary = "Activate user",
            description = "Activate a user account (Admin only)"
    )
    public ApiResponse<Void> activateNormalUser(@PathVariable Long id) {
        adminUserService.activateNormalUser(id);
        return ApiResponse.success("User activated successfully", null);
    }

    @PutMapping(ApiConstants.USER_DEACTIVATE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:DEACTIVATE')")
    @Operation(
            summary = "Deactivate user",
            description = "Deactivate a user account (Admin/Super Admin only)"
    )
    public ApiResponse<Void> deactivateNormalUser(@PathVariable Long id) {
        adminUserService.deactivateNormalUser(id);
        return ApiResponse.success("User deactivated successfully", null);
    }

    @PutMapping(ApiConstants.USER_SUSPEND)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:SUSPEND')")
    @Operation(
            summary = "Suspend user",
            description = "Suspend a user account (Admin/Super Admin only). " +
                    "Suspended users cannot log in until unlocked by an admin. " +
                    "Use this for temporary account restrictions due to policy violations or security concerns."
    )
    public ApiResponse<Void> suspendNormalUser(
            @PathVariable Long id,
            @Parameter(description = "Reason for suspension (optional)")
            @RequestParam(required = false) String reason) {
        adminUserService.suspendNormalUser(id, reason);
        return ApiResponse.success("User suspended successfully", null);
    }

    @PutMapping(ApiConstants.USER_UNLOCK)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:UNLOCK')")
    @Operation(
            summary = "Unlock suspended user",
            description = "Unlock a suspended user account and reset failed login attempts (Admin only). " +
                    "Use this to reactivate accounts that were suspended due to multiple failed login attempts."
    )
    public ApiResponse<Void> unlockNormalUser(@PathVariable Long id) {
        adminUserService.unlockNormalUser(  id);
        return ApiResponse.success("User account unlocked successfully", null);
    }

    @DeleteMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:DELETE')")
    @Operation(
            summary = "Soft delete user",
            description = "Soft delete a user by ID (Admin/Super Admin). " +
                    "This will disable the account, remove profile picture from storage, and mark user as deleted. " +
                    "The user can be restored later using the restore endpoint."
    )
    public ApiResponse<Void> deleteNormalUser(@PathVariable Long id) {
        adminUserService.deleteNormalUser(id);
        return ApiResponse.success("User soft deleted successfully", null);
    }

    @PutMapping(ApiConstants.USER_RESET_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:RESET_PASSWORD')")
    @Operation(
            summary = "Reset user password",
            description = "Reset a user's password and send new credentials via email (Super Admin only)"
    )
    public ApiResponse<Void> resetNormalUserPassword(@PathVariable Long id) {
        adminUserService.resetNormalUserPassword(id);
        return ApiResponse.success("Password reset email sent successfully", null);
    }
}

