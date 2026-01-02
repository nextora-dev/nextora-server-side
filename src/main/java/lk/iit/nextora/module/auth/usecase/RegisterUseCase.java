package lk.iit.nextora.module.auth.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.config.security.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.factory.RegistrationStrategyFactory;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lk.iit.nextora.module.auth.strategy.RegistrationStrategy;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUseCase {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationService authenticationService;
    private final RegistrationStrategyFactory registrationStrategyFactory;

    public AuthResponse execute(RegisterRequest request) {
        log.info("Registration attempt for: {} as role: {}", request.getEmail(), request.getRole());

        // 1. Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // 2. Check if email already exists
        if (authenticationService.emailExists(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // 3. Resolve role
        UserRole role = resolveRole(String.valueOf(request.getRole()));

        // 4. Get role-specific strategy
        RegistrationStrategy strategy = registrationStrategyFactory.getStrategy(role);

        // 5. Validate role-specific data
        strategy.validate(request);

        // 6. Map DTO to entity
        BaseUser user = strategy.mapToEntity(request);

        // 7. Set common fields
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        // 8. Save user
        entityManager.persist(user);
        entityManager.flush();

        // 9. Post-registration actions
        strategy.postRegistration(user);

        // 10. Generate JWT tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {} - {}", user.getEmail(), user.getUserType());

        // 11. Return response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiryDate())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .userType(user.getUserType())
                .build();
    }

    private UserRole resolveRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return UserRole.ROLE_STUDENT;
        }

        String normalized = roleStr.toUpperCase().startsWith("ROLE_")
                ? roleStr.toUpperCase()
                : "ROLE_" + roleStr.toUpperCase();

        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: " + roleStr);
        }
    }
}
