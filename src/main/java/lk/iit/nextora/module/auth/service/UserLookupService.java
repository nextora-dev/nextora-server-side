package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.entity.BaseUser;

import java.util.Optional;

/**
 * Service for user lookup operations across different user types.
 */
public interface UserLookupService {

    Optional<BaseUser> findUserByEmail(String email);

    Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role);

    boolean emailExists(String email);

    BaseUser getUserById(Long id, UserRole role);
}
