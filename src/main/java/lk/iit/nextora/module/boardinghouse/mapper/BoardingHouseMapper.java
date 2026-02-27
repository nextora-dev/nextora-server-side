package lk.iit.nextora.module.boardinghouse.mapper;

import lk.iit.nextora.common.enums.Gender;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.springframework.stereotype.Component;

@Component
public class BoardingHouseMapper {

    public BoardingHouse toEntity(CreateBoardingHouseRequest request) {

        return BoardingHouse.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .city(request.getCity())
                .address(request.getAddress())
                .gender(request.getGender())
                .contactNumber1(request.getContactNumber1())
                .contactNumber2(request.getContactNumber2())
                .keyMoneyRequired(request.getKeyMoneyRequired())
                .waterBillIncluded(request.getWaterBillIncluded())
                .electricityBillIncluded(request.getElectricityBillIncluded())
                .foodIncluded(request.getFoodIncluded())
                .furnitureIncluded(request.getFurnitureIncluded())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    public BoardingHouseResponse toResponse(BoardingHouse entity) {

        return BoardingHouseResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .city(entity.getCity())
                .address(entity.getAddress())
                .gender(entity.getGender().name())
                .contactNumber1(entity.getContactNumber1())
                .contactNumber2(entity.getContactNumber2())
                .keyMoneyRequired(entity.getKeyMoneyRequired())
                .waterBillIncluded(entity.getWaterBillIncluded())
                .electricityBillIncluded(entity.getElectricityBillIncluded())
                .foodIncluded(entity.getFoodIncluded())
                .furnitureIncluded(entity.getFurnitureIncluded())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}