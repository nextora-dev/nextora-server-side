package lk.iit.nextora.module.auth.usecase;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationService authenticationService;
    private final AuthMapper authMapper;
    private final UserResponseMapper userResponseMapper;

    public AuthResponse execute(LoginRequest request) {
        // Validate input
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");

        log.info("Login attempt for: {} as role: {}", StringUtils.maskEmail(request.getEmail()), request.getRole());

        try {
            // Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Find user by email and role
            BaseUser user = authenticationService
                    .findUserByEmailAndRole(request.getEmail(), request.getRole())
                    .orElseThrow(() -> new BadRequestException(
                            "No account found with email " + StringUtils.maskEmail(request.getEmail()) +
                                    " for role " + request.getRole().getDisplayName()
                    ));

            // Verify user is active
            ValidationUtils.requireTrue(user.isActive(), "Account is inactive");

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(user);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            log.info("User logged in successfully: {} - {}",
                    StringUtils.maskEmail(user.getEmail()), user.getUserType());

            // Use mappers to build response
            return authMapper.toAuthResponseWithRoleData(
                    user,
                    accessToken,
                    refreshToken,
                    tokenProvider.getAccessTokenExpiryDate(),
                    userResponseMapper.extractRoleSpecificData(user)
            );

        } catch (AuthenticationException ex) {
            log.error("Authentication failed: {}", StringUtils.maskEmail(request.getEmail()));
            throw new BadRequestException("Invalid email or password");
        }
    }
}