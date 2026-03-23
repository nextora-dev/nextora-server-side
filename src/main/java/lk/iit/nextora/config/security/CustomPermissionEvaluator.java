package lk.iit.nextora.config.security;

import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for fine-grained permission checks.
 * Enables usage of hasPermission() in @PreAuthorize annotations.
 *
 * Usage examples:
 * - @PreAuthorize("hasPermission(null, 'KUPPI:CREATE')")
 * - @PreAuthorize("hasPermission(#kuppiSession, 'KUPPI:UPDATE')")
 * - @PreAuthorize("hasPermission(#id, 'KuppiSession', 'KUPPI:DELETE')")
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final SecurityService securityService;

    /**
     * Evaluates permission when target object is provided
     *
     * @param authentication the current authentication
     * @param targetDomainObject the target object (can be null for general permission check)
     * @param permission the permission string (e.g., "KUPPI:CREATE")
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();

        // Check if the user has the permission
        boolean hasPermission = checkPermission(authentication, permissionString);

        // If target object provided, also check ownership for non-admin users
        if (hasPermission && targetDomainObject != null) {
            return checkOwnershipOrAdmin(authentication, targetDomainObject);
        }

        return hasPermission;
    }

    /**
     * Evaluates permission when target ID and type are provided
     *
     * @param authentication the current authentication
     * @param targetId the ID of the target object
     * @param targetType the type of the target object (e.g., "KuppiSession")
     * @param permission the permission string
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                  String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        String permissionString = permission.toString();
        return checkPermission(authentication, permissionString);
    }

    private boolean checkPermission(Authentication authentication, String permissionString) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof BaseUser user) {
            // Check if user has the permission
            if (user.hasPermission(permissionString)) {
                return true;
            }

            // Try to match by Permission enum
            try {
                Permission permission = findPermissionByString(permissionString);
                if (permission != null) {
                    return user.hasPermission(permission);
                }
            } catch (Exception e) {
                log.debug("Permission {} not found in enum", permissionString);
            }
        }

        // Fallback: check authorities directly
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(permissionString));
    }

    private Permission findPermissionByString(String permissionString) {
        for (Permission p : Permission.values()) {
            if (p.getPermission().equals(permissionString) || p.name().equals(permissionString)) {
                return p;
            }
        }
        return null;
    }

    private boolean checkOwnershipOrAdmin(Authentication authentication, Object targetDomainObject) {
        // Admins and Super Admins always have access
        if (securityService.isAdmin()) {
            return true;
        }

        // Check if the target has an owner ID field
        try {
            if (targetDomainObject instanceof BaseUser targetUser) {
                return securityService.isOwner(targetUser.getId());
            }

            // Try to get ownerId or hostId or createdBy field via reflection
            java.lang.reflect.Method getOwnerMethod = findOwnerMethod(targetDomainObject);
            if (getOwnerMethod != null) {
                Object ownerId = getOwnerMethod.invoke(targetDomainObject);
                if (ownerId instanceof Long) {
                    return securityService.isOwner((Long) ownerId);
                }
            }
        } catch (Exception e) {
            log.debug("Could not check ownership for object: {}", targetDomainObject.getClass().getSimpleName());
        }

        return true; // Default to true if no ownership check can be performed
    }

    private java.lang.reflect.Method findOwnerMethod(Object target) {
        Class<?> clazz = target.getClass();
        String[] methodNames = {"getHostId", "getOwnerId", "getCreatedBy", "getUserId"};

        for (String methodName : methodNames) {
            try {
                return clazz.getMethod(methodName);
            } catch (NoSuchMethodException ignored) {
                // Try next method name
            }
        }
        return null;
    }
}
