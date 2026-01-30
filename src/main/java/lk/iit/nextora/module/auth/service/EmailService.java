package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.module.auth.entity.BaseUser;

public interface EmailService {

    void sendVerificationEmail(BaseUser user, String token);

    void sendPasswordResetEmail(BaseUser user, String token);

    void sendAccountActivatedEmail(BaseUser user);

    void sendAccountCredentialsEmail(String toEmail, String firstName, String loginEmail, String temporaryPassword);
}
