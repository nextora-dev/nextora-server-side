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

@Entity
@Table(name = "found_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 300)
    private String location;

    @Column(nullable = false, length = 100)
    private String contactNumber;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(name = "reporter_name", length = 200)
    private String reporterName;

    @Column(name = "date_found")
    private LocalDateTime dateFound;

    @Column(name = "pickup_location", length = 300)
    private String pickupLocation;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ItemCategory category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}