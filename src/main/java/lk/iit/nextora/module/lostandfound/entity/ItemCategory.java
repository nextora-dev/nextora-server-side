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
 * JPA entity representing a category for lost and found items.
 * Example categories: Electronics, Clothing, Documents, Keys, Accessories.
 *
 * ✅ FIX: Added @Table(name = "item_categories") — explicit table name.
 * ✅ FIX: Added @Builder, @AllArgsConstructor to match Kuppi entity pattern.
 * ✅ FIX: Added @Column(nullable = false, unique = true) on name — category names
 *         must be unique to prevent duplicates like "Electronics" and "electronics".
 */
@Entity
@Table(name = "item_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {

    // Auto-generated surrogate primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ FIX: added nullable=false and unique=true — category names must be unique
    //         and non-empty so the service can reliably look them up by name
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}