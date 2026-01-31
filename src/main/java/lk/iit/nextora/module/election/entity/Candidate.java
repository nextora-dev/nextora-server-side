package lk.iit.nextora.module.election.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.CandidateStatus;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a candidate in an election.
 * A student can nominate themselves or be nominated by others (based on election settings).
 */
@Entity
@Table(name = "candidates",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_election_candidate", columnNames = {"election_id", "student_id"})
       },
       indexes = {
           @Index(name = "idx_candidate_election", columnList = "election_id"),
           @Index(name = "idx_candidate_student", columnList = "student_id"),
           @Index(name = "idx_candidate_status", columnList = "status")
       })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(length = 3000)
    private String manifesto;

    @Column(length = 500)
    private String slogan;

    @Column(length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.PENDING;

    @Column
    private LocalDateTime nominatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nominated_by_id")
    private Student nominatedBy;

    @Column
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private BaseUser reviewedBy;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 1000)
    private String qualifications;

    @Column(length = 500)
    private String previousExperience;

    @Column(nullable = false)
    @Builder.Default
    private Integer voteCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    // ==================== Business Logic Methods ====================

    /**
     * Check if candidate can receive votes
     */
    public boolean canReceiveVotes() {
        return status == CandidateStatus.APPROVED && election.isVotingOpen();
    }

    /**
     * Approve candidate
     */
    public void approve(BaseUser reviewer) {
        this.status = CandidateStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewer;
    }

    /**
     * Reject candidate
     */
    public void reject(BaseUser reviewer, String reason) {
        this.status = CandidateStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewer;
        this.rejectionReason = reason;
    }

    /**
     * Withdraw candidacy
     */
    public void withdraw() {
        this.status = CandidateStatus.WITHDRAWN;
    }

    /**
     * Disqualify candidate
     */
    public void disqualify(BaseUser reviewer, String reason) {
        this.status = CandidateStatus.DISQUALIFIED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewer;
        this.rejectionReason = reason;
    }

    /**
     * Increment vote count
     */
    public void incrementVoteCount() {
        this.voteCount++;
    }

    /**
     * Get candidate display name
     */
    public String getCandidateName() {
        return student != null ? student.getFullName() : "Unknown";
    }
}
