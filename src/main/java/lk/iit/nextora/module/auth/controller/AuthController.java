package lk.iit.nextora.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.usecase.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.AUTH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Multi-role authentication endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;

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
}