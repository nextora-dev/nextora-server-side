package lk.iit.nextora.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.config.security.jwt.JwtBlacklistService;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.usecase.LoginUseCase;
import lk.iit.nextora.module.auth.usecase.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.AUTH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Multi-role authentication endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping(ApiConstants.AUTH_REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register for all roles",
            description = "Register new user - request body varies by role"
    )
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = registerUseCase.execute(request);
        return ApiResponse.success("Registration successful", response);
    }

    @PostMapping(ApiConstants.AUTH_LOGIN)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Login for all roles",
            description = "Authenticate user with email, password, and role"
    )
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.execute(request);
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


}