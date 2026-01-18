package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;

/**
 * Service to manage login attempt tracking.
 * Uses separate transactions to ensure failed attempts are persisted
 * even when the main login transaction is rolled back.
 */
public interface LoginAttemptService {

    /**
     * Record a failed login attempt for a user.
     * Increments the failed attempt counter and suspends the account after 5 attempts per day.
     * Admin and SuperAdmin users are exempt from account lockout.
     *
     * @param userId the user's ID
     * @param role   the user's role
     * @return true if the account was suspended as a result of this attempt
     */
    boolean recordFailedAttempt(Long userId, UserRole role);

    /**
     * Reset failed login attempts for a user.
     * Called on successful login or when admin unlocks an account.
     *
     * @param userId the user's ID
     */
    void resetFailedAttempts(Long userId);
}
