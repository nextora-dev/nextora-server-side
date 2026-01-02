//package lk.iit.nextora.module.auth.service;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lk.iit.nextora.common.enums.UserRole;
//import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
//import lk.iit.nextora.module.auth.entity.*;
//import lk.iit.nextora.module.auth.entity.BaseUser;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
///**
// * Unified authentication service for all user types
// */
//@Slf4j
//@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
//public class AuthenticationService {
//
//    @PersistenceContext
//    private final EntityManager entityManager;
//
//    /**
//     * Find user by email across all user tables
//     */
//    public Optional<BaseUser> findUserByEmail(String email) {
//        log.debug("Finding user by email: {}", email);
//
//        // Query all user tables
//        for (UserRole role : UserRole.values()) {
//            Optional<BaseUser> user = findUserByEmailAndRole(email, role);
//            if (user.isPresent()) {
//                return user;
//            }
//        }
//
//        return Optional.empty();
//    }
//
//    /**
//     * Find user by email and specific role
//     */
//    public Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role) {
//        try {
//            String query = String.format(
//                    "SELECT u FROM %s u WHERE u.email = :email",
//                    getEntityName(role)
//            );
//
//            BaseUser user = (BaseUser) entityManager
//                    .createQuery(query)
//                    .setParameter("email", email)
//                    .getSingleResult();
//
//            return Optional.of(user);
//
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//    }
//
//    /**
//     * Check if email exists in any user table
//     */
//    public boolean emailExists(String email) {
//        return findUserByEmail(email).isPresent();
//    }
//
//    /**
//     * Get user by ID and role
//     */
//    public BaseUser getUserById(Long id, UserRole role) {
//        Class<? extends BaseUser> entityClass = getEntityClass(role);
//        BaseUser user = entityManager.find(entityClass, id);
//
//        if (user == null) {
//            throw new ResourceNotFoundException("User not found with id: ", "id", id);
//        }
//
//        return user;
//    }
//
//    private String getEntityName(UserRole role) {
//        return switch (role) {
//            case ROLE_STUDENT -> "Student";
//            case ROLE_LECTURER -> "Lecturer";
//            case ROLE_ACADEMIC_STAFF -> "AcademicStaff";
//            case ROLE_NON_ACADEMIC_STAFF -> "NonAcademicStaff";
//            case ROLE_ADMIN -> "Admin";
//            case ROLE_SUPER_ADMIN -> "SuperAdmin";
//        };
//    }
//
//    private Class<? extends BaseUser> getEntityClass(UserRole role) {
//        return switch (role) {
//            case ROLE_STUDENT -> Student.class;
//            case ROLE_LECTURER -> Lecturer.class;
//            case ROLE_ACADEMIC_STAFF -> AcademicStaff.class;
//            case ROLE_NON_ACADEMIC_STAFF -> NonAcademicStaff.class;
//            case ROLE_ADMIN -> Admin.class;
//            case ROLE_SUPER_ADMIN -> SuperAdmin.class;
//        };
//    }
//}

package lk.iit.nextora.module.auth.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.auth.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Unified authentication service for all user types
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find user by email across all user tables
     */
    public Optional<BaseUser> findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);

        // Query all user tables
        for (UserRole role : UserRole.values()) {
            Optional<BaseUser> user = findUserByEmailAndRole(email, role);
            if (user.isPresent()) {
                return user;
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by email and specific role
     */
    public Optional<BaseUser> findUserByEmailAndRole(String email, UserRole role) {
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
            log.debug("No user found with email {} for role {}", email, role);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding user by email and role: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Check if email exists in any user table
     */
    public boolean emailExists(String email) {
        return findUserByEmail(email).isPresent();
    }

    /**
     * Get user by ID and role
     */
    public BaseUser getUserById(Long id, UserRole role) {
        Class<? extends BaseUser> entityClass = getEntityClass(role);
        BaseUser user = entityManager.find(entityClass, id);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id, "id", id);
        }

        return user;
    }

    /**
     * Get entity name for JPQL queries
     */
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

    /**
     * Get entity class for role
     */
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