package lk.iit.nextora.module.boardinghouse.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Boarding House operations
 */
public interface BoardingHouseService {

    // ==================== Student Operations ====================

    /** Get all available listings */
    PagedResponse<BoardingHouseResponse> getAllAvailable(Pageable pageable);

    /** Get a specific listing by ID (increments view count) */
    BoardingHouseResponse getById(Long houseId);

    /** Search listings by keyword */
    PagedResponse<BoardingHouseResponse> search(String keyword, Pageable pageable);

    /** Filter listings by city, district, gender, price */
    PagedResponse<BoardingHouseResponse> filter(BoardingHouseFilterRequest filterRequest);

    /** Filter by city */
    PagedResponse<BoardingHouseResponse> filterByCity(String city, Pageable pageable);

    /** Filter by district */
    PagedResponse<BoardingHouseResponse> filterByDistrict(String district, Pageable pageable);

    // ==================== Admin Operations ====================

    /** Create a new listing */
    BoardingHouseResponse create(CreateBoardingHouseRequest request);

    /** Update own listing */
    BoardingHouseResponse update(Long houseId, UpdateBoardingHouseRequest request);

    /** Soft delete own listing */
    void delete(Long houseId);

    /** Get all listings (including unavailable) - for admin */
    PagedResponse<BoardingHouseResponse> getAllForAdmin(Pageable pageable);

    /** Get own listings */
    PagedResponse<BoardingHouseResponse> getMyListings(Pageable pageable);

    // ==================== Super Admin Operations ====================

    /** Admin override update any listing */
    BoardingHouseResponse adminUpdate(Long houseId, UpdateBoardingHouseRequest request);

    /** Admin soft delete any listing */
    void adminDelete(Long houseId);

    /** Permanently delete a listing */
    void permanentlyDelete(Long houseId);

    /** Get platform statistics */
    BoardingHouseStatsResponse getStats();
}
