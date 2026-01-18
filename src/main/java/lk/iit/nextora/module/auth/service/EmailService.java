package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.module.auth.entity.BaseUser;

/**
 * Service for sending authentication-related emails.
 */
public interface EmailService {

    /**
     * Send verification email with token.
     */
    void sendVerificationEmail(BaseUser user, String token);

    /**
     * Send password reset email with token.
     */
    void sendPasswordResetEmail(BaseUser user, String token);

    /**
     * Send account activated confirmation email.
     */
    void sendAccountActivatedEmail(BaseUser user);
}
