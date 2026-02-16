package lk.iit.nextora.module.lostandfound.repository;

import lk.iit.nextora.module.lostandfound.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    // Get all images of a lost item
    List<ItemImage> findByLostItemId(Long lostItemId);

    // Get all images of a found item
    List<ItemImage> findByFoundItemId(Long foundItemId);

    // Count images for lost item
    long countByLostItemId(Long lostItemId);

    // Count images for found item
    long countByFoundItemId(Long foundItemId);
}
