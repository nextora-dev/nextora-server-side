package lk.iit.nextora.module.boardinghouse.controller;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boarding-houses")
@RequiredArgsConstructor
public class BoardingHouseController {

    private final BoardingHouseService service;

    //  Admin / Super Admin only
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public BoardingHouseResponse create(@RequestBody CreateBoardingHouseRequest request) {
        return service.create(request);
    }

    //  Admin / Super Admin only
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public BoardingHouseResponse update(@PathVariable Long id,
                                        @RequestBody UpdateBoardingHouseRequest request) {
        return service.update(id, request);
    }

    //  Admin / Super Admin only
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    //  Student + Admin + Super Admin
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}")
    public BoardingHouseResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    //  Student + Admin + Super Admin
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping
    public PagedResponse<BoardingHouseResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    //  Student + Admin + Super Admin
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/search")
    public PagedResponse<BoardingHouseResponse> filter(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRent,
            @RequestParam(required = false) Double maxRent,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean withFood,
            @RequestParam(required = false) Boolean withFurniture,
            Pageable pageable
    ) {
        return service.filter(city, minRent, maxRent, gender, withFood, withFurniture, pageable);
    }
}
