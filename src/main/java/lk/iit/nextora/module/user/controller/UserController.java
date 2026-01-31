package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
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
@RequestMapping(ApiConstants.USER)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ==================== Current User Endpoints ====================

    @GetMapping(ApiConstants.USER_ME)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:READ')")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieve the authenticated user's profile information"
    )
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ApiResponse.success("Profile retrieved successfully", profile);
    }

    @PutMapping(ApiConstants.USER_ME)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @Operation(
            summary = "Update current user profile",
            description = "Update the authenticated user's profile information"
    )
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateCurrentUserProfile(request);
        return ApiResponse.success("Profile updated successfully", profile);
    }

    @PutMapping(ApiConstants.USER_CHANGE_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @Operation(
            summary = "Change password",
            description = "Change the authenticated user's password"
    )
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("Password changed successfully", null);
    }
}

