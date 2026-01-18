package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.module.auth.entity.BaseUser;

public interface EmailVerificationService {

    void sendVerificationEmail(BaseUser user);

    boolean verifyEmail(String token);

    void resendVerificationEmail(String email);

    boolean isEmailVerified(Long userId);
}
