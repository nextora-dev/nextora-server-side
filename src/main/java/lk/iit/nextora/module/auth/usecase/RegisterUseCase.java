package lk.iit.nextora.module.auth.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.factory.RegistrationStrategyFactory;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lk.iit.nextora.module.auth.strategy.RegistrationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuthMapper authMapper;
    private final UserResponseMapper userResponseMapper;

    public AuthResponse execute(RegisterRequest request) {
        // Validate input using ValidationUtils
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireMinLength(request.getPassword(), 8, "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");
        ValidationUtils.requireNonBlank(request.getFirstName(), "First name");
        ValidationUtils.requireNonBlank(request.getLastName(), "Last name");

        log.info("Registration attempt for: {} as role: {}",
                StringUtils.maskEmail(request.getEmail()), request.getRole());

        // Validate passwords match
        ValidationUtils.requireEquals(
                request.getPassword(),
                request.getConfirmPassword(),
                "Passwords do not match"
        );

        // Check if email already exists
        ValidationUtils.requireFalse(
                authenticationService.emailExists(request.getEmail()),
                "Email already registered"
        );

        // Get registration strategy for role
        RegistrationStrategy strategy = registrationStrategyFactory.getStrategy(request.getRole());

        // Validate role-specific fields
        strategy.validate(request);

        // Map to entity
        BaseUser user = strategy.mapToEntity(request);

        // Set common fields
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Persist user
        entityManager.persist(user);
        entityManager.flush();

        // Post-registration processing
        strategy.postRegistration(user);

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {} - {}",
                StringUtils.maskEmail(user.getEmail()), user.getUserType());

        // Build response using mappers
        return authMapper.toAuthResponseWithRoleData(
                user,
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpiryDate(),
                userResponseMapper.extractRoleSpecificData(user)
        );
    }
}
