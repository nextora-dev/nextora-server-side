package lk.iit.nextora.module.lostandfound.mapper;

// ── Common project mapper config (shared ComponentModel, etc.) ──────────────
import lk.iit.nextora.common.mapper.MapperConfiguration;

// ── Request DTOs ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;

// ── Entity ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.LostItem;

// ── MapStruct ───────────────────────────────────────────────────────────────
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for LostItem entity <-> DTO conversions.
 *
 * ✅ FIX: Was a plain static utility class — converted to a proper MapStruct @Mapper
 *         interface to match the Kuppi KuppiMapper pattern exactly.
 * ✅ FIX: All field-level @Mapping annotations are explicit so no fields are silently ignored.
 * ✅ FIX: Added updateLostItemFromRequest for null-safe partial updates (same as Kuppi's
 *         updateNoteFromRequest / updateSessionFromRequest).
 * ✅ FIX: Added toResponseList for bulk-mapping used in searchLostItems service method.
 */
@Mapper(config = MapperConfiguration.class)
public interface LostItemMapper {

    // ── Create mapping ──────────────────────────────────────────────────────

    /**
     * Map a CreateLostItemRequest to a new LostItem entity.
     * Fields that are set by the service layer (category, timestamps) are ignored here.
     */
    @Mapping(target = "id", ignore = true)
    // category is resolved from the category name string by the service — not set here
    @Mapping(target = "category", ignore = true)
    // ✅ FIX: active has a @Builder.Default value; ignore so MapStruct doesn't overwrite it
    @Mapping(target = "active", ignore = true)
    // Timestamps are set by @PrePersist — never set manually
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LostItem toEntity(CreateLostItemRequest request);

    // ── Response mapping ────────────────────────────────────────────────────

    /**
     * Map a LostItem entity to an ItemResponse DTO.
     * Flattens the nested ItemCategory into two flat fields: categoryId and category.
     */
    // ✅ FIX: map the nested category.id → categoryId (was not mapped in the static mapper)
    @Mapping(target = "categoryId", source = "category.id")
    // Map the nested category.name → category (the string field on ItemResponse)
    @Mapping(target = "category", source = "category.name")
    ItemResponse toResponse(LostItem lostItem);

    /**
     * Map a list of LostItem entities to a list of ItemResponse DTOs.
     * Used in searchLostItems service methods.
     */
    List<ItemResponse> toResponseList(List<LostItem> lostItems);

    // ── Update mapping ──────────────────────────────────────────────────────

    /**
     * Apply non-null fields from UpdateItemRequest to an existing LostItem entity.
     * Null fields in the request are ignored — this is a partial (PATCH-style) update.
     *
     * ✅ FIX: the original service did manual null checks on every field; this
     *         replaces all that boilerplate with a single mapper call.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateLostItemFromRequest(UpdateItemRequest request, @MappingTarget LostItem lostItem);
}