package lk.iit.nextora.module.auth.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.exception.custom.BadRequestException;
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
        log.info("Registration attempt for: {} as role: {}", request.getEmail(), request.getRole());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (authenticationService.emailExists(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        RegistrationStrategy strategy = registrationStrategyFactory.getStrategy(request.getRole());

        strategy.validate(request);

        BaseUser user = strategy.mapToEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        entityManager.persist(user);
        entityManager.flush();

        strategy.postRegistration(user);

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {} - {}", user.getEmail(), user.getUserType());

        // Use mappers to build response
        return authMapper.toAuthResponseWithRoleData(
                user,
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpiryDate(),
                userResponseMapper.extractRoleSpecificData(user)
        );
    }
}
