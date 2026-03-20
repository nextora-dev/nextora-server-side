package lk.iit.nextora.module.boardinghouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseStatsResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin/Super Admin controller for Boarding House management.
 * Allows override update, force delete, and platform stats.
 */
@RestController
@RequestMapping(ApiConstants.BOARDINGHOUSE_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Boarding House Admin", description = "Admin endpoints for boarding house management")
public class BoardingHouseAdminController {

    private final BoardingHouseService boardingHouseService;

    @PutMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @Operation(summary = "Admin update listing", description = "Admin override update any boarding house listing")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:ADMIN_UPDATE')")
    public ApiResponse<BoardingHouseResponse> adminUpdate(
            @PathVariable Long houseId,
            @Valid @RequestBody UpdateBoardingHouseRequest request) {
        return ApiResponse.success("Boarding house updated by admin",
                boardingHouseService.adminUpdate(houseId, request));
    }

    @DeleteMapping(ApiConstants.BOARDINGHOUSE_HOUSES_BY_ID)
    @Operation(summary = "Admin delete listing", description = "Admin soft delete any boarding house listing")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:ADMIN_DELETE')")
    public ApiResponse<Void> adminDelete(@PathVariable Long houseId) {
        boardingHouseService.adminDelete(houseId);
        return ApiResponse.success("Boarding house removed by admin");
    }

    @DeleteMapping(ApiConstants.BOARDINGHOUSE_HOUSES_PERMANENT)
    @Operation(summary = "Permanently delete listing", description = "Permanently delete a boarding house (Super Admin only)")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDelete(@PathVariable Long houseId) {
        boardingHouseService.permanentlyDelete(houseId);
        return ApiResponse.success("Boarding house permanently deleted");
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform stats", description = "Get boarding house platform statistics")
    @PreAuthorize("hasAuthority('BOARDING_HOUSE:ADMIN_READ')")
    public ApiResponse<BoardingHouseStatsResponse> getStats() {
        return ApiResponse.success("Statistics retrieved successfully",
                boardingHouseService.getStats());
    }
}
