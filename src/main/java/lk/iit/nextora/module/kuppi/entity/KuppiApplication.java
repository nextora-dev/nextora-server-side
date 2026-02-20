package lk.iit.nextora.module.kuppi.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "kuppi_applications", indexes = {
        @Index(name = "idx_kuppi_app_student", columnList = "student_id"),
        @Index(name = "idx_kuppi_app_status", columnList = "status"),
        @Index(name = "idx_kuppi_app_created", columnList = "created_at")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KuppiApplicationStatus status = KuppiApplicationStatus.PENDING;

    // ==================== Application Details ====================

    @Column(nullable = false, length = 1000)
    private String motivation;

    @Column(length = 500)
    private String relevantExperience;

    @ElementCollection
    @CollectionTable(name = "kuppi_application_subjects", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "subject", length = 100)
    @Builder.Default
    private Set<String> subjectsToTeach = new HashSet<>();

    @Column(length = 20)
    private String preferredExperienceLevel;

    @Column(length = 500)
    private String availability;

    @Column(nullable = false)
    @Builder.Default
    private Double currentGpa = 0.0;

    @Column(length = 50)
    private String currentSemester;

    // ==================== Review Details ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private BaseUser reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(length = 1000)
    private String reviewNotes;

    @Column(length = 500)
    private String rejectionReason;

    // ==================== Timestamps ====================

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ==================== Helper Methods ====================

    /**
     * Check if application can be approved
     */
    public boolean canBeApproved() {
        return status.canBeApproved();
    }

    /**
     * Check if application can be rejected
     */
    public boolean canBeRejected() {
        return status.canBeRejected();
    }

    /**
     * Check if application can be cancelled by student
     */
    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    /**
     * Check if application is in final state
     */
    public boolean isFinalState() {
        return status.isFinalState();
    }

    /**
     * Approve the application
     */
    public void approve(BaseUser reviewer, String notes) {
        this.status = KuppiApplicationStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.approvedAt = LocalDateTime.now();
        this.reviewNotes = notes;
    }

    /**
     * Reject the application
     */
    public void reject(BaseUser reviewer, String reason, String notes) {
        this.status = KuppiApplicationStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.reviewNotes = notes;
    }

    /**
     * Cancel the application (by student)
     */
    public void cancel() {
        this.status = KuppiApplicationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Mark as under review
     */
    public void markUnderReview(BaseUser reviewer) {
        this.status = KuppiApplicationStatus.UNDER_REVIEW;
        this.reviewedBy = reviewer;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}

