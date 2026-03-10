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
 * JPA entity storing a computed similarity match between a LostItem and a FoundItem.
 * Records are created by ItemMatchingService when it detects likely matches.
 *
 * ✅ FIX: Added @Table(name = "item_matches") — explicit table name.
 * ✅ FIX: Added @Builder, @AllArgsConstructor to match Kuppi entity pattern.
 * ✅ FIX: Added @Column constraints on score.
 * ✅ FIX: Added FetchType.LAZY on both @ManyToOne relationships.
 * ✅ FIX: Added matchReason — the original entity stored only a raw score number with
 *         no explanation; the new field holds a human-readable match summary.
 * ✅ FIX: Added createdAt — needed to show when the match was detected.
 */
@Entity
@Table(name = "item_matches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemMatch {

    // Auto-generated surrogate primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The lost item on one side of this match
    // ✅ FIX: changed to FetchType.LAZY — avoids loading full LostItem graph
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    // The found item on the other side of this match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    // Similarity score from 0.0 (no match) to 1.0 (perfect match)
    @Column(nullable = false)
    private double score;

    // ✅ FIX: matchReason was completely absent — now stores a human-readable explanation
    //         of why this pair was matched (e.g. "Title match 100%, same category")
    @Column(length = 500)
    private String matchReason;

    // ✅ FIX: createdAt was missing — needed to know when the match was first computed
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Set the creation timestamp automatically when the match record is first saved.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}