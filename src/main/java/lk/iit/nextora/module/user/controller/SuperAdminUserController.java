package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.user.dto.request.UpdateAdminRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.dto.response.UserSummaryResponse;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Super Admin to manage Admin users.
 *
 * <p>This controller provides endpoints exclusively for Super Admin to:
 * <ul>
 *   <li>View all admin users</li>
 *   <li>View specific admin user details</li>
 *   <li>Update admin user information</li>
 *   <li>Soft delete admin users (recoverable)</li>
 *   <li>Permanently delete admin users (irreversible)</li>
 *   <li>Restore soft-deleted admin users</li>
 * </ul>
 *
 * <p>All endpoints require SUPER_ADMIN role with appropriate permissions.
 *
 * @author Nextora Development Team
 * @version 1.0.0
 * @since 2026-02-18
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.ADMIN_USER_MANAGEMENT)
@RequiredArgsConstructor
@Tag(name = "Super Admin - Admin User Management",
     description = "Endpoints for Super Admin to manage Admin users. These operations are restricted to Super Admin only.")
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminUserController {

    private final UserService userService;

    // ==================== Read Operations ====================

    /**
     * Retrieves all admin users with pagination support.
     * Only accessible by Super Admin.
     *
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param sortDirection Sort direction (ASC or DESC)
     * @return Paginated list of admin users
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:READ')")
    @Operation(
            summary = "Get all admin users",
            description = """
                Retrieves all admin users with pagination support.
                
                **Access Control:** Super Admin only
                
                **Note:** This endpoint returns only ROLE_ADMIN users, excluding ROLE_SUPER_ADMIN.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<PagedResponse<UserSummaryResponse>> getAllAdminUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Super Admin requesting all admin users - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<UserSummaryResponse> adminUsers = userService.getAllAdminUsers(pageable);

        return ApiResponse.success("Admin users retrieved successfully", adminUsers);
    }

    /**
     * Retrieves a specific admin user by ID.
     * Only accessible by Super Admin.
     *
     * @param adminId The ID of the admin user to retrieve
     * @return Admin user profile details
     */
    @GetMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:READ')")
    @Operation(
            summary = "Get admin user by ID",
            description = """
                Retrieves detailed information about a specific admin user.
                
                **Access Control:** Super Admin only
                
                **Note:** Only ROLE_ADMIN users can be retrieved. Attempting to retrieve a Super Admin will result in an error.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin user retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Admin user not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<UserProfileResponse> getAdminUserById(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {

        log.info("Super Admin requesting admin user details for ID: {}", adminId);

        UserProfileResponse adminProfile = userService.getAdminUserById(adminId);

        return ApiResponse.success("Admin user retrieved successfully", adminProfile);
    }

    // ==================== Update Operations ====================

    /**
     * Updates an admin user's information.
     * Only accessible by Super Admin.
     *
     * @param adminId The ID of the admin user to update
     * @param request The update request containing new values
     * @return Updated admin user profile
     */
    @PutMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:UPDATE')")
    @Operation(
            summary = "Update admin user",
            description = """
                Updates an admin user's information including:
                - Email address
                - Password (will be securely hashed)
                - Personal information (first name, last name, phone)
                - Department
                - Permissions
                
                **Access Control:** Super Admin only
                
                **Note:** All fields are optional. Only provided fields will be updated.
                If permissions are provided, they will replace all existing permissions.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin user updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid input data or email already in use"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Admin user not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<UserProfileResponse> updateAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminRequest request) {

        log.info("Super Admin updating admin user with ID: {}", adminId);

        UserProfileResponse updatedProfile = userService.updateAdminUser(adminId, request);

        return ApiResponse.success("Admin user updated successfully", updatedProfile);
    }

    // ==================== Delete Operations ====================

    /**
     * Soft deletes an admin user.
     * The admin user can be restored later using the restore endpoint.
     * Only accessible by Super Admin.
     *
     * @param adminId The ID of the admin user to soft delete
     */
    @DeleteMapping(ApiConstants.ADMIN_USER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:SOFT_DELETE')")
    @Operation(
            summary = "Soft delete admin user",
            description = """
                Soft deletes an admin user by:
                - Setting status to DELETED
                - Marking account as inactive
                - Removing profile picture from storage
                
                **Access Control:** Super Admin only
                
                **Recovery:** The admin user can be restored using the restore endpoint.
                
                **Note:** Super Admin cannot delete their own account.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin user soft deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Cannot delete yourself or user is already deleted"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Admin user not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<Void> softDeleteAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {

        log.warn("Super Admin initiating soft delete for admin user ID: {}", adminId);

        userService.softDeleteAdminUser(adminId);

        return ApiResponse.success("Admin user soft deleted successfully", null);
    }

    /**
     * Permanently deletes an admin user from the database.
     * This action is IRREVERSIBLE.
     * Only accessible by Super Admin.
     *
     * @param adminId The ID of the admin user to permanently delete
     */
    @DeleteMapping(ApiConstants.ADMIN_USER_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:PERMANENT_DELETE')")
    @Operation(
            summary = "Permanently delete admin user",
            description = """
                ⚠️ **DANGER ZONE** ⚠️
                
                Permanently removes an admin user from the database.
                
                **This action is IRREVERSIBLE!**
                
                All data associated with the admin user will be permanently lost:
                - User account information
                - Profile picture
                - Permissions
                - Activity history
                
                **Access Control:** Super Admin only
                
                **Note:** Super Admin cannot delete their own account.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin user permanently deleted"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Cannot delete yourself"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Admin user not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<Void> permanentlyDeleteAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {

        log.warn("CRITICAL: Super Admin initiating PERMANENT delete for admin user ID: {}", adminId);

        userService.permanentlyDeleteAdminUser(adminId);

        return ApiResponse.success("Admin user permanently deleted", null);
    }

    // ==================== Restore Operations ====================

    /**
     * Restores a soft-deleted admin user.
     * Only accessible by Super Admin.
     *
     * @param adminId The ID of the admin user to restore
     */
    @PutMapping(ApiConstants.ADMIN_USER_RESTORE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN_USER:RESTORE')")
    @Operation(
            summary = "Restore deleted admin user",
            description = """
                Restores a soft-deleted admin user by:
                - Setting status back to ACTIVE
                - Re-enabling the account
                
                **Access Control:** Super Admin only
                
                **Note:** Profile picture will need to be re-uploaded by the admin user.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Admin user restored successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Admin user is not in deleted state"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Admin user not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have Super Admin privileges"
            )
    })
    public ApiResponse<Void> restoreAdminUser(
            @Parameter(description = "Admin user ID", required = true, example = "1")
            @PathVariable Long adminId) {

        log.info("Super Admin restoring admin user with ID: {}", adminId);

        userService.restoreAdminUser(adminId);

        return ApiResponse.success("Admin user restored successfully", null);
    }
}

