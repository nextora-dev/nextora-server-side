package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.ItemMatch;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ItemMatch entities.
 *
 * ✅ FIX: Added @Repository annotation — was missing.
 */
@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    /**
     * Find all matches computed for a specific lost item, ordered by score descending.
     * The highest-scoring match is the most likely candidate for the owner to review.
     */
    List<ItemMatch> findByLostItemIdOrderByScoreDesc(Long lostItemId);

    /**
     * Find all matches that reference a specific found item.
     * Useful when a found item is claimed — check which lost items were matched to it.
     */
    List<ItemMatch> findByFoundItemId(Long foundItemId);

    /**
     * Find all matches with a score at or above the given threshold.
     * Used when auto-notifying users of high-confidence matches.
     */
    @Query("SELECT m FROM ItemMatch m WHERE m.score >= :score ORDER BY m.score DESC")
    List<ItemMatch> findHighScoreMatches(@Param("score") double score);

    /**
     * Find matches for a lost item — pageable version for API responses.
     * Ordered by score descending so the best match appears first.
     */
    @Query("SELECT m FROM ItemMatch m WHERE m.lostItem.id = :lostItemId ORDER BY m.score DESC")
    Page<ItemMatch> findByLostItemId(@Param("lostItemId") Long lostItemId, Pageable pageable);

    /**
     * Compute the average match score across all stored matches.
     * Returns null if there are no matches yet.
     */
    @Query("SELECT AVG(m.score) FROM ItemMatch m")
    Double getAverageMatchScore();

    /**
     * Count how many matches exceed the given score threshold.
     * Used for analytics — e.g. "how many high-confidence matches exist today?"
     */
    @Query("SELECT COUNT(m) FROM ItemMatch m WHERE m.score >= :score")
    long countHighScoreMatches(@Param("score") double score);

    /**
     * Check if a match between a specific lostItem and foundItem already exists.
     * Prevents the matching service from creating duplicate match records.
     */
    boolean existsByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);
}