package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.LostItem;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for LostItem entities.
 *
 * ✅ FIX: Added @Repository annotation — Kuppi repositories all have it explicitly.
 * All other methods were already correct; documented below for clarity.
 */
@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    /**
     * Find all active lost items — used by the "get all" / default search path.
     * The active flag is false when an item has been found/resolved.
     */
    Page<LostItem> findByActiveTrue(Pageable pageable);

    /**
     * Find active lost items belonging to a specific category.
     * Called when the user filters the search by category name
     * (category name is resolved to an ID in the service layer first).
     */
    Page<LostItem> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Case-insensitive keyword search on the item title.
     * Used as the primary keyword search path in searchLostItems.
     */
    @Query("SELECT l FROM LostItem l " +
            "WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND l.active = true")
    Page<LostItem> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Case-insensitive keyword search on the item location.
     * Used as a fallback in searchLostItems when the title search returns nothing.
     */
    @Query("SELECT l FROM LostItem l " +
            "WHERE LOWER(l.location) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND l.active = true")
    Page<LostItem> searchByLocation(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Fetch a single LostItem with its category eagerly loaded in one query.
     * Avoids a secondary SELECT for the category when the mapper accesses category.name.
     */
    @Query("SELECT l FROM LostItem l " +
            "LEFT JOIN FETCH l.category " +
            "WHERE l.id = :id AND l.active = true")
    Optional<LostItem> findByIdWithCategory(@Param("id") Long id);

    /**
     * Count of all currently active (unresolved) lost items.
     * Used by statistics / analytics endpoints.
     */
    long countByActiveTrue();

    /**
     * Count active lost items in a specific category.
     * Used by category-level analytics.
     */
    long countByCategoryIdAndActiveTrue(Long categoryId);
}