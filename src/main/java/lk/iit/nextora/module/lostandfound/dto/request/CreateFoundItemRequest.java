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
 * Request DTO for reporting a new found item.
 * Used by any authenticated student who has found an item on campus.
 *
 * ✅ FIX: Added @Builder, @Data, @AllArgsConstructor to match Kuppi request DTO pattern.
 * ✅ FIX: Added @Size constraints on all string fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFoundItemRequest {

    // Short name / title of what was found (e.g. "Black wallet", "iPhone 14")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    // Optional additional details about the item's appearance, condition, etc.
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // Category name — resolved to the ItemCategory entity inside the service layer
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    // Where the item was found (building, room, campus area)
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    // Contact info for the person who found the item — so the owner can reach them
    @NotBlank(message = "Contact number is required")
    @Size(max = 100, message = "Contact number must not exceed 100 characters")
    private String contactNumber;
}