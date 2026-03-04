package lk.iit.nextora.module.club.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity for auditing all significant club actions.
 * Provides a complete audit trail for compliance and transparency.
 */
@Entity
@Table(name = "club_activity_logs", indexes = {
        @Index(name = "idx_activity_club", columnList = "club_id"),
        @Index(name = "idx_activity_type", columnList = "activityType"),
        @Index(name = "idx_activity_user", columnList = "performedByUserId")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClubActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Long performedByUserId;

    @Column(length = 100)
    private String performedByName;

    @Column
    private Long targetUserId;

    @Column(length = 100)
    private String targetUserName;

    @Column
    private Long relatedEntityId;

    @Column(length = 50)
    private String relatedEntityType;

    @Column(length = 2000)
    private String metadata;

    @Getter
    public enum ActivityType {
        // Club lifecycle
        CLUB_CREATED("Club Created"),
        CLUB_UPDATED("Club Updated"),
        CLUB_DELETED("Club Deleted"),
        CLUB_REGISTRATION_OPENED("Registration Opened"),
        CLUB_REGISTRATION_CLOSED("Registration Closed"),

        // Membership actions
        MEMBER_JOINED("Member Joined"),
        MEMBER_APPROVED("Member Approved"),
        MEMBER_REJECTED("Member Rejected"),
        MEMBER_SUSPENDED("Member Suspended"),
        MEMBER_LEFT("Member Left"),
        MEMBER_POSITION_CHANGED("Position Changed"),
        MEMBER_REVOKED("Membership Revoked"),


        // Announcement actions
        ANNOUNCEMENT_POSTED("Announcement Posted"),
        ANNOUNCEMENT_UPDATED("Announcement Updated"),
        ANNOUNCEMENT_DELETED("Announcement Deleted"),
        ANNOUNCEMENT_PINNED("Announcement Pinned"),

        // Election-related
        ELECTION_CREATED("Election Created"),
        ELECTION_NOMINATIONS_OPENED("Nominations Opened"),
        ELECTION_NOMINATIONS_CLOSED("Nominations Closed"),
        ELECTION_VOTING_OPENED("Voting Opened"),
        ELECTION_VOTING_CLOSED("Voting Closed"),
        ELECTION_RESULTS_PUBLISHED("Results Published"),
        ELECTION_CANCELLED("Election Cancelled"),
        ELECTION_PRESIDENT_AUTO_UPDATED("President Auto-Updated from Election Results"),

        // Admin actions
        BULK_MEMBER_APPROVED("Bulk Members Approved"),
        ADMIN_OVERRIDE("Admin Override");

        private final String displayName;

        ActivityType(String displayName) {
            this.displayName = displayName;
        }
    }
}

