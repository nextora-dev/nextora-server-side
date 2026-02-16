package lk.iit.nextora.module.boardinghouse.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfiguration.class)
public interface BoardingHouseMapper {

    @Mapping(target = "city", source = "location.city")
    @Mapping(target = "address", source = "location.address")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    @Mapping(target = "contactNumber1", source = "contact.contactNumber1")
    @Mapping(target = "contactNumber2", source = "contact.contactNumber2")
    @Mapping(target = "waterBillIncluded", source = "waterBill.included")
    @Mapping(target = "electricityBillIncluded", source = "electricityBill.included")
    BoardingHouseResponse toResponse(BoardingHouse house);
}
