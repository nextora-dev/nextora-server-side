package lk.iit.nextora.config.security;

import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Provides security-related utilities for the currently logged-in user
 */
@Service
public class SecurityService {

    /**
     * Get the current authenticated user
     */
    public Optional<BaseUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof BaseUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Get the current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser()
                .map(BaseUser::getId)
                .orElseThrow(() -> new RuntimeException("Unauthenticated user"));
    }

    /**
     * Get the current user's email
     */
    public String getCurrentUserEmail() {
        return getCurrentUser()
                .map(BaseUser::getEmail)
                .orElseThrow(() -> new RuntimeException("Unauthenticated user"));
    }

    /**
     * Get the current user's role
     */
    public UserRole getCurrentUserRole() {
        return getCurrentUser()
                .map(BaseUser::getRole)
                .orElseThrow(() -> new RuntimeException("Unauthenticated user"));
    }

    /**
     * Get the current student's sub-role types (only for students)
     */
    public Optional<java.util.Set<StudentRoleType>> getCurrentStudentRoleTypes() {
        return getCurrentUser()
                .filter(user -> user instanceof Student)
                .map(user -> ((Student) user).getStudentRoleTypes());
    }

    /**
     * Check if the current student has a specific role type
     */
    public boolean hasStudentRoleType(StudentRoleType roleType) {
        return getCurrentUser()
                .filter(user -> user instanceof Student)
                .map(user -> ((Student) user).hasRoleType(roleType))
                .orElse(false);
    }

    /**
     * @deprecated Use getCurrentStudentRoleTypes() instead
     */
    @Deprecated
    public Optional<StudentRoleType> getCurrentStudentRoleType() {
        return getCurrentUser()
                .filter(user -> user instanceof Student)
                .map(user -> ((Student) user).getPrimaryRoleType());
    }

    /**
     * Check if the current user has a specific role
     */
    public boolean hasRole(UserRole role) {
        return getCurrentUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }

    /**
     * Check if the current user has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return getCurrentUser()
                .map(user -> user.hasPermission(permission))
                .orElse(false);
    }

    /**
     * Check if the current user has a specific permission by string
     */
    public boolean hasPermission(String permissionString) {
        return getCurrentUser()
                .map(user -> user.hasPermission(permissionString))
                .orElse(false);
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public boolean hasAnyRole(UserRole... roles) {
        return getCurrentUser()
                .map(user -> {
                    for (UserRole role : roles) {
                        if (user.getRole() == role) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Check if the current user has any of the specified permissions
     */
    public boolean hasAnyPermission(Permission... permissions) {
        return getCurrentUser()
                .map(user -> {
                    for (Permission permission : permissions) {
                        if (user.hasPermission(permission)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Check if the current user is a Super Admin
     */
    public boolean isSuperAdmin() {
        return hasRole(UserRole.ROLE_SUPER_ADMIN);
    }

    /**
     * Check if the current user is an Admin (includes Super Admin)
     */
    public boolean isAdmin() {
        return hasAnyRole(UserRole.ROLE_ADMIN, UserRole.ROLE_SUPER_ADMIN);
    }

    /**
     * Check if the current user is a Student
     */
    public boolean isStudent() {
        return hasRole(UserRole.ROLE_STUDENT);
    }

    /**
     * Check if the current user is an Academic Staff (includes former Lecturer role)
     */
    public boolean isAcademicStaff() {
        return hasRole(UserRole.ROLE_ACADEMIC_STAFF);
    }

    public boolean isNonAcademicstaff() {
        return hasRole(UserRole.ROLE_NON_ACADEMIC_STAFF);
    }

    /**
     * Check if the current user has Kuppi Student capabilities
     * Checks for both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
     */
    public boolean isKuppiStudent() {
        return hasStudentRoleType(StudentRoleType.KUPPI_STUDENT) || hasStudentRoleType(StudentRoleType.SENIOR_KUPPI);
    }

    /**
     * @deprecated Use isKuppiStudent() instead
     */
    @Deprecated
    public boolean isSeniorKuppi() {
        return isKuppiStudent();
    }

    /**
     * Check if the current user is a Batch Representative
     */
    public boolean isBatchRep() {
        return hasStudentRoleType(StudentRoleType.BATCH_REP);
    }

    /**
     * Check if the current user is a Club Member
     */
    public boolean isClubMember() {
        return hasStudentRoleType(StudentRoleType.CLUB_MEMBER);
    }

    /**
     * Get all authorities of the current user
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getCurrentUser()
                .map(BaseUser::getAuthorities)
                .orElse(null);
    }

    /**
     * Check if the current user owns a resource
     */
    public boolean isOwner(Long resourceOwnerId) {
        return getCurrentUser()
                .map(user -> user.getId().equals(resourceOwnerId))
                .orElse(false);
    }

    /**
     * Check if the current user can access a resource (owner or admin)
     */
    public boolean canAccess(Long resourceOwnerId) {
        return isOwner(resourceOwnerId) || isAdmin();
    }
}
