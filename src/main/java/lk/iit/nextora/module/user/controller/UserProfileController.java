package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.user.dto.request.ChangePasswordRequest;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lk.iit.nextora.module.user.dto.response.UserProfileResponse;
import lk.iit.nextora.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * REST Controller for user profile management
 */
@RestController
@RequestMapping(ApiConstants.USER)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserService userService;

    // ==================== Current User Endpoints ====================

    @GetMapping(ApiConstants.USER_ME)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:READ')")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieve the authenticated user's profile information including profile picture URL"
    )
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ApiResponse.success("Profile retrieved successfully", profile);
    }

    @PutMapping(value = ApiConstants.USER_ME, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @Operation(
            summary = "Update current user profile with optional profile picture",
            description = "Update the authenticated user's profile information. Optionally upload a new profile picture (JPEG, PNG, GIF, WebP, max 5MB) or set deleteProfilePicture=true to remove existing picture."
    )
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(
            @Parameter(description = "First name") @RequestParam(value = "firstName", required = false) String firstName,
            @Parameter(description = "Last name") @RequestParam(value = "lastName", required = false) String lastName,
            @Parameter(description = "Phone number") @RequestParam(value = "phone", required = false) String phone,
            @Parameter(description = "Address") @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Guardian name") @RequestParam(value = "guardianName", required = false) String guardianName,
            @Parameter(description = "Guardian phone number") @RequestParam(value = "guardianPhone", required = false) String guardianPhone,
            @Parameter(description = "Date of birth (format: yyyy-MM-dd)") @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @Parameter(description = "Profile picture file (JPEG, PNG, GIF, WebP). Max 5MB") @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            @Parameter(description = "Set to true to delete existing profile picture") @RequestParam(value = "deleteProfilePicture", required = false, defaultValue = "false") Boolean deleteProfilePicture) {

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .address(address)
                .guardianName(guardianName)
                .guardianPhone(guardianPhone)
                .dateOfBirth(dateOfBirth)
                .build();

        UserProfileResponse profile = userService.updateCurrentUserProfile(request, profilePicture, deleteProfilePicture);
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

