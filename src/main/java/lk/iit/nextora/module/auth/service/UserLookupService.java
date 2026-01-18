package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.entity.BaseUser;

import java.util.Optional;

/**
 * Service for user lookup operations across different user types.
 */
public interface UserLookupService {

    /**
     * Find a user by email across all user types.
     */
    Optional<BaseUser> findUserByEmail(String email);

    /**
     * Find a user by email and specific role.
     */
    Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role);

    /**
     * Check if an email already exists in the system.
     */
    boolean emailExists(String email);

    /**
     * Get a user by ID and role.
     */
    BaseUser getUserById(Long id, UserRole role);
}
