package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user profile management
 */
@RestController
@RequestMapping(ApiConstants.USER_ADMIN)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserManagementController {

    private final UserService userService;

    // ==================== Admin Endpoints ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_CREATE')")
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Get all users",
            description = "Retrieve all users (Admin only)"
    )
    public ApiResponse<List<UserSummaryResponse>> getAllUsers() {
        List<UserSummaryResponse> users = userService.getAllUsers();
        return ApiResponse.success("Users retrieved successfully", users);
    }

    @GetMapping(ApiConstants.USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:ADMIN_READ')")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by ID (Admin only)"
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
            description = "Update a specific user by ID (Admin only)"
    )
    public ApiResponse<UserProfileResponse> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateUserById(id, request);
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
    @PreAuthorize("hasAuthority('USER:ACTIVATE')")
    @Operation(
            summary = "Deactivate user",
            description = "Deactivate a user account (Admin only)"
    )
    public ApiResponse<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ApiResponse.success("User deactivated successfully", null);
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
            summary = "Delete user",
            description = "Delete (disable) a user by ID (Super Admin only)"
    )
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("User deleted successfully", null);
    }

    @PutMapping(ApiConstants.USER_RESTORE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:RESTORE')")
    @Operation(
            summary = "Restore user",
            description = "Restore a deleted user by ID (Super Admin only)"
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
            description = "Reset a user's password (Super Admin only)"
    )
    public ApiResponse<Void> resetUserPassword(@PathVariable Long id) {
        userService.resetUserPassword(id);
        return ApiResponse.success("Password reset email sent successfully", null);
    }
}

