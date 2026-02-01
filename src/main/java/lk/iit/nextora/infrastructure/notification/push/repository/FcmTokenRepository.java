package lk.iit.nextora.infrastructure.notification.push.repository;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.infrastructure.notification.push.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FCM Token operations.
 *
 * Query Design:
 * - All queries filter by isActive=true by default for active tokens
 * - Bulk operations use @Modifying for efficiency
 * - Named parameters for readability and safety
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * Find token by its value.
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * Find all active tokens for a specific user.
     */
    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all active tokens for users with a specific role.
     */
    List<FcmToken> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Find all active tokens for multiple users.
     */
    @Query("SELECT f FROM FcmToken f WHERE f.userId IN :userIds AND f.isActive = true")
    List<FcmToken> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Check if a token exists for a user.
     */
    boolean existsByTokenAndUserId(String token, Long userId);

    /**
     * Deactivate all tokens for a user (e.g., on logout from all devices).
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false, f.updatedAt = :now WHERE f.userId = :userId")
    int deactivateAllTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Deactivate a specific token.
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false, f.updatedAt = :now WHERE f.token = :token")
    int deactivateToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Delete tokens that haven't been used in a specified period (cleanup stale tokens).
     */
    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.lastUsedAt < :cutoffDate OR (f.lastUsedAt IS NULL AND f.createdAt < :cutoffDate)")
    int deleteStaleTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update last used timestamp for tokens.
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.lastUsedAt = :now, f.updatedAt = :now WHERE f.token IN :tokens")
    int updateLastUsedAt(@Param("tokens") List<String> tokens, @Param("now") LocalDateTime now);

    /**
     * Delete a specific token by its value (hard delete for invalid tokens).
     */
    void deleteByToken(String token);

    /**
     * Count active tokens by role (for analytics).
     */
    long countByRoleAndIsActiveTrue(UserRole role);

    /**
     * Find all active tokens (for broadcast notifications).
     */
    List<FcmToken> findByIsActiveTrue();
}
