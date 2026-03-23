package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.Claim;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Claim entities.
 *
 * ✅ FIX: Added @Repository annotation — Kuppi repositories all have it explicitly.
 * ✅ FIX: Added findByClaimantId / findByClaimantIdAndStatus — ClaimController now has
 *         a GET /my endpoint that needs these queries.
 * ✅ FIX: Changed List-based methods to Page-based where pagination makes sense.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    /**
     * Find all claims submitted by a specific student (pageable).
     * Used by GET /my — the "my claims" endpoint on ClaimController.
     *
     * ✅ FIX: was missing — ClaimController.getMyClaims() needs this to fetch
     *         only the current student's claims.
     */
    Page<Claim> findByClaimantId(Long claimantId, Pageable pageable);

    /**
     * Find all claims submitted by a specific student filtered by status.
     * Useful for "my pending claims" / "my approved claims" views.
     */
    Page<Claim> findByClaimantIdAndStatus(Long claimantId, String status, Pageable pageable);

    /**
     * Find all claims for a given lost item.
     * Used by admin to see all claimants competing for the same lost item.
     */
    List<Claim> findByLostItemId(Long lostItemId);

    /**
     * Find all claims targeting a specific found item.
     * An admin may need to see multiple students claiming the same found item.
     */
    List<Claim> findByFoundItemId(Long foundItemId);

    /**
     * Find all claims filtered by status — pageable for the admin review queue.
     * Used by GET /status/{status} on ClaimController.
     */
    Page<Claim> findByStatus(String status, Pageable pageable);

    /**
     * Count claims by status — used for admin statistics / dashboard.
     * e.g. "how many claims are currently PENDING review?"
     */
    long countByStatus(String status);

    /**
     * Fetch a claim with its lostItem and foundItem eagerly loaded.
     * Avoids N+1 queries when the service needs to access nested item data.
     */
    @Query("SELECT c FROM Claim c " +
            "LEFT JOIN FETCH c.lostItem " +
            "LEFT JOIN FETCH c.foundItem " +
            "WHERE c.id = :id")
    java.util.Optional<Claim> findByIdWithDetails(@Param("id") Long id);
}