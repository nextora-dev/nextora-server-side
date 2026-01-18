package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.EmailVerificationToken;
import lk.iit.nextora.module.auth.repository.EmailVerificationTokenRepository;
import lk.iit.nextora.module.auth.service.EmailService;
import lk.iit.nextora.module.auth.service.EmailVerificationService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserLookupService userLookupService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void sendVerificationEmail(BaseUser user) {
        // Delete any existing tokens for this user
        tokenRepository.findByUserId(user.getId()).ifPresent(tokenRepository::delete);

        // Create new token
        EmailVerificationToken token = new EmailVerificationToken(user);
        tokenRepository.save(token);

        // Send email
        emailService.sendVerificationEmail(user, token.getToken());

        log.info("Verification email sent for user ID: {}", user.getId());
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (!verificationToken.isValid()) {
            if (verificationToken.isExpired()) {
                throw new BadRequestException("Verification token has expired. Please request a new verification email.");
            }
            throw new BadRequestException("Verification token has already been used.");
        }

        // Mark token as used
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        // Activate user
        BaseUser user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        entityManager.merge(user);

        // Send confirmation email
        emailService.sendAccountActivatedEmail(user);

        log.info("Email verified successfully for user ID: {}", user.getId());
        return true;
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        BaseUser user = userLookupService.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "email", email));

        if (UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BadRequestException("Email is already verified");
        }

        if (!UserStatus.PENDING_VERIFICATION.equals(user.getStatus())) {
            throw new BadRequestException("Cannot resend verification email for this account status");
        }

        sendVerificationEmail(user);
        log.info("Verification email resent for user: {}", email);
    }

    @Override
    public boolean isEmailVerified(Long userId) {
        BaseUser user = entityManager.find(BaseUser.class, userId);
        if (user == null) {
            return false;
        }
        return UserStatus.ACTIVE.equals(user.getStatus());
    }
}
