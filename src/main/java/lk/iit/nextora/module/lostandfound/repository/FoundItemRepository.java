package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.FoundItem;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for FoundItem entities.
 *
 * ✅ FIX: Added @Repository annotation — Kuppi repositories all have it explicitly.
 * All other methods were already correct; documented below for clarity.
 */
@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    /**
     * Find all active found items — the default listing used when no filters are applied.
     */
    Page<FoundItem> findByActiveTrue(Pageable pageable);

    /**
     * Find active found items in a specific category.
     * The category name is resolved to an ID in the service before calling this.
     */
    Page<FoundItem> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Case-insensitive title search across active found items.
     */
    @Query("SELECT f FROM FoundItem f " +
            "WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND f.active = true")
    Page<FoundItem> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Fetch a single FoundItem with its category pre-loaded to avoid a secondary SELECT.
     */
    @Query("SELECT f FROM FoundItem f " +
            "LEFT JOIN FETCH f.category " +
            "WHERE f.id = :id AND f.active = true")
    Optional<FoundItem> findByIdWithCategory(@Param("id") Long id);

    /**
     * Count of all currently active (unclaimed) found items.
     */
    long countByActiveTrue();
}