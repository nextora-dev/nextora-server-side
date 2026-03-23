package lk.iit.nextora.module.lostandfound.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;
import lk.iit.nextora.module.lostandfound.entity.Claim;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface ClaimMapper {

    @Mapping(target = "lostItemId", source = "lostItem.id")
    @Mapping(target = "lostItemTitle", source = "lostItem.title")
    @Mapping(target = "foundItemId", source = "foundItem.id")
    @Mapping(target = "foundItemTitle", source = "foundItem.title")
    ClaimResponse toResponse(Claim claim);

    List<ClaimResponse> toResponseList(List<Claim> claims);
}
