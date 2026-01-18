package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;

/**
 * Service for tracking and managing login attempts.
 */
public interface LoginAttemptService {

    /**
     * Record a failed login attempt.
     * @return true if account was suspended due to too many attempts
     */
    boolean recordFailedAttempt(Long userId, UserRole role);

    /**
     * Reset failed login attempts after successful login.
     */
    void resetFailedAttempts(Long userId);
}
