package lk.iit.nextora.module.lostandfound.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemImageService {

    String uploadImage(Long itemId, boolean isLostItem, MultipartFile file);

    List<String> uploadImages(Long itemId, boolean isLostItem, List<MultipartFile> files);

    List<String> getImagesForLostItem(Long lostItemId);

    List<String> getImagesForFoundItem(Long foundItemId);

    void deleteImage(Long imageId);

    void deleteAllImagesForLostItem(Long lostItemId);

    void deleteAllImagesForFoundItem(Long foundItemId);
}
