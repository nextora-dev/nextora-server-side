package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find a valid token by its value
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find valid (unused and not expired) token for a user
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false AND t.expiryDate > :now")
    Optional<PasswordResetToken> findValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for a user (when password is changed)
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true, t.usedAt = :now WHERE t.user.id = :userId AND t.used = false")
    void invalidateAllTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Count valid tokens for a user (to prevent spam)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false AND t.expiryDate > :now")
    long countValidTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
