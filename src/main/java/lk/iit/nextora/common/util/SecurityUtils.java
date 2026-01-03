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
     * Check if current user is lecturer
     */
    public static boolean isLecturer() {
        return hasRole("ROLE_LECTURER");
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

