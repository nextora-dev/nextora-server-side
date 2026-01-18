package lk.iit.nextora.module.auth.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @PersistenceContext
    private EntityManager entityManager;

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
                    user.setStatus(UserStatus.ACTIVE);
                    user.setFailedLoginAttempts(0);
                    entityManager.merge(user);
                    entityManager.flush();
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
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLastFailedLoginAt(null);
                entityManager.merge(user);
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
            log.error("Authentication failed: {}", StringUtils.maskEmail(request.getEmail()));

            // Handle failed login attempt for non-admin/super-admin users
            if (user != null) {
                handleFailedLoginAttempt(user);
            }

            throw new BadRequestException("Invalid email or password");
        }
    }

    /**
     * Handle failed login attempt.
     * Increment failed attempts counter and suspend account after MAX_FAILED_ATTEMPTS per day
     * for non-admin/super-admin users.
     * The counter resets each day at midnight.
     */
    private void handleFailedLoginAttempt(BaseUser user) {
        // Admin and Super Admin are exempt from account lockout
        if (UserRole.ROLE_ADMIN.equals(user.getRole()) || UserRole.ROLE_SUPER_ADMIN.equals(user.getRole())) {
            log.info("Admin/SuperAdmin failed login - not tracking attempts for: {}",
                    StringUtils.maskEmail(user.getEmail()));
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastFailedAt = user.getLastFailedLoginAt();

        // Check if it's a new day - reset attempts if last failure was on a different day
        boolean isNewDay = lastFailedAt == null ||
                           !lastFailedAt.toLocalDate().equals(now.toLocalDate());

        int currentAttempts;
        if (isNewDay) {
            // Reset counter for new day
            currentAttempts = 0;
            log.info("New day - resetting failed login attempts for user: {}",
                    StringUtils.maskEmail(user.getEmail()));
        } else {
            currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        }

        int newAttempts = currentAttempts + 1;

        user.setFailedLoginAttempts(newAttempts);
        user.setLastFailedLoginAt(now);

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            // Suspend the account
            user.setStatus(UserStatus.SUSPENDED);
            log.warn("Account suspended due to {} failed login attempts today: {}",
                    newAttempts, StringUtils.maskEmail(user.getEmail()));
        } else {
            int remainingAttempts = MAX_FAILED_ATTEMPTS - newAttempts;
            log.warn("Failed login attempt {} of {} for today. {} attempts remaining for user: {}",
                    newAttempts, MAX_FAILED_ATTEMPTS, remainingAttempts, StringUtils.maskEmail(user.getEmail()));
        }

        entityManager.merge(user);
        entityManager.flush();
    }
}