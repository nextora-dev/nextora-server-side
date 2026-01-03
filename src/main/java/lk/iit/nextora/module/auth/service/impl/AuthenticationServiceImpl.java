package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<BaseUser> findUserByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return Optional.empty();
        }

        log.debug("Finding user by email: {}", StringUtils.maskEmail(email));

        // Query all user tables
        for (UserRole role : UserRole.values()) {
            Optional<BaseUser> user = findUserByEmailAndRole(email, role);
            if (user.isPresent()) {
                return user;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role) {
        if (StringUtils.isBlank(email) || role == null) {
            return Optional.empty();
        }

        try {
            String query = String.format(
                    "SELECT u FROM %s u WHERE u.email = :email",
                    getEntityName(role)
            );

            BaseUser user = (BaseUser) entityManager
                    .createQuery(query)
                    .setParameter("email", email)
                    .getSingleResult();

            return Optional.of(user);

        } catch (NoResultException e) {
            log.debug("No user found with email {} for role {}", StringUtils.maskEmail(email), role);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding user by email and role: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean emailExists(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return findUserByEmail(email).isPresent();
    }

    @Override
    public BaseUser getUserById(Long id, UserRole role) {
        ValidationUtils.requireNonNull(id, "User ID");
        ValidationUtils.requireNonNull(role, "Role");

        Class<? extends BaseUser> entityClass = getEntityClass(role);
        BaseUser user = entityManager.find(entityClass, id);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id, "id", id);
        }

        return user;
    }

    private String getEntityName(UserRole role) {
        return switch (role) {
            case ROLE_STUDENT -> "Student";
            case ROLE_LECTURER -> "Lecturer";
            case ROLE_ACADEMIC_STAFF -> "AcademicStaff";
            case ROLE_NON_ACADEMIC_STAFF -> "NonAcademicStaff";
            case ROLE_ADMIN -> "Admin";
            case ROLE_SUPER_ADMIN -> "SuperAdmin";
        };
    }

    private Class<? extends BaseUser> getEntityClass(UserRole role) {
        return switch (role) {
            case ROLE_STUDENT -> Student.class;
            case ROLE_LECTURER -> Lecturer.class;
            case ROLE_ACADEMIC_STAFF -> AcademicStaff.class;
            case ROLE_NON_ACADEMIC_STAFF -> NonAcademicStaff.class;
            case ROLE_ADMIN -> Admin.class;
            case ROLE_SUPER_ADMIN -> SuperAdmin.class;
        };
    }
}

