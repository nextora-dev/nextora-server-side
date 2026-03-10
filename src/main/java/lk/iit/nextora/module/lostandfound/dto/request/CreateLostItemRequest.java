package lk.iit.nextora.module.lostandfound.dto.request;

// ── Jakarta validation constraints ──────────────────────────────────────────
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reporting a new lost item.
 * Used by any authenticated student who has lost an item on campus.
 *
 * ✅ FIX: Added @Builder, @Data, @AllArgsConstructor to match Kuppi request DTO pattern.
 * ✅ FIX: Added @Size constraints on every field — Kuppi validates max length on all strings.
 * ✅ FIX: Separated @Getter/@Setter into @Data (includes equals, hashCode, toString too).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLostItemRequest {

    // Title of the lost item — short, human-readable name
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    // Detailed description: color, brand, distinguishing features, etc.
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Category name (e.g. "Electronics", "Clothing") — resolved to ItemCategory entity in service
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    // Where the item was last seen (building name, floor, etc.)
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    // Phone number or email through which the owner can be contacted
    @NotBlank(message = "Contact number is required")
    @Size(max = 100, message = "Contact number must not exceed 100 characters")
    private String contactNumber;
}