package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.user.dto.request.CreateAdminRequest;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.SuperAdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.USER_SUPER_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Super Admin - Admin User Management",
     description = "Endpoints for Super Admin to manage Admin users. These operations are restricted to Super Admin only.")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminUserController {

    private final SuperAdminUserService superAdminUserService;

    // ==================== Super Admin Endpoints ====================

    @PostMapping(ApiConstants.ADMIN_USER)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN_USER:CREATE')")
    @Operation(
            summary = "Create Admin user",
            description = "Create a new Admin user (Super Admin only). "
    )
    public ApiResponse<UserProfileResponse> createAdminUser(@Valid @RequestBody CreateAdminRequest request) {
        UserProfileResponse profile = superAdminUserService.createAdminUser(request);
        return ApiResponse.success("Admin user created successfully", profile);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:READ')")
    @Operation(
            summary = "Get all admin users",
            description = "Retrieves all admin users with pagination support"
    )
    public ApiResponse<PagedResponse<UserSummaryResponse>> getAllAdminUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserSummaryResponse> adminUsers = superAdminUserService.getAllAdminUsers(pageable);
        return ApiResponse.success("Admin users retrieved successfully", adminUsers);
    }

    @GetMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:READ')")
    @Operation(
            summary = "Get admin user by ID",
            description = "Retrieves detailed information about a specific admin user."
    )
    public ApiResponse<UserProfileResponse> getAdminUserById(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {
        UserProfileResponse adminProfile = superAdminUserService.getAdminUserById(adminId);
        return ApiResponse.success("Admin user retrieved successfully", adminProfile);
    }

    @PutMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:UPDATE')")
    @Operation(
            summary = "Update admin user",
            description = "Updates an admin user"
    )
    public ApiResponse<UserProfileResponse> updateAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminRequest request) {
        UserProfileResponse updatedProfile = superAdminUserService.updateAdminUser(adminId, request);
        return ApiResponse.success("Admin user updated successfully", updatedProfile);
    }

    @DeleteMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:SOFT_DELETE')")
    @Operation(
            summary = "Soft delete admin user",
            description = "Soft deletes an admin user "
    )
    public ApiResponse<Void> softDeleteAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {
        superAdminUserService.softDeleteAdminUser(adminId);
        return ApiResponse.success("Admin user soft deleted successfully", null);
    }

    @DeleteMapping(ApiConstants.ADMIN_USER_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:PERMANENT_DELETE')")
    @Operation(
            summary = "Permanently delete admin user",
            description = "Permanently removes an admin user from the database. This action is IRREVERSIBLE."
    )
    public ApiResponse<Void> permanentlyDeleteAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {
        superAdminUserService.permanentlyDeleteUser(adminId);
        return ApiResponse.success("Admin user permanently deleted", null);
    }

    @PutMapping(ApiConstants.ADMIN_USER_RESTORE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:RESTORE')")
    @Operation(
            summary = "Restore deleted admin user",
            description = "Restores a soft-deleted admin user"
    )
    public ApiResponse<Void> restoreAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {
        superAdminUserService.restoreUser(adminId);
        return ApiResponse.success("Admin user restored successfully", null);
    }

    // ==================== Normal User Management (Restore & Permanent Delete) ====================

    @PutMapping(ApiConstants.NORMAL_USER_RESTORE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:RESTORE')")
    @Operation(
            summary = "Restore deleted normal user",
            description = "Restore a soft-deleted normal user by ID (Super Admin only). " +
                    "Note: Profile picture will need to be re-uploaded by the user."
    )
    public ApiResponse<Void> restoreNormalUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        superAdminUserService.restoreUser(userId);
        return ApiResponse.success("User restored successfully", null);
    }

    @DeleteMapping(ApiConstants.NORMAL_USER_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('NORMAL_USER:PERMANENT_DELETE')")
    @Operation(
            summary = "Permanently delete normal user",
            description = "DANGER: Permanently delete a normal user from the database (Super Admin only). " +
                    "This action is IRREVERSIBLE and will remove all user data including profile picture."
    )
    public ApiResponse<Void> permanentlyDeleteNormalUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        superAdminUserService.permanentlyDeleteUser(userId);
        return ApiResponse.success("User permanently deleted", null);
    }
}

