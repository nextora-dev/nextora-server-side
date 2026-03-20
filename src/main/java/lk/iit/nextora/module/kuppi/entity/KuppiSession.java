package lk.iit.nextora.module.kuppi.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.enums.KuppiSessionType;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Kuppi session (tutoring session)
 * Users click on liveLink to go to external platform (Google Meet, Zoom, etc.)
 */
@Entity
@Table(name = "kuppi_sessions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiSession extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 100)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KuppiSessionType sessionType = KuppiSessionType.LIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KuppiSessionStatus status = KuppiSessionStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Student host;

    @Column(nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column(nullable = false)
    private LocalDateTime scheduledEndTime;

    @Column(nullable = false, length = 500)
    private String liveLink;

    @Column(length = 200)
    private String meetingPlatform;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(length = 500)
    private String cancellationReason;

    @Column
    private LocalDateTime cancelledAt;

    // File metadata for sessions (optional)
    @Column(length = 500)
    private String fileUrl;

    @Column(length = 100)
    private String fileName;

    @Column
    private Long fileSize;

    @Column(length = 50)
    private String fileType;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<KuppiNote> notes = new HashSet<>();

    public boolean isJoinable() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(scheduledEndTime) &&
               (status == KuppiSessionStatus.SCHEDULED || status == KuppiSessionStatus.LIVE);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
