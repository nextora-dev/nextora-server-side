package lk.iit.nextora.module.lostandfound.repository;

import lk.iit.nextora.module.lostandfound.entity.ItemMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    // Find matches for a lost item
    List<ItemMatch> findByLostItemId(Long lostItemId);

    // Find matches for a found item
    List<ItemMatch> findByFoundItemId(Long foundItemId);

    // Find high-score matches
    @Query("SELECT m FROM ItemMatch m WHERE m.score >= :score")
    List<ItemMatch> findHighScoreMatches(@Param("score") double score);

    // Pagination support
    Page<ItemMatch> findByLostItemId(Long lostItemId, Pageable pageable);

    // Analytics - average match score
    @Query("SELECT AVG(m.score) FROM ItemMatch m")
    Double getAverageMatchScore();

    // Count matches above threshold
    @Query("SELECT COUNT(m) FROM ItemMatch m WHERE m.score >= :score")
    long countHighScoreMatches(@Param("score") double score);
}
