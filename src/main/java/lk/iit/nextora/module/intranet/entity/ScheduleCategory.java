package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a schedule category (orientation, temporary, assessments, annual-events).
 * Contains a bidirectional one-to-many relationship with {@link ScheduleEvent}.
 */
@Entity
@Table(name = "intranet_schedule_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCategory extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "scheduleCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("startDate ASC")
    @Builder.Default
    private List<ScheduleEvent> events = new ArrayList<>();

    public void addEvent(ScheduleEvent event) {
        events.add(event);
        event.setScheduleCategory(this);
    }
}

