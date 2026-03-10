package lk.iit.nextora.module.lostandfound.dto.response;

// ── Java time ───────────────────────────────────────────────────────────────
import java.time.LocalDateTime;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned for any single lost or found item.
 * Used as the element type in ItemListResponse and PagedResponse<ItemResponse>.
 *
 * ✅ FIX: Added @NoArgsConstructor — required by Jackson for deserialization
 *         and by MapStruct if ever migrated to interface-based mapping.
 * ✅ FIX: Added @AllArgsConstructor — required by @Builder pattern consistency.
 * ✅ FIX: Added categoryId alongside categoryName — useful for UI dropdowns / deep links.
 * ✅ FIX: Added updatedAt — matches Kuppi response DTOs that expose both timestamps.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    // Database primary key of the item
    private Long id;

    // Short descriptive title of the lost / found item
    private String title;

    // Longer description with identifying details (color, brand, etc.)
    private String description;

    // ✅ FIX: added categoryId — allows frontend to filter/link by category without
    //         needing a second lookup request
    private Long categoryId;

    // Human-readable category name (e.g. "Electronics", "Clothing")
    private String category;

    // Where the item was lost or found
    private String location;

    // Contact info of the reporter
    private String contactNumber;

    // Whether the item listing is still active (false = resolved / archived)
    private boolean active;

    // When the item was first reported — set automatically via @PrePersist
    private LocalDateTime createdAt;

    // ✅ FIX: added updatedAt — Kuppi responses always expose both timestamps
    private LocalDateTime updatedAt;
}