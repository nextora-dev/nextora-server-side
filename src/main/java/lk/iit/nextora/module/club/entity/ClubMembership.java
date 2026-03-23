package lk.iit.nextora.module.club.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a student's membership in a club.
 * Only active members can participate in club elections.
 */
@Entity
@Table(name = "club_memberships",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_club_member", columnNames = {"club_id", "member_id"})
       },
       indexes = {
           @Index(name = "idx_membership_club", columnList = "club_id"),
           @Index(name = "idx_membership_member", columnList = "member_id"),
           @Index(name = "idx_membership_status", columnList = "status")
       })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Student member;

    @Column(unique = true, length = 30)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClubMembershipStatus status = ClubMembershipStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ClubPositionsType position;

    @Column(nullable = false)
    private LocalDate joinDate;

    @Column
    private LocalDate expiryDate;

    @Column
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Student approvedBy;

    @Column(length = 500)
    private String remarks;

    /**
     * Check if membership is valid for voting
     */
    public boolean canVote() {
        return status == ClubMembershipStatus.ACTIVE &&
               (expiryDate == null || expiryDate.isAfter(LocalDate.now()));
    }

    /**
     * Check if member can nominate as candidate
     */
    public boolean canNominate() {
        return canVote() &&
               joinDate.plusMonths(3).isBefore(LocalDate.now()); // Must be member for at least 3 months
    }

    /**
     * Approve membership
     */
    public void approve(Student approver) {
        this.status = ClubMembershipStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approver;
    }

    /**
     * Suspend membership
     */
    public void suspend(String reason) {
        this.status = ClubMembershipStatus.SUSPENDED;
        this.remarks = reason;
    }

    /**
     * Generate membership number
     */
    public static String generateMembershipNumber(String clubCode, Long memberId) {
        return String.format("%s-%06d", clubCode, memberId);
    }
}
