package lk.iit.nextora.module.lostandfound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA entity representing a student's claim that a FoundItem belongs to them.
 * A claim links a LostItem to a FoundItem and tracks the admin review lifecycle:
 * PENDING → APPROVED or REJECTED.
 */
@Entity
@Table(name = "claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    // ID of the student who submitted this claim
    @Column(name = "claimant_id", nullable = false)
    private Long claimantId;

    // Name of the claimant captured at submission time
    @Column(name = "claimant_name", length = 200)
    private String claimantName;

    // Optional proof text provided by the claimant to support ownership
    @Column(name = "proof_description", length = 1000)
    private String proofDescription;

    // Lifecycle status: PENDING on creation; admin sets APPROVED or REJECTED
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    // Reason provided by the admin when rejecting a claim
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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