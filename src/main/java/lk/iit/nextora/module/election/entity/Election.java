package lk.iit.nextora.module.election.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.enums.ElectionType;
import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import lk.iit.nextora.module.club.entity.Club;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an election within a club.
 * Elections go through various states: DRAFT -> NOMINATION_OPEN -> NOMINATION_CLOSED -> VOTING_OPEN -> VOTING_CLOSED -> RESULTS_PUBLISHED
 */
@Entity
@Table(name = "elections", indexes = {
        @Index(name = "idx_election_club", columnList = "club_id"),
        @Index(name = "idx_election_status", columnList = "status"),
        @Index(name = "idx_election_type", columnList = "electionType"),
        @Index(name = "idx_election_voting_start", columnList = "votingStartTime"),
        @Index(name = "idx_election_voting_end", columnList = "votingEndTime")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Election extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ElectionType electionType = ElectionType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ElectionStatus status = ElectionStatus.DRAFT;

    @Column(nullable = false)
    private LocalDateTime nominationStartTime;

    @Column(nullable = false)
    private LocalDateTime nominationEndTime;

    @Column(nullable = false)
    private LocalDateTime votingStartTime;

    @Column(nullable = false)
    private LocalDateTime votingEndTime;

    @Column
    private LocalDateTime resultsPublishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private NonAcademicStaff createdBy;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxCandidates = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer winnersCount = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAnonymousVoting = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requireManifesto = false;

    @Column(length = 1000)
    private String eligibilityCriteria;

    @Column(length = 500)
    private String cancellationReason;

    @Column
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Candidate> candidates = new HashSet<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Vote> votes = new HashSet<>();

    // ==================== Business Logic Methods ====================

    /**
     * Check if nominations are currently open.
     * Nominations are open if status is NOMINATION_OPEN.
     * Time-based check is optional - if manually opened, nominations are accepted.
     */
    public boolean isNominationOpen() {
        return status == ElectionStatus.NOMINATION_OPEN;
    }

    /**
     * Check if nominations are within the scheduled time window
     */
    public boolean isWithinNominationPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(nominationStartTime) && now.isBefore(nominationEndTime);
    }

    /**
     * Check if voting is currently open.
     * Voting is open if status is VOTING_OPEN.
     */
    public boolean isVotingOpen() {
        return status == ElectionStatus.VOTING_OPEN;
    }

    /**
     * Check if voting is within the scheduled time window
     */
    public boolean isWithinVotingPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(votingStartTime) && now.isBefore(votingEndTime);
    }

    /**
     * Check if results can be viewed
     */
    public boolean canViewResults() {
        return status == ElectionStatus.RESULTS_PUBLISHED || status == ElectionStatus.ARCHIVED;
    }

    /**
     * Check if election can be modified
     */
    public boolean canModify() {
        return status == ElectionStatus.DRAFT || status == ElectionStatus.NOMINATION_OPEN;
    }

    /**
     * Get total vote count
     */
    public int getTotalVotes() {
        return votes != null ? votes.size() : 0;
    }

    /**
     * Get approved candidates count
     */
    public long getApprovedCandidatesCount() {
        return candidates != null ?
               candidates.stream()
                   .filter(c -> c.getStatus() == lk.iit.nextora.common.enums.CandidateStatus.APPROVED)
                   .count() : 0;
    }

    /**
     * Open nominations
     */
    public void openNominations() {
        this.status = ElectionStatus.NOMINATION_OPEN;
    }

    /**
     * Close nominations
     */
    public void closeNominations() {
        this.status = ElectionStatus.NOMINATION_CLOSED;
    }

    /**
     * Open voting
     */
    public void openVoting() {
        this.status = ElectionStatus.VOTING_OPEN;
    }

    /**
     * Close voting
     */
    public void closeVoting() {
        this.status = ElectionStatus.VOTING_CLOSED;
    }

    /**
     * Publish results
     */
    public void publishResults() {
        this.status = ElectionStatus.RESULTS_PUBLISHED;
        this.resultsPublishedAt = LocalDateTime.now();
    }

    /**
     * Cancel election
     */
    public void cancel(String reason) {
        this.status = ElectionStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
}
