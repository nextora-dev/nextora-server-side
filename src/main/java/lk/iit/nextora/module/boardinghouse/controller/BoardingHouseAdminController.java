package lk.iit.nextora.module.boardinghouse.controller;

import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.BOARDINGHOUSE_ADMIN)
@RequiredArgsConstructor
public class BoardingHouseAdminController {

    private final BoardingHouseService service;

    @PostMapping(ApiConstants.BOARDINGHOUSE_HOUSES)
    @PreAuthorize("hasAuthority('BOARDINGHOUSE:CREATE')")
    public ApiResponse<BoardingHouseResponse> create(
            @Valid @RequestBody CreateBoardingHouseRequest request) {

        return ApiResponse.success(
                "Created successfully",
                service.create(request)
        );
    }

    @PutMapping(ApiConstants.BOARDINGHOUSE_HOUSES)
    @PreAuthorize("hasAuthority('BOARDINGHOUSE:UPDATE')")
    public ApiResponse<BoardingHouseResponse> update(
            @Valid @RequestBody UpdateBoardingHouseRequest request) {

        return ApiResponse.success(
                "Updated successfully",
                service.update(request)
        );
    }

    @DeleteMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @PreAuthorize("hasAuthority('BOARDINGHOUSE:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long houseId) {

        service.delete(houseId);

        return ApiResponse.success("Deleted successfully");
    }
}