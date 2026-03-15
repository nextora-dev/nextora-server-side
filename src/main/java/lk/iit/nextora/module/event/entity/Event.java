package lk.iit.nextora.module.event.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing an Event
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class Event extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(length = 300)
    private String location;

    @Column(length = 300)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EventType eventType = EventType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private BaseUser createdBy;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column
    private Integer maxAttendees;

    @Column(length = 500)
    private String registrationLink;

    @Column(length = 500)
    private String cancellationReason;

    @Column
    private LocalDateTime cancelledAt;

    /* -------- CORE BUSINESS RULES -------- */

    public boolean hasValidTimeRange() {
        return startAt.isBefore(endAt);
    }

    public boolean canEdit() {
        return status == EventStatus.DRAFT;
    }

    public boolean isVisible() {
        return status == EventStatus.PUBLISHED;
    }

    public boolean isUpcoming() {
        return startAt.isAfter(LocalDateTime.now()) && status == EventStatus.PUBLISHED;
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt) && status == EventStatus.PUBLISHED;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
