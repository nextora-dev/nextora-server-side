package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a schedule event belonging to a {@link ScheduleCategory}.
 */
@Entity
@Table(name = "intranet_schedule_events")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_category_id", nullable = false)
    private ScheduleCategory scheduleCategory;

    @Column(name = "event_name", nullable = false, length = 300)
    private String eventName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", length = 20)
    private String startDate;

    @Column(name = "end_date", length = 20)
    private String endDate;

    @Column(name = "venue", length = 300)
    private String venue;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "is_event_active")
    @Builder.Default
    private Boolean isEventActive = true;
}
