package lk.iit.nextora.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.config.security.jwt.JwtBlacklistService;
import lk.iit.nextora.module.auth.dto.request.ForgotPasswordRequest;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.request.ResetPasswordRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.ForgotPasswordResponse;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.AUTH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints - Login, Logout, and Password Reset")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping(ApiConstants.AUTH_LOGIN)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Login for all roles",
            description = "Authenticate user with email, password, and role. " +
                    "Users created by admin must change password on first login. " +
                    "If status is PASSWORD_CHANGE_REQUIRED, use /api/v1/admin/users/change-password-first-login endpoint."
    )
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping(ApiConstants.AUTH_LOGOUT)
    @Operation(summary = "Logout", description = "Logout user and blacklist JWT token")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        jwtBlacklistService.blacklistToken(token);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // ==================== FORGOT PASSWORD ENDPOINTS ====================

    @PostMapping(ApiConstants.AUTH_FORGOT_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Initiate forgot password",
            description = "Send password reset token to user's email. " +
                    "For security, always returns success message regardless of whether email exists."
    )
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = authenticationService.initiatePasswordReset(request);
        return ApiResponse.success(response.getMessage(), response);
    }

    @PostMapping(ApiConstants.AUTH_RESET_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Reset password with token",
            description = "Reset user's password using the verified token. " +
                    "New password must meet complexity requirements."
    )
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.success("Password has been reset successfully. You can now log in with your new password.", null);
    }

}