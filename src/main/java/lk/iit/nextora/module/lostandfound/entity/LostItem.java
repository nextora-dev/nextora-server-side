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
 * JPA entity representing an item that has been reported as lost.
 *
 * ✅ FIX: Added @Table(name = "lost_items") — explicit table name, Kuppi uses named tables.
 * ✅ FIX: Added createdAt + updatedAt fields — were completely missing (FoundItem had them).
 *         LostItemMapper was trying to set .createdAt() but LostItem had no such field.
 * ✅ FIX: Added @PrePersist + @PreUpdate lifecycle hooks — same pattern as FoundItem.
 * ✅ FIX: Added @Builder, @AllArgsConstructor to match Kuppi entity conventions.
 * ✅ FIX: Added @Column constraints (nullable, length) matching validation in the request DTO.
 * ✅ FIX: Added @Builder.Default on 'active' so builder doesn't default it to false.
 */
@Entity
// ✅ FIX: explicit table name avoids Hibernate guessing / conflicts with reserved words
@Table(name = "lost_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostItem {

    // Auto-generated surrogate primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short descriptive name of the lost item — required, max 200 chars (matches request DTO)
    @Column(nullable = false, length = 200)
    private String title;

    // Optional detailed description with identifying features
    @Column(length = 1000)
    private String description;

    // Location where the item was last seen
    @Column(length = 300)
    private String location;

    // Contact number or email of the person who lost the item
    @Column(nullable = false, length = 100)
    private String contactNumber;

    // Whether this listing is still active (false = item was found / closed)
    // ✅ FIX: @Builder.Default needed so that builder().build() sets active=true not false
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // Lazy-loaded many-to-one relationship to ItemCategory
    // FetchType.LAZY avoids loading the category for every LostItem query
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ItemCategory category;

    // ✅ FIX: createdAt was MISSING — LostItemMapper called .createdAt() which caused
    //         a compile error. FoundItem had this field but LostItem did not.
    // Automatically set by @PrePersist when the entity is first saved
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ FIX: updatedAt was also missing — Kuppi entities track both timestamps
    // Automatically updated by @PreUpdate on every save after creation
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle hook — called once before the entity is first inserted.
     * Sets both createdAt and updatedAt to the current moment.
     */
    @PrePersist
    protected void onCreate() {
        // Set creation and last-update time to now on first insert
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle hook — called before every subsequent update to the entity.
     * Keeps updatedAt current without requiring manual assignment in service code.
     */
    @PreUpdate
    protected void onUpdate() {
        // Refresh updatedAt so callers always see the true last-modified time
        this.updatedAt = LocalDateTime.now();
    }
}