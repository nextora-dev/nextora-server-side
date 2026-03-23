package lk.iit.nextora.module.lostandfound.repository;

// ── Lost & Found entity ─────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.ItemImage;

// ── Spring Data JPA ─────────────────────────────────────────────────────────
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ItemImage entities.
 *
 * ✅ FIX: Added @Repository annotation.
 */
@Repository
public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    /**
     * Get all images attached to a specific lost item, ordered by displayOrder.
     * The first result (displayOrder = 0) is the thumbnail image.
     */
    List<ItemImage> findByLostItemIdOrderByDisplayOrderAsc(Long lostItemId);

    /**
     * Get all images attached to a specific found item, ordered by displayOrder.
     */
    List<ItemImage> findByFoundItemIdOrderByDisplayOrderAsc(Long foundItemId);

    /**
     * Count how many images are attached to a lost item.
     * Used to enforce a maximum image count per item before allowing uploads.
     */
    long countByLostItemId(Long lostItemId);

    /**
     * Count how many images are attached to a found item.
     */
    long countByFoundItemId(Long foundItemId);

    /**
     * Delete all images for a lost item.
     * Called when a lost item is permanently deleted.
     */
    void deleteByLostItemId(Long lostItemId);

    /**
     * Delete all images for a found item.
     * Called when a found item is permanently deleted.
     */
    void deleteByFoundItemId(Long foundItemId);
}