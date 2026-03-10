package lk.iit.nextora.module.lostandfound.dto.request;

// ── Jakarta validation constraints ──────────────────────────────────────────
import jakarta.validation.constraints.Size;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for partially updating a lost or found item.
 * All fields are optional — only non-null fields are applied by the service
 * (null-safe partial update, same pattern as Kuppi's UpdateKuppiNoteRequest).
 *
 * ✅ FIX: Added @Builder, @Data, @AllArgsConstructor to match Kuppi DTO pattern.
 * ✅ FIX: Added @Size constraints — all string fields now validated for max length.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    // New title — leave null to keep the existing title unchanged
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    // New description — leave null to keep existing description
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    // New last-seen / found location — leave null to keep existing location
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    // Updated contact number — leave null to keep existing contact number
    @Size(max = 100, message = "Contact number must not exceed 100 characters")
    private String contactNumber;

    // Set to false to mark the item as resolved / no longer searchable
    // Use Boolean (wrapper) so null means "don't change the active flag"
    private Boolean active;
}