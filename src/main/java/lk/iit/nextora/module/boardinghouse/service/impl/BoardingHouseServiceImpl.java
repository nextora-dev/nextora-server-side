package lk.iit.nextora.module.boardinghouse.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.repository.BaseUserRepository;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseImageResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouseImage;
import lk.iit.nextora.module.boardinghouse.mapper.BoardingHouseMapper;
import lk.iit.nextora.module.boardinghouse.repository.BoardingHouseImageRepository;
import lk.iit.nextora.module.boardinghouse.repository.BoardingHouseRepository;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lk.iit.nextora.infrastructure.notification.service.BoardingHouseNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardingHouseServiceImpl implements BoardingHouseService {

    private static final String S3_BOARDING_HOUSE_FOLDER = "boarding-houses/images";
    private static final int MAX_IMAGES_PER_LISTING = 10;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private final BoardingHouseRepository boardingHouseRepository;
    private final BoardingHouseImageRepository boardingHouseImageRepository;
    private final BoardingHouseMapper boardingHouseMapper;
    private final SecurityService securityService;
    private final S3Service s3Service;
    private final List<BaseUserRepository<? extends BaseUser>> userRepositories;
    private final BoardingHouseNotificationService boardingHouseNotificationService;

    // ==================== Browse Operations (All authenticated users) ====================

    @Override
    public PagedResponse<BoardingHouseResponse> getAllAvailable(Pageable pageable) {
        Page<BoardingHouse> page = boardingHouseRepository.findByIsDeletedFalseAndIsAvailableTrue(pageable);
        return toPagedResponse(page);
    }

    @Override
    @Transactional
    public BoardingHouseResponse getById(Long houseId) {
        BoardingHouse house = findById(houseId);
        house.incrementViewCount();
        boardingHouseRepository.save(house);
        return boardingHouseMapper.toResponse(house);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAvailable(pageable);
        }
        Page<BoardingHouse> page = boardingHouseRepository.searchByKeyword(keyword.trim(), pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> filter(BoardingHouseFilterRequest filterRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                filterRequest.getSortBy());
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<BoardingHouse> page = boardingHouseRepository.filterBoardingHouses(
                filterRequest.getCity(),
                filterRequest.getDistrict(),
                filterRequest.getGenderPreference(),
                filterRequest.getMinPrice(),
                filterRequest.getMaxPrice(),
                pageable
        );
        return toPagedResponse(page);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> filterByCity(String city, Pageable pageable) {
        Page<BoardingHouse> page = boardingHouseRepository.findByCity(city, pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> filterByDistrict(String district, Pageable pageable) {
        Page<BoardingHouse> page = boardingHouseRepository.findByDistrict(district, pageable);
        return toPagedResponse(page);
    }

    // ==================== CRUD Operations ====================

    @Override
    @Transactional
    public BoardingHouseResponse create(CreateBoardingHouseRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BaseUser poster = findUserById(currentUserId);

        BoardingHouse house = boardingHouseMapper.toEntity(request);
        house.setPostedBy(poster);

        if (house.getViewCount() == null) {
            house.setViewCount(0L);
        }
        if (house.getIsAvailable() == null) {
            house.setIsAvailable(true);
        }

        house = boardingHouseRepository.save(house);
        log.info("Boarding house listing created by user {}: {}", currentUserId, house.getId());

        boardingHouseNotificationService.notifyNewListing(
                house.getId(), house.getTitle(), house.getCity(),
                house.getDistrict(), house.getPrice()
        );

        return boardingHouseMapper.toResponse(house);
    }

    @Override
    @Transactional
    public BoardingHouseResponse update(Long houseId, UpdateBoardingHouseRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        // Owner or admin can update
        if (!isOwnerOrAdmin(house, currentUserId)) {
            throw new UnauthorizedException("You can only modify your own listings");
        }

        boardingHouseMapper.updateFromRequest(request, house);
        house = boardingHouseRepository.save(house);

        log.info("Boarding house {} updated by user {}", houseId, currentUserId);
        return boardingHouseMapper.toResponse(house);
    }

    @Override
    @Transactional
    public void delete(Long houseId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        if (!isOwnerOrAdmin(house, currentUserId)) {
            throw new UnauthorizedException("You can only delete your own listings");
        }

        house.softDelete();
        boardingHouseRepository.save(house);

        log.info("Boarding house {} soft deleted by user {}", houseId, currentUserId);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> getAllForAdmin(Pageable pageable) {
        Page<BoardingHouse> page = boardingHouseRepository.findByIsDeletedFalse(pageable);
        return toPagedResponse(page);
    }

    @Override
    public PagedResponse<BoardingHouseResponse> getMyListings(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<BoardingHouse> page = boardingHouseRepository.findByPostedByIdAndIsDeletedFalse(currentUserId, pageable);
        return toPagedResponse(page);
    }

    // ==================== Image Operations (S3) ====================

    @Override
    @Transactional
    public List<BoardingHouseImageResponse> uploadImages(Long houseId, List<MultipartFile> files) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        if (!isOwnerOrAdmin(house, currentUserId)) {
            throw new UnauthorizedException("You can only upload images to your own listings");
        }

        // Validate file count
        int currentCount = boardingHouseImageRepository.countByBoardingHouseId(houseId);
        if (currentCount + files.size() > MAX_IMAGES_PER_LISTING) {
            throw new BadRequestException(
                    String.format("Maximum %d images allowed per listing. Currently has %d, trying to add %d",
                            MAX_IMAGES_PER_LISTING, currentCount, files.size()));
        }

        // Validate files
        for (MultipartFile file : files) {
            validateImageFile(file);
        }

        Integer maxOrder = boardingHouseImageRepository.findMaxDisplayOrder(houseId);
        int nextOrder = (maxOrder != null ? maxOrder : -1) + 1;
        boolean isFirstImage = currentCount == 0;

        List<BoardingHouseImage> savedImages = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // Upload to S3
            String s3Key = s3Service.uploadFile(file, S3_BOARDING_HOUSE_FOLDER + "/" + houseId);
            String imageUrl = s3Service.getPublicUrl(s3Key);

            BoardingHouseImage image = BoardingHouseImage.builder()
                    .boardingHouse(house)
                    .imageUrl(imageUrl)
                    .s3Key(s3Key)
                    .displayOrder(nextOrder + i)
                    .isPrimary(isFirstImage && i == 0) // First image of first upload is primary
                    .build();

            savedImages.add(boardingHouseImageRepository.save(image));
        }

        log.info("Uploaded {} images for boarding house {} by user {}", files.size(), houseId, currentUserId);
        return boardingHouseMapper.toImageResponseList(savedImages);
    }

    @Override
    @Transactional
    public void deleteImage(Long houseId, Long imageId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        if (!isOwnerOrAdmin(house, currentUserId)) {
            throw new UnauthorizedException("You can only delete images from your own listings");
        }

        BoardingHouseImage image = boardingHouseImageRepository.findByIdAndBoardingHouseId(imageId, houseId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        // Delete from S3
        try {
            s3Service.deleteFile(image.getS3Key());
        } catch (Exception e) {
            log.warn("Failed to delete image from S3: {}. Continuing with DB deletion.", image.getS3Key(), e);
        }

        // If deleting primary image, set next image as primary
        boolean wasPrimary = image.getIsPrimary();
        boardingHouseImageRepository.delete(image);

        if (wasPrimary) {
            List<BoardingHouseImage> remaining = boardingHouseImageRepository
                    .findByBoardingHouseIdOrderByDisplayOrderAsc(houseId);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsPrimary(true);
                boardingHouseImageRepository.save(remaining.get(0));
            }
        }

        log.info("Deleted image {} from boarding house {} by user {}", imageId, houseId, currentUserId);
    }

    @Override
    @Transactional
    public BoardingHouseImageResponse setPrimaryImage(Long houseId, Long imageId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        if (!isOwnerOrAdmin(house, currentUserId)) {
            throw new UnauthorizedException("You can only modify images of your own listings");
        }

        // Clear current primary
        List<BoardingHouseImage> allImages = boardingHouseImageRepository
                .findByBoardingHouseIdOrderByDisplayOrderAsc(houseId);
        for (BoardingHouseImage img : allImages) {
            if (img.getIsPrimary()) {
                img.setIsPrimary(false);
                boardingHouseImageRepository.save(img);
            }
        }

        // Set new primary
        BoardingHouseImage image = boardingHouseImageRepository.findByIdAndBoardingHouseId(imageId, houseId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
        image.setIsPrimary(true);
        image = boardingHouseImageRepository.save(image);

        log.info("Set primary image {} for boarding house {} by user {}", imageId, houseId, currentUserId);
        return boardingHouseMapper.toImageResponse(image);
    }

    @Override
    public List<BoardingHouseImageResponse> getImages(Long houseId) {
        // Verify house exists
        findById(houseId);
        List<BoardingHouseImage> images = boardingHouseImageRepository
                .findByBoardingHouseIdOrderByDisplayOrderAsc(houseId);
        return boardingHouseMapper.toImageResponseList(images);
    }

    // ==================== Super Admin Operations ====================

    @Override
    @Transactional
    public BoardingHouseResponse adminUpdate(Long houseId, UpdateBoardingHouseRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        boardingHouseMapper.updateFromRequest(request, house);
        house = boardingHouseRepository.save(house);

        log.info("Boarding house {} updated by admin {}", houseId, currentUserId);
        return boardingHouseMapper.toResponse(house);
    }

    @Override
    @Transactional
    public void adminDelete(Long houseId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        house.softDelete();
        boardingHouseRepository.save(house);

        boardingHouseNotificationService.notifyListingRemovedByAdmin(
                houseId, house.getPostedBy().getId(), house.getTitle()
        );

        log.info("Boarding house {} soft deleted by admin {}", houseId, currentUserId);
    }

    @Override
    @Transactional
    public void permanentlyDelete(Long houseId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = boardingHouseRepository.findById(houseId)
                .orElseThrow(() -> new ResourceNotFoundException("BoardingHouse", "id", houseId));

        // Delete all images from S3
        List<BoardingHouseImage> images = boardingHouseImageRepository
                .findByBoardingHouseIdOrderByDisplayOrderAsc(houseId);
        for (BoardingHouseImage image : images) {
            try {
                s3Service.deleteFile(image.getS3Key());
            } catch (Exception e) {
                log.warn("Failed to delete image from S3 during permanent delete: {}", image.getS3Key(), e);
            }
        }

        boardingHouseRepository.delete(house);
        log.info("Boarding house {} permanently deleted by super admin {}", houseId, currentUserId);
    }

    @Override
    public BoardingHouseStatsResponse getStats() {
        long total = boardingHouseRepository.countByIsDeletedFalse();
        long available = boardingHouseRepository.countByIsDeletedFalseAndIsAvailableTrue();
        Long totalViews = boardingHouseRepository.getTotalViews();

        return BoardingHouseStatsResponse.builder()
                .totalListings(total)
                .availableListings(available)
                .unavailableListings(total - available)
                .totalViews(totalViews != null ? totalViews : 0L)
                .maleOnlyListings(boardingHouseRepository.countMaleOnlyListings())
                .femaleOnlyListings(boardingHouseRepository.countFemaleOnlyListings())
                .anyGenderListings(boardingHouseRepository.countAnyGenderListings())
                .build();
    }

    // ==================== Helper Methods ====================

    private BoardingHouse findById(Long houseId) {
        return boardingHouseRepository.findByIdWithDetails(houseId)
                .orElseThrow(() -> new ResourceNotFoundException("BoardingHouse", "id", houseId));
    }

    private BaseUser findUserById(Long userId) {
        for (BaseUserRepository<? extends BaseUser> repo : userRepositories) {
            Optional<? extends BaseUser> opt = repo.findById(userId);
            if (opt.isPresent()) return opt.get();
        }
        throw new ResourceNotFoundException("User", "id", userId);
    }

    private boolean isOwnerOrAdmin(BoardingHouse house, Long userId) {
        if (house.getPostedBy().getId().equals(userId)) {
            return true;
        }
        return securityService.isAdmin();
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Image file cannot be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Image size must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPEG, PNG, and WebP images are allowed");
        }
    }

    private PagedResponse<BoardingHouseResponse> toPagedResponse(Page<BoardingHouse> page) {
        List<BoardingHouseResponse> content = boardingHouseMapper.toResponseList(page.getContent());
        return PagedResponse.<BoardingHouseResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
