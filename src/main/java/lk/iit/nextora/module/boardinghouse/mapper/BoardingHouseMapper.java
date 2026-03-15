package lk.iit.nextora.module.boardinghouse.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.boardinghouse.dto.request.CreateBoardingHouseRequest;
import lk.iit.nextora.module.boardinghouse.dto.response.BoardingHouseResponse;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * MapStruct mapper for Boarding House module
 */
@Mapper(config = MapperConfiguration.class)
public interface BoardingHouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    BoardingHouse toEntity(CreateBoardingHouseRequest request);

    @Mapping(target = "postedById", source = "postedBy.id")
    @Mapping(target = "postedByName", expression = "java(boardingHouse.getPostedBy().getFullName())")
    @Mapping(target = "genderPreferenceDisplay", expression = "java(boardingHouse.getGenderPreference().getDisplayName())")
    @Mapping(target = "formattedPrice", expression = "java(formatPrice(boardingHouse.getPrice()))")
    BoardingHouseResponse toResponse(BoardingHouse boardingHouse);

    List<BoardingHouseResponse> toResponseList(List<BoardingHouse> boardingHouses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateFromRequest(
            lk.iit.nextora.module.boardinghouse.dto.request.UpdateBoardingHouseRequest request,
            @MappingTarget BoardingHouse boardingHouse);

    default String formatPrice(BigDecimal price) {
        if (price == null) return "N/A";
        return "Rs. " + String.format("%,.0f", price) + "/month";
    }
}
