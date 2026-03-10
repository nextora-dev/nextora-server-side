package lk.iit.nextora.module.lostandfound.mapper;

// ── Common project mapper config ────────────────────────────────────────────
import lk.iit.nextora.common.mapper.MapperConfiguration;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.ClaimResponse;

// ── Entity ──────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.Claim;

// ── MapStruct ───────────────────────────────────────────────────────────────
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Claim entity <-> DTO conversions.
 *
 * ✅ FIX: Was a plain static class — converted to MapStruct @Mapper interface.
 * ✅ FIX: Original had no null-check: claim.getLostItem().getId() threw NullPointerException
 *         if lostItem was null. MapStruct generates safe null-handling code automatically.
 * ✅ FIX: Added all the new ClaimResponse fields (lostItemTitle, foundItemTitle,
 *         claimantId, claimantName, proofDescription, rejectionReason, createdAt, updatedAt).
 */
@Mapper(config = MapperConfiguration.class)
public interface ClaimMapper {

    /**
     * Map a Claim entity to a ClaimResponse DTO.
     * Flattens nested LostItem and FoundItem objects into flat ID + title fields.
     */

    // ── Lost item flattening ────────────────────────────────────────────────

    // Map the nested lostItem's primary key to lostItemId on the response
    @Mapping(target = "lostItemId", source = "lostItem.id")
    // ✅ FIX: lostItemTitle was missing — map from nested lostItem.title
    @Mapping(target = "lostItemTitle", source = "lostItem.title")

    // ── Found item flattening ───────────────────────────────────────────────

    // Map the nested foundItem's primary key to foundItemId on the response
    @Mapping(target = "foundItemId", source = "foundItem.id")
    // ✅ FIX: foundItemTitle was missing — map from nested foundItem.title
    @Mapping(target = "foundItemTitle", source = "foundItem.title")

    // ── Claimant fields ─────────────────────────────────────────────────────

    // claimantId and claimantName are flat columns on Claim — mapped by name automatically
    // (no explicit @Mapping needed when source and target field names match)

    // ── Direct field mappings ───────────────────────────────────────────────

    // proofDescription, status, rejectionReason, createdAt, updatedAt all map by name
    ClaimResponse toResponse(Claim claim);

    /**
     * Bulk-map a list of Claim entities to a list of ClaimResponse DTOs.
     * Used in getClaimsByStatus and getMyClaims service methods.
     */
    List<ClaimResponse> toResponseList(List<Claim> claims);
}