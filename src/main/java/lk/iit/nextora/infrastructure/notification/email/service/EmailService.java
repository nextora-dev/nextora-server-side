package lk.iit.nextora.infrastructure.notification.email.service;

import lk.iit.nextora.module.auth.entity.BaseUser;

/**
 * Email Service Interface
 *
 * Handles all email sending operations in the application.
 */
public interface EmailService {

    /**
     * Send password reset email to user
     */
    void sendPasswordResetEmail(BaseUser user, String token);

    /**
     * Send account credentials email to newly created user
     */
    void sendAccountCredentialsEmail(String toEmail, String firstName, String loginEmail, String temporaryPassword);
}

