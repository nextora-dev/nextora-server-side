package lk.iit.nextora.module.boardinghouse.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.repository.BaseUserRepository;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import lk.iit.nextora.module.boardinghouse.mapper.BoardingHouseMapper;
import lk.iit.nextora.module.boardinghouse.repository.BoardingHouseRepository;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardingHouseServiceImpl implements BoardingHouseService {

    private final BoardingHouseRepository boardingHouseRepository;
    private final BoardingHouseMapper boardingHouseMapper;
    private final SecurityService securityService;
    private final List<BaseUserRepository<? extends BaseUser>> userRepositories;

    // ==================== Student Operations ====================

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
        Page<BoardingHouse> page = boardingHouseRepository.searchByKeyword(keyword, pageable);
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

    // ==================== Admin Operations ====================

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

        house = boardingHouseRepository.save(house);
        log.info("Boarding house listing created by user {}: {}", currentUserId, house.getId());
        return boardingHouseMapper.toResponse(house);
    }

    @Override
    @Transactional
    public BoardingHouseResponse update(Long houseId, UpdateBoardingHouseRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = findById(houseId);

        validateOwnership(house, currentUserId);

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

        validateOwnership(house, currentUserId);

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

        log.info("Boarding house {} soft deleted by admin {}", houseId, currentUserId);
    }

    @Override
    @Transactional
    public void permanentlyDelete(Long houseId) {
        Long currentUserId = securityService.getCurrentUserId();
        BoardingHouse house = boardingHouseRepository.findById(houseId)
                .orElseThrow(() -> new ResourceNotFoundException("BoardingHouse", "id", houseId));

        boardingHouseRepository.delete(house);
        log.info("Boarding house {} permanently deleted by super admin {}", houseId, currentUserId);
    }

    @Override
    public BoardingHouseStatsResponse getStats() {
        long total = boardingHouseRepository.countByIsDeletedFalse();
        long available = boardingHouseRepository.countByIsDeletedFalseAndIsAvailableTrue();

        return BoardingHouseStatsResponse.builder()
                .totalListings(total)
                .availableListings(available)
                .unavailableListings(total - available)
                .build();
    }

    // ==================== Helper Methods ====================

    private BoardingHouse findById(Long houseId) {
        return boardingHouseRepository.findByIdWithDetails(houseId)
                .orElseThrow(() -> new ResourceNotFoundException("BoardingHouse", "id", houseId));
    }

    private BaseUser findUserById(Long userId) {
        for (BaseUserRepository<? extends BaseUser> repo : userRepositories) {
            var opt = repo.findById(userId);
            if (opt.isPresent()) return opt.get();
        }
        throw new ResourceNotFoundException("User", "id", userId);
    }

    private void validateOwnership(BoardingHouse house, Long userId) {
        if (!house.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own listings");
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
