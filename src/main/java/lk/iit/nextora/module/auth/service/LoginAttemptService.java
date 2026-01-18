package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;

public interface LoginAttemptService {

    boolean recordFailedAttempt(Long userId, UserRole role);

    void resetFailedAttempts(Long userId);
}
