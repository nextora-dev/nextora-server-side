package lk.iit.nextora.module.boardinghouse.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.BoardingHouseFilterRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseImageResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardingHouseService {

    // ==================== Browse Operations (All authenticated users) ====================

    PagedResponse<BoardingHouseResponse> getAllAvailable(Pageable pageable);

    BoardingHouseResponse getById(Long houseId);

    PagedResponse<BoardingHouseResponse> search(String keyword, Pageable pageable);

    PagedResponse<BoardingHouseResponse> filter(BoardingHouseFilterRequest filterRequest);

    PagedResponse<BoardingHouseResponse> filterByCity(String city, Pageable pageable);

    PagedResponse<BoardingHouseResponse> filterByDistrict(String district, Pageable pageable);

    // ==================== CRUD Operations (Admin, Non-Academic Staff, Super Admin) ====================

    BoardingHouseResponse create(CreateBoardingHouseRequest request);

    BoardingHouseResponse update(Long houseId, UpdateBoardingHouseRequest request);

    void delete(Long houseId);

    PagedResponse<BoardingHouseResponse> getAllForAdmin(Pageable pageable);

    PagedResponse<BoardingHouseResponse> getMyListings(Pageable pageable);

    // ==================== Image Operations (S3) ====================

    List<BoardingHouseImageResponse> uploadImages(Long houseId, List<MultipartFile> files);

    void deleteImage(Long houseId, Long imageId);

    BoardingHouseImageResponse setPrimaryImage(Long houseId, Long imageId);

    List<BoardingHouseImageResponse> getImages(Long houseId);

    // ==================== Super Admin Operations ====================

    BoardingHouseResponse adminUpdate(Long houseId, UpdateBoardingHouseRequest request);

    void adminDelete(Long houseId);

    void permanentlyDelete(Long houseId);

    BoardingHouseStatsResponse getStats();
}
