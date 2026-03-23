package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemImage;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemImageRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.ItemImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemImageServiceImpl implements ItemImageService {

    private static final String S3_FOLDER = "lost-and-found";
    private static final int MAX_IMAGES_PER_ITEM = 5;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final ItemImageRepository imageRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public String uploadImage(Long itemId, boolean isLostItem, MultipartFile file) {
        validateFile(file);
        validateImageCount(itemId, isLostItem);

        String folder = S3_FOLDER + (isLostItem ? "/lost/" + itemId : "/found/" + itemId);
        String imageUrl = s3Service.uploadFilePublic(file, folder);

        int currentCount = isLostItem
                ? (int) imageRepository.countByLostItemId(itemId)
                : (int) imageRepository.countByFoundItemId(itemId);

        ItemImage image = ItemImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(currentCount)
                .build();

        if (isLostItem) {
            LostItem lostItem = lostItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", itemId));
            image.setLostItem(lostItem);
        } else {
            FoundItem foundItem = foundItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", itemId));
            image.setFoundItem(foundItem);
        }

        imageRepository.save(image);
        log.info("Image uploaded for {} item {}: {}", isLostItem ? "lost" : "found", itemId, imageUrl);

        return imageUrl;
    }

    @Override
    @Transactional
    public List<String> uploadImages(Long itemId, boolean isLostItem, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image file is required");
        }

        int currentCount = isLostItem
                ? (int) imageRepository.countByLostItemId(itemId)
                : (int) imageRepository.countByFoundItemId(itemId);

        if (currentCount + files.size() > MAX_IMAGES_PER_ITEM) {
            throw new BadRequestException(
                    String.format("Maximum %d images allowed per item. Current: %d, Trying to add: %d",
                            MAX_IMAGES_PER_ITEM, currentCount, files.size()));
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadImage(itemId, isLostItem, file));
        }
        return urls;
    }

    @Override
    public List<String> getImagesForLostItem(Long lostItemId) {
        return imageRepository.findByLostItemIdOrderByDisplayOrderAsc(lostItemId)
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getImagesForFoundItem(Long foundItemId) {
        return imageRepository.findByFoundItemIdOrderByDisplayOrderAsc(foundItemId)
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ItemImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemImage", "id", imageId));

        try {
            String url = image.getImageUrl();
            String key = extractS3Key(url);
            if (key != null) {
                s3Service.deleteFile(key);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from S3 for imageId {}: {}", imageId, e.getMessage());
        }

        imageRepository.delete(image);
        log.info("Image {} deleted", imageId);
    }

    @Override
    @Transactional
    public void deleteAllImagesForLostItem(Long lostItemId) {
        List<ItemImage> images = imageRepository.findByLostItemIdOrderByDisplayOrderAsc(lostItemId);
        for (ItemImage image : images) {
            try {
                String key = extractS3Key(image.getImageUrl());
                if (key != null) {
                    s3Service.deleteFile(key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for image {}: {}", image.getId(), e.getMessage());
            }
        }
        imageRepository.deleteByLostItemId(lostItemId);
        log.info("All images deleted for lost item {}", lostItemId);
    }

    @Override
    @Transactional
    public void deleteAllImagesForFoundItem(Long foundItemId) {
        List<ItemImage> images = imageRepository.findByFoundItemIdOrderByDisplayOrderAsc(foundItemId);
        for (ItemImage image : images) {
            try {
                String key = extractS3Key(image.getImageUrl());
                if (key != null) {
                    s3Service.deleteFile(key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for image {}: {}", image.getId(), e.getMessage());
            }
        }
        imageRepository.deleteByFoundItemId(foundItemId);
        log.info("All images deleted for found item {}", foundItemId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Image file size must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed (JPEG, PNG, GIF, WebP)");
        }
    }

    private void validateImageCount(Long itemId, boolean isLostItem) {
        long count = isLostItem
                ? imageRepository.countByLostItemId(itemId)
                : imageRepository.countByFoundItemId(itemId);
        if (count >= MAX_IMAGES_PER_ITEM) {
            throw new BadRequestException(
                    String.format("Maximum %d images allowed per item", MAX_IMAGES_PER_ITEM));
        }
    }

    private String extractS3Key(String url) {
        if (url == null) return null;
        String bucketName = s3Service.getBucketName();
        int idx = url.indexOf(bucketName);
        if (idx >= 0) {
            String afterBucket = url.substring(idx + bucketName.length());
            if (afterBucket.contains(".amazonaws.com/")) {
                return afterBucket.substring(afterBucket.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            }
        }
        return null;
    }
}
