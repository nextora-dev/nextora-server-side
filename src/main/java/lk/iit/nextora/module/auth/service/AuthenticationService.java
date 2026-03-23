package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.module.auth.dto.request.ForgotPasswordRequest;
import lk.iit.nextora.module.auth.dto.request.LoginRequest;
import lk.iit.nextora.module.auth.dto.request.ResetPasswordRequest;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.ForgotPasswordResponse;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request);

    ForgotPasswordResponse initiatePasswordReset(ForgotPasswordRequest request);


    void resetPassword(ResetPasswordRequest request);

    void cleanupExpiredTokens();
}
