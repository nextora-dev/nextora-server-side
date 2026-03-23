package lk.iit.nextora.infrastructure.notification.push.repository;

import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import lk.iit.nextora.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FCM Token Repository
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * Find active tokens for a specific user.
     */
    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find a specific token.
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * Find all active tokens for a list of users.
     */
    @Query("SELECT f FROM FcmToken f WHERE f.userId IN :userIds AND f.isActive = true")
    List<FcmToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Find all active tokens for a specific role.
     */
    List<FcmToken> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Find all active tokens.
     */
    List<FcmToken> findByIsActiveTrue();

    /**
     * Check if a token exists for a user.
     */
    boolean existsByTokenAndUserId(String token, Long userId);

    /**
     * Deactivate all tokens for a user.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE FcmToken f SET f.isActive = false, f.updatedAt = :now WHERE f.userId = :userId")
    int deactivateAllTokensForUser(@Param("userId") Long userId, @Param("now") ZonedDateTime now);

    /**
     * Deactivate a specific token.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE FcmToken f SET f.isActive = false, f.updatedAt = :now WHERE f.token = :token")
    int deactivateToken(@Param("token") String token, @Param("now") ZonedDateTime now);

    /**
     * Update last used timestamp for tokens by token strings.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE FcmToken f SET f.lastUsedAt = :now WHERE f.token IN :tokens")
    int updateLastUsedAt(@Param("tokens") List<String> tokens, @Param("now") ZonedDateTime now);

    /**
     * Delete stale tokens older than cutoff date.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM FcmToken f WHERE f.isActive = false AND f.updatedAt < :cutoffDate")
    int deleteStaleTokens(@Param("cutoffDate") ZonedDateTime cutoffDate);

    /**
     * Delete a token by its value.
     */
    void deleteByToken(String token);

    /**
     * Count active tokens by role.
     */
    @Query("SELECT COUNT(f) FROM FcmToken f WHERE f.role = :role AND f.isActive = true")
    long countActiveTokensByRole(@Param("role") UserRole role);

    /**
     * Find all user IDs that have active tokens for a specific role.
     * Used for storing notification history for targeted role notifications.
     */
    @Query("SELECT DISTINCT f.userId FROM FcmToken f WHERE f.role = :role AND f.isActive = true")
    List<Long> findDistinctUserIdsByRoleAndIsActiveTrue(@Param("role") UserRole role);
}
