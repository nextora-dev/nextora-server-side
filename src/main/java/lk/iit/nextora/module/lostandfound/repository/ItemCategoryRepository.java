package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for ItemCategory entities.
 *
 * ✅ FIX: The original repository was COMPLETELY EMPTY — it had zero query methods.
 *         Both LostItemServiceImpl and FoundItemServiceImpl were calling
 *         categoryRepository.findAll() and then streaming + filtering in Java memory
 *         for EVERY create/search request. This is a serious performance bug that gets
 *         worse as more categories are added.
 *
 *         The fix: add findByNameIgnoreCase so the service can resolve a category name
 *         with a single targeted DB query instead of loading all categories into memory.
 */
@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {

    /**
     * Find a category by its name, case-insensitive.
     *
     * ✅ FIX: This is the missing method. Services were doing:
     *     categoryRepository.findAll().stream()
     *         .filter(c -> c.getName().equalsIgnoreCase(name))
     *         .findFirst()
     *
     *   Replace all of those with a single call to:
     *     categoryRepository.findByNameIgnoreCase(name)
     *
     * Spring Data generates: SELECT * FROM item_categories WHERE LOWER(name) = LOWER(:name)
     */
    Optional<ItemCategory> findByNameIgnoreCase(String name);

    /**
     * Check whether a category with the given name already exists.
     * Used by the admin category-creation endpoint to prevent duplicates.
     */
    boolean existsByNameIgnoreCase(String name);
}