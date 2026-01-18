package lk.iit.nextora.module.auth.usecase;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
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
import lk.iit.nextora.module.auth.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private final LoginAttemptService loginAttemptService;

    public AuthResponse execute(LoginRequest request) {
        // Validate input
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");

        log.info("Login attempt for: {} as role: {}", StringUtils.maskEmail(request.getEmail()), request.getRole());

        // First, check if user exists and if account is suspended
        BaseUser user = authenticationService
                .findUserByEmailAndRole(request.getEmail(), request.getRole())
                .orElse(null);

        if (user != null) {
            // Check if account is suspended
            if (UserStatus.SUSPENDED.equals(user.getStatus())) {
                // Check if it's a new day - auto-unlock if suspension was from a previous day
                LocalDateTime lastFailedAt = user.getLastFailedLoginAt();
                boolean isNewDay = lastFailedAt == null ||
                                   !lastFailedAt.toLocalDate().equals(LocalDateTime.now().toLocalDate());

                if (isNewDay) {
                    // Auto-unlock the account for the new day
                    loginAttemptService.resetFailedAttempts(user.getId());
                    // Refresh user object
                    user = authenticationService.findUserByEmailAndRole(request.getEmail(), request.getRole())
                            .orElse(null);
                    log.info("Account auto-unlocked for new day: {}", StringUtils.maskEmail(request.getEmail()));
                } else {
                    // Still suspended for today
                    log.warn("Login attempt for suspended account: {}", StringUtils.maskEmail(request.getEmail()));
                    throw new BadRequestException(
                            "Your account has been suspended due to multiple failed login attempts today. " +
                            "Please try again tomorrow or contact an administrator to reactivate your account."
                    );
                }
            }
        }

        try {
            // Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Find user by email and role (should exist at this point)
            if (user == null) {
                throw new BadRequestException(
                        "No account found with email " + StringUtils.maskEmail(request.getEmail()) +
                                " for role " + request.getRole().getDisplayName()
                );
            }

            // Verify user is active
            ValidationUtils.requireTrue(user.isActive(), "Account is inactive");

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
                loginAttemptService.resetFailedAttempts(user.getId());
                log.info("Reset failed login attempts for user: {}", StringUtils.maskEmail(user.getEmail()));
            }

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
            log.error("Authentication failed for: {}", StringUtils.maskEmail(request.getEmail()));

            // Handle failed login attempt for non-admin/super-admin users
            // This runs in a separate transaction so it persists even if we throw an exception
            if (user != null) {
                boolean suspended = loginAttemptService.recordFailedAttempt(user.getId(), user.getRole());
                if (suspended) {
                    throw new BadRequestException(
                            "Your account has been suspended due to 5 failed login attempts today. " +
                            "Please try again tomorrow or contact an administrator to reactivate your account."
                    );
                }
            }

            throw new BadRequestException("Invalid email or password");
        }
    }
}