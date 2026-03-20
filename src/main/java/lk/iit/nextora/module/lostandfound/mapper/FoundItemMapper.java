package lk.iit.nextora.module.lostandfound.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface FoundItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "reporterName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FoundItem toEntity(CreateFoundItemRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "imageUrls", ignore = true)
    ItemResponse toResponse(FoundItem foundItem);

    List<ItemResponse> toResponseList(List<FoundItem> foundItems);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "reporterName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFoundItemFromRequest(UpdateItemRequest request, @MappingTarget FoundItem foundItem);
}
