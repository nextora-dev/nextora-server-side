package lk.iit.nextora.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.auth.dto.request.AdminCreateUserRequest;
import lk.iit.nextora.module.auth.dto.request.FirstTimePasswordChangeRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.auth.service.AdminUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Endpoints for Admin/Super Admin to create and manage users")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Create a new user",
            description = "Admin/Super Admin creates a new user (Student, Academic Staff, or Non-Academic Staff). " +
                    "System generates a temporary password and sends credentials via email. " +
                    "User must change password on first login."
    )
    public ApiResponse<UserCreatedResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserCreatedResponse response = adminUserManagementService.createUser(request);
        return ApiResponse.success("User created successfully", response);
    }
}
