package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.module.auth.entity.BaseUser;

/**
 * Service for email verification operations.
 */
public interface EmailVerificationService {

    /**
     * Send verification email to user (creates token and sends email).
     */
    void sendVerificationEmail(BaseUser user);

    /**
     * Verify email using token.
     * @return true if verification successful
     */
    boolean verifyEmail(String token);

    /**
     * Resend verification email to user.
     */
    void resendVerificationEmail(String email);

    /**
     * Check if user's email is verified.
     */
    boolean isEmailVerified(Long userId);
}
