package lk.iit.nextora.module.lostandfound.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface LostItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "reporterName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LostItem toEntity(CreateLostItemRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "imageUrls", ignore = true)
    ItemResponse toResponse(LostItem lostItem);

    List<ItemResponse> toResponseList(List<LostItem> lostItems);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "reporterName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateLostItemFromRequest(UpdateItemRequest request, @MappingTarget LostItem lostItem);
}
