package lk.iit.nextora.common.util;

import lk.iit.nextora.module.auth.entity.BaseUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Utility class for security-related operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * Optional<String> email = SecurityUtils.getCurrentUserEmail();
 * boolean isAdmin = SecurityUtils.hasRole("ROLE_ADMIN");
 * Long userId = SecurityUtils.getCurrentUserId();
 * </pre>
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // ==================== Current User ====================

    /**
     * Get current authentication object
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Get current authenticated user's email/username
     */
    public static Optional<String> getCurrentUserEmail() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> !"anonymousUser".equals(name));
    }

    /**
     * Get current authenticated user's ID (if principal is BaseUser)
     */
    public static Optional<Long> getCurrentUserId() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof BaseUser)
                .map(p -> ((BaseUser) p).getId());
    }

    /**
     * Get current user as BaseUser entity
     */
    public static Optional<BaseUser> getCurrentUser() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof BaseUser)
                .map(p -> (BaseUser) p);
    }

    /**
     * Get current user as UserDetails
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetails)
                .map(p -> (UserDetails) p);
    }

    // ==================== Authentication Status ====================

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(p -> !"anonymousUser".equals(p))
                .isPresent();
    }

    /**
     * Check if user is anonymous
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    // ==================== Role Checks ====================

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(role)))
                .orElse(false);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> {
                            for (String role : roles) {
                                if (a.getAuthority().equals(role)) return true;
                            }
                            return false;
                        }))
                .orElse(false);
    }

    /**
     * Check if current user has all specified roles
     */
    public static boolean hasAllRoles(String... roles) {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    for (String role : roles) {
                        boolean hasRole = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(role));
                        if (!hasRole) return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasAnyRole("ROLE_ADMIN", "ROLE_SUPER_ADMIN");
    }

    /**
     * Check if current user is super admin
     */
    public static boolean isSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }

    /**
     * Check if current user is student
     */
    public static boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }

    /**
     * Check if current user is academic staff (includes former lecturer role)
     */
    public static boolean isAcademicStaff() {
        return hasRole("ROLE_ACADEMIC_STAFF");
    }

    // ==================== Student Sub-Role Checks ====================

    /**
     * Get current student's sub-role types
     */
    public static Optional<java.util.Set<lk.iit.nextora.common.enums.StudentRoleType>> getCurrentStudentRoleTypes() {
        return getCurrentUser()
                .filter(user -> user instanceof lk.iit.nextora.module.auth.entity.Student)
                .map(user -> ((lk.iit.nextora.module.auth.entity.Student) user).getStudentRoleTypes());
    }

    /**
     * Check if current student has specific sub-role
     */
    public static boolean hasStudentRoleType(lk.iit.nextora.common.enums.StudentRoleType roleType) {
        return getCurrentUser()
                .filter(user -> user instanceof lk.iit.nextora.module.auth.entity.Student)
                .map(user -> ((lk.iit.nextora.module.auth.entity.Student) user).hasRoleType(roleType))
                .orElse(false);
    }

    /**
     * @deprecated Use getCurrentStudentRoleTypes() instead
     */
    @Deprecated
    public static Optional<lk.iit.nextora.common.enums.StudentRoleType> getCurrentStudentRoleType() {
        return getCurrentUser()
                .filter(user -> user instanceof lk.iit.nextora.module.auth.entity.Student)
                .map(user -> ((lk.iit.nextora.module.auth.entity.Student) user).getPrimaryRoleType());
    }

    /**
     * Check if current user is a Club Member student
     */
    public static boolean isClubMember() {
        return hasStudentRoleType(lk.iit.nextora.common.enums.StudentRoleType.CLUB_MEMBER);
    }

    /**
     * Check if current user has Kuppi Student capabilities
     * Checks for both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
     */
    public static boolean isKuppiStudent() {
        return hasStudentRoleType(lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT)
                || hasStudentRoleType(lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI);
    }

    /**
     * @deprecated Use isKuppiStudent() instead
     */
    @Deprecated
    public static boolean isSeniorKuppi() {
        return isKuppiStudent();
    }

    /**
     * Check if current user is a Batch Representative
     */
    public static boolean isBatchRep() {
        return hasStudentRoleType(lk.iit.nextora.common.enums.StudentRoleType.BATCH_REP);
    }

    /**
     * Check if current student has any special sub-role (not only NORMAL)
     */
    public static boolean hasSpecialStudentRole() {
        return getCurrentStudentRoleTypes()
                .map(types -> types.stream().anyMatch(type -> type != lk.iit.nextora.common.enums.StudentRoleType.NORMAL))
                .orElse(false);
    }

    // ==================== Owner Checks ====================

    /**
     * Check if current user is owner of a resource
     */
    public static boolean isOwner(Long resourceOwnerId) {
        return getCurrentUserId()
                .map(userId -> userId.equals(resourceOwnerId))
                .orElse(false);
    }

    /**
     * Check if current user is owner or admin
     */
    public static boolean isOwnerOrAdmin(Long resourceOwnerId) {
        return isOwner(resourceOwnerId) || isAdmin();
    }

    // ==================== Context Management ====================

    /**
     * Clear security context (for logout, etc.)
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}

