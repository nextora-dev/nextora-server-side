package lk.iit.nextora.module.lostandfound.service;

// ── Response DTO ─────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;

import java.util.List;

/**
 * Service interface for item image operations.
 *
 * ✅ FIX: Added getImagesForLostItem / getImagesForFoundItem — the original interface
 *         only had saveImage() with no way to retrieve images back.
 * ✅ FIX: Added deleteImage() — cleanup path needed when items are deleted.
 */
public interface ItemImageService {

    /**
     * Save an image URL linked to a lost or found item.
     *
     * @param itemId      ID of the LostItem or FoundItem this image belongs to
     * @param imageUrl    Full URL of the uploaded image (e.g. S3 URL)
     * @param isLostItem  true = link to LostItem, false = link to FoundItem
     */
    void saveImage(Long itemId, String imageUrl, boolean isLostItem);

    /**
     * Retrieve all image URLs for a specific lost item.
     */
    List<String> getImagesForLostItem(Long lostItemId);

    /**
     * Retrieve all image URLs for a specific found item.
     */
    List<String> getImagesForFoundItem(Long foundItemId);

    /**
     * Delete an image by its ID from both the database and object storage.
     */
    void deleteImage(Long imageId);
}