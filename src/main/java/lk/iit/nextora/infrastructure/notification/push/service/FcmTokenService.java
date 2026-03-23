package lk.iit.nextora.infrastructure.notification.push.service;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.dto.request.FcmTokenRequest;
import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import lk.iit.nextora.module.auth.entity.BaseUser;

/**
 * FCM Token Management Service Interface.
 *
 * Provides methods for managing Firebase Cloud Messaging tokens
 * for push notification targeting.
 */
public interface FcmTokenService {

    /**
     * Register or update FCM token for the authenticated user.
     * If token already exists for another user, ownership is transferred.
     *
     * @param request Token registration request containing the FCM token
     * @param user    The authenticated user
     * @return The registered or updated FcmToken entity
     */
    FcmToken registerToken(FcmTokenRequest request, BaseUser user);

    /**
     * Deactivate a specific FCM token.
     * Called when user logs out from a specific device.
     *
     * @param token The FCM token to deactivate
     */
    void deactivateToken(String token);

    /**
     * Deactivate all FCM tokens for a user.
     * Called when user logs out from all devices or account is deleted.
     *
     * @param userId The user's ID
     */
    void deactivateAllTokensForUser(Long userId);

    /**
     * Count active tokens for a specific role.
     * Used for analytics and capacity planning.
     *
     * @param role The user role to count tokens for
     * @return Number of active tokens for the role
     */
    long countActiveTokensByRole(UserRole role);

    /**
     * Cleanup old inactive tokens.
     * Should be called periodically by a scheduled job.
     *
     * @param daysOld Delete tokens inactive for more than this many days
     * @return Number of tokens deleted
     */
    int cleanupInactiveTokens(int daysOld);
}
