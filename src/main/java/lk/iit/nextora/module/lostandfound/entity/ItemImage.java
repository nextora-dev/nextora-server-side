package lk.iit.nextora.module.lostandfound.entity;

// ── JPA annotations ─────────────────────────────────────────────────────────
import jakarta.persistence.*;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a photo attached to a lost or found item.
 * An image belongs to either a LostItem OR a FoundItem — not both simultaneously.
 * The non-owning side will be null for any given image row.
 *
 * ✅ FIX: Added @Table(name = "item_images") — explicit table name.
 * ✅ FIX: Added @Builder, @AllArgsConstructor to match Kuppi entity pattern.
 * ✅ FIX: Added @Column constraints on imageUrl.
 * ✅ FIX: Added FetchType.LAZY on both @ManyToOne — avoids loading entire
 *         LostItem/FoundItem graphs when only the image URL is needed.
 * ✅ FIX: Added displayOrder so images can be sorted (first image = thumbnail).
 */
@Entity
@Table(name = "item_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemImage {

    // Auto-generated surrogate primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full URL of the image stored in S3 (or other object storage)
    @Column(nullable = false, length = 500)
    private String imageUrl;

    // ✅ FIX: changed to FetchType.LAZY — Kuppi uses lazy on all @ManyToOne
    // Non-null when this image belongs to a LostItem; null when it belongs to a FoundItem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id")
    private LostItem lostItem;

    // Non-null when this image belongs to a FoundItem; null when it belongs to a LostItem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id")
    private FoundItem foundItem;

    // ✅ FIX: added displayOrder — the original had no way to control which image
    //         appears first (used as the thumbnail in list views)
    // Zero-based ordering index for display; 0 = primary / thumbnail image
    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}