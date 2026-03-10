package lk.iit.nextora.module.lostandfound.entity;

// ── JPA annotations ─────────────────────────────────────────────────────────
import jakarta.persistence.*;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ── Java time ───────────────────────────────────────────────────────────────
import java.time.LocalDateTime;

/**
 * JPA entity representing an item that has been reported as found.
 *
 * ✅ FIX: Added explicit @Table(name = "found_items") to match Kuppi naming pattern.
 * ✅ FIX: Added @Builder, @AllArgsConstructor — required for builder pattern.
 * ✅ FIX: Added @Builder.Default on 'active' — without it, builder().build() sets active=false.
 * ✅ FIX: Added @Column constraints (nullable, length) to match the request DTO validations.
 * ✅ FIX: Added @PreUpdate + updatedAt — the original only had @PrePersist and createdAt.
 * ✅ FIX: Added FetchType.LAZY on category — eager loading causes N+1 on list queries.
 */
@Entity
@Table(name = "found_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoundItem {

    // Auto-generated surrogate primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short descriptive name of the found item — required
    @Column(nullable = false, length = 200)
    private String title;

    // Optional detailed description with identifying features
    @Column(length = 1000)
    private String description;

    // Location where the item was found
    @Column(length = 300)
    private String location;

    // Contact info of the person who found the item
    @Column(nullable = false, length = 100)
    private String contactNumber;

    // Whether this listing is still active (false = item has been claimed / closed)
    // ✅ FIX: @Builder.Default ensures builder sets this to true, not the Java default false
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ✅ FIX: changed to FetchType.LAZY — avoids loading the full category on every
    //         FoundItem query; only fetched when explicitly needed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ItemCategory category;

    // Set once when the entity is first persisted — never updated after that
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ FIX: updatedAt was missing — every Kuppi entity tracks both timestamps
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Set both timestamps on first insert.
     * Called automatically by JPA before the entity is saved for the first time.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * ✅ FIX: @PreUpdate was missing — updatedAt was never refreshed on subsequent saves.
     * Keeps updatedAt accurate without requiring service code to set it manually.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}