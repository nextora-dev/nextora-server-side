package lk.iit.nextora.module.club.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a club announcement / news post.
 * Club officers can post announcements visible to all members or publicly.
 */
@Entity
@Table(name = "club_announcements", indexes = {
        @Index(name = "idx_announcement_club", columnList = "club_id"),
        @Index(name = "idx_announcement_priority", columnList = "priority"),
        @Index(name = "idx_announcement_pinned", columnList = "isPinned")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClubAnnouncement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Student author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isMembersOnly = false;

    @Column(length = 500)
    private String attachmentUrl;

    @Column(length = 200)
    private String attachmentName;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    public void incrementViewCount() {
        this.viewCount++;
    }

    @Getter
    public enum AnnouncementPriority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        AnnouncementPriority(String displayName) {
            this.displayName = displayName;
        }
    }
}

