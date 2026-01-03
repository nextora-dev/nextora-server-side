package lk.iit.nextora.module.auth.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.entity.BaseUser;

import java.util.Optional;

public interface AuthenticationService {

    Optional<BaseUser> findUserByEmail(String email);

    Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role);

    boolean emailExists(String email);

    BaseUser getUserById(Long id, UserRole role);
}