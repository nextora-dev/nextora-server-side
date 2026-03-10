package lk.iit.nextora.module.lostandfound.mapper;

// ── Common project mapper config ────────────────────────────────────────────
import lk.iit.nextora.common.mapper.MapperConfiguration;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Entity ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.FoundItem;

// ── MapStruct ───────────────────────────────────────────────────────────────
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for FoundItem entity <-> DTO conversions.
 *
 * ✅ FIX: Was a plain static utility class — converted to a proper MapStruct @Mapper
 *         interface to match the Kuppi KuppiMapper pattern exactly.
 * ✅ FIX: The static mapper had no null-check guards; MapStruct handles null inputs safely.
 * ✅ FIX: Added toResponseList and updateFoundItemFromRequest methods.
 */
@Mapper(config = MapperConfiguration.class)
public interface FoundItemMapper {

    // ── Create mapping ──────────────────────────────────────────────────────

    /**
     * Map a CreateFoundItemRequest to a new FoundItem entity.
     * category is a String in the request — resolved to an ItemCategory entity
     * by the service before calling this mapper.
     */
    @Mapping(target = "id", ignore = true)
    // category entity is set by service after resolving the category name — ignore here
    @Mapping(target = "category", ignore = true)
    // active defaults to true via @Builder.Default — do not overwrite with mapper
    @Mapping(target = "active", ignore = true)
    // JPA lifecycle sets these automatically — never set manually
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FoundItem toEntity(CreateFoundItemRequest request);

    // ── Response mapping ────────────────────────────────────────────────────

    /**
     * Map a FoundItem entity to an ItemResponse DTO.
     * Flattens the nested ItemCategory into categoryId (Long) and category (String).
     */
    // ✅ FIX: categoryId was not mapped in the original static mapper
    @Mapping(target = "categoryId", source = "category.id")
    // Map the category entity's name string to the flat "category" field on the response
    @Mapping(target = "category", source = "category.name")
    ItemResponse toResponse(FoundItem foundItem);

    /**
     * Bulk-map a list of FoundItem entities to a list of ItemResponse DTOs.
     */
    List<ItemResponse> toResponseList(List<FoundItem> foundItems);

    // ── Update mapping ──────────────────────────────────────────────────────

    /**
     * Apply non-null fields from UpdateItemRequest onto an existing FoundItem entity.
     * Null request fields are ignored — partial (PATCH-style) update.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFoundItemFromRequest(UpdateItemRequest request, @MappingTarget FoundItem foundItem);
}