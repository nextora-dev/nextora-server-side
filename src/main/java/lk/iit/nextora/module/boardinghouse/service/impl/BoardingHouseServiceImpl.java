package lk.iit.nextora.module.boardinghouse.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.entity.*;
import lk.iit.nextora.module.boardinghouse.mapper.BoardingHouseMapper;
import lk.iit.nextora.module.boardinghouse.repository.BoardingHouseRepository;
import lk.iit.nextora.module.boardinghouse.service.BoardingHouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardingHouseServiceImpl implements BoardingHouseService {

    private final BoardingHouseRepository repository;
    private final BoardingHouseMapper mapper;

    // ---------------- CREATE ----------------
    @Override
    @Transactional
    public BoardingHouseResponse create(CreateBoardingHouseRequest request) {

        BoardingHouse house = BoardingHouse.builder()
                .name(request.getName())
                .description(request.getDescription())
                .monthlyRent(request.getMonthlyRent())
                .keyMoneyRequired(request.getKeyMoneyRequired())
                .genderType(BoardingGenderType.valueOf(request.getGenderType()))
                .withFood(request.getWithFood())
                .withFurniture(request.getWithFurniture())
                .location(BoardingLocation.builder()
                        .city(request.getCity())
                        .address(request.getAddress())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .build())
                .contact(BoardingContact.builder()
                        .contactNumber1(request.getContactNumber1())
                        .contactNumber2(request.getContactNumber2())
                        .build())
                .waterBill(BoardingBill.builder()
                        .billType(BoardingBillType.WATER)
                        .included(request.getWaterBillIncluded())
                        .build())
                .electricityBill(BoardingBill.builder()
                        .billType(BoardingBillType.ELECTRICITY)
                        .included(request.getElectricityBillIncluded())
                        .build())
                .build();

        return mapper.toResponse(repository.save(house));
    }

    // ---------------- UPDATE ----------------
    @Override
    @Transactional
    public BoardingHouseResponse update(Long id, UpdateBoardingHouseRequest request) {
        BoardingHouse house = findHouse(id);

        if (request.getName() != null) house.setName(request.getName());
        if (request.getDescription() != null) house.setDescription(request.getDescription());
        if (request.getMonthlyRent() != null) house.setMonthlyRent(request.getMonthlyRent());
        if (request.getKeyMoneyRequired() != null) house.setKeyMoneyRequired(request.getKeyMoneyRequired());
        if (request.getGenderType() != null)
            house.setGenderType(BoardingGenderType.valueOf(request.getGenderType()));
        if (request.getWithFood() != null) house.setWithFood(request.getWithFood());
        if (request.getWithFurniture() != null) house.setWithFurniture(request.getWithFurniture());

        // Location
        if (request.getCity() != null) house.getLocation().setCity(request.getCity());
        if (request.getAddress() != null) house.getLocation().setAddress(request.getAddress());
        if (request.getLatitude() != null) house.getLocation().setLatitude(request.getLatitude());
        if (request.getLongitude() != null) house.getLocation().setLongitude(request.getLongitude());

        // Contact
        if (request.getContactNumber1() != null)
            house.getContact().setContactNumber1(request.getContactNumber1());
        if (request.getContactNumber2() != null)
            house.getContact().setContactNumber2(request.getContactNumber2());

        // Bills
        if (request.getWaterBillIncluded() != null)
            house.getWaterBill().setIncluded(request.getWaterBillIncluded());
        if (request.getElectricityBillIncluded() != null)
            house.getElectricityBill().setIncluded(request.getElectricityBillIncluded());

        return mapper.toResponse(repository.save(house));
    }

    // ---------------- DELETE (SOFT DELETE) ----------------
    @Override
    @Transactional
    public void delete(Long id) {
        BoardingHouse house = findHouse(id);
        house.softDelete();
        repository.save(house);
    }

    // ---------------- GET BY ID ----------------
    @Override
    public BoardingHouseResponse getById(Long id) {
        return mapper.toResponse(findHouse(id));
    }

    // ---------------- GET ALL ----------------
    @Override
    public PagedResponse<BoardingHouseResponse> getAll(Pageable pageable) {
        Page<BoardingHouse> page = repository.findByIsDeletedFalse(pageable);
        return toPagedResponse(page);
    }

    // ---------------- FILTER ----------------
    @Override
    public PagedResponse<BoardingHouseResponse> filter(String city,
                                                       Double minRent,
                                                       Double maxRent,
                                                       String gender,
                                                       Boolean withFood,
                                                       Boolean withFurniture,
                                                       Pageable pageable) {

        Page<BoardingHouse> page;

        if (city != null && minRent != null && maxRent != null) {
            page = repository.findByLocation_CityIgnoreCaseAndMonthlyRentBetweenAndIsDeletedFalse(
                    city, minRent, maxRent, pageable);
        } else if (city != null) {
            page = repository.findByLocation_CityIgnoreCaseAndIsDeletedFalse(city, pageable);
        } else if (minRent != null && maxRent != null) {
            page = repository.findByMonthlyRentBetweenAndIsDeletedFalse(minRent, maxRent, pageable);
        } else if (gender != null) {
            page = repository.findByGenderTypeAndIsDeletedFalse(
                    BoardingGenderType.valueOf(gender), pageable);
        } else if (withFood != null && withFurniture != null) {
            page = repository.findByWithFoodAndWithFurnitureAndIsDeletedFalse(
                    withFood, withFurniture, pageable);
        } else {
            page = repository.findByIsDeletedFalse(pageable);
        }

        return toPagedResponse(page);
    }

    // ---------------- HELPERS ----------------
    private BoardingHouse findHouse(Long id) {
        return repository.findById(id)
                .filter(h -> !h.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("BoardingHouse", "id", id));
    }

    private PagedResponse<BoardingHouseResponse> toPagedResponse(Page<BoardingHouse> page) {
        List<BoardingHouseResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .toList();

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
