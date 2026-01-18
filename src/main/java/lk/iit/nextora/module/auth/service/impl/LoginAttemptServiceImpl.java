package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of LoginAttemptService.
 * Uses REQUIRES_NEW propagation to ensure failed attempts are saved in a separate transaction,
 * so they persist even when the main login transaction is rolled back.
 */
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recordFailedAttempt(Long userId, UserRole role) {
        // Admin and Super Admin are exempt from account lockout
        if (UserRole.ROLE_ADMIN.equals(role) || UserRole.ROLE_SUPER_ADMIN.equals(role)) {
            log.info("Admin/SuperAdmin failed login - not tracking attempts for user ID: {}", userId);
            return false;
        }

        BaseUser user = entityManager.find(BaseUser.class, userId);
        if (user == null) {
            log.warn("User not found for failed attempt tracking: {}", userId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastFailedAt = user.getLastFailedLoginAt();

        // Check if it's a new day - reset attempts if last failure was on a different day
        boolean isNewDay = lastFailedAt == null ||
                           !lastFailedAt.toLocalDate().equals(now.toLocalDate());

        int currentAttempts;
        if (isNewDay) {
            // Reset counter for new day
            currentAttempts = 0;
            log.info("New day - resetting failed login attempts for user: {}",
                    StringUtils.maskEmail(user.getEmail()));
        } else {
            currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        }

        int newAttempts = currentAttempts + 1;

        user.setFailedLoginAttempts(newAttempts);
        user.setLastFailedLoginAt(now);

        boolean suspended = false;
        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            // Suspend the account
            user.setStatus(UserStatus.SUSPENDED);
            suspended = true;
            log.warn("Account SUSPENDED due to {} failed login attempts today: {}",
                    newAttempts, StringUtils.maskEmail(user.getEmail()));
        } else {
            int remainingAttempts = MAX_FAILED_ATTEMPTS - newAttempts;
            log.warn("Failed login attempt {} of {} for today. {} attempts remaining for user: {}",
                    newAttempts, MAX_FAILED_ATTEMPTS, remainingAttempts, StringUtils.maskEmail(user.getEmail()));
        }

        entityManager.merge(user);
        entityManager.flush();

        return suspended;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        BaseUser user = entityManager.find(BaseUser.class, userId);
        if (user == null) {
            log.warn("User not found for resetting failed attempts: {}", userId);
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginAt(null);

        // If user was suspended due to failed attempts, reactivate them
        if (UserStatus.SUSPENDED.equals(user.getStatus())) {
            user.setStatus(UserStatus.ACTIVE);
            log.info("Account reactivated for user: {}", StringUtils.maskEmail(user.getEmail()));
        }

        entityManager.merge(user);
        entityManager.flush();

        log.info("Reset failed login attempts for user: {}", StringUtils.maskEmail(user.getEmail()));
    }
}
