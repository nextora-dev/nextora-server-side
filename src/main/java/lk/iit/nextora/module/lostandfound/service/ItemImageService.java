package lk.iit.nextora.module.lostandfound.service;

public interface ItemImageService {

    void saveImage(Long itemId, String imageUrl, boolean isLostItem);
}
