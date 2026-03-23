package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an academic calendar for a partner university.
 */
@Entity
@Table(name = "intranet_academic_calendars", uniqueConstraints = {
        @UniqueConstraint(columnNames = "university_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicCalendar extends BaseEntity {

    @Column(name = "university_name", nullable = false, length = 300)
    private String universityName;

    @Column(name = "university_slug", nullable = false, unique = true, length = 200)
    private String universitySlug;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "calendar_file_url", length = 500)
    private String calendarFileUrl;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("startDate ASC")
    private List<CalendarEvent> events = new ArrayList<>();

    // Helper
    public void addEvent(CalendarEvent event) {
        events.add(event);
        event.setCalendar(this);
    }
}

