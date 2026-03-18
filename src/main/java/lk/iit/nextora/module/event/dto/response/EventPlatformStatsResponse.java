package lk.iit.nextora.module.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for platform-wide Event statistics (for Admin/Super Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPlatformStatsResponse {

    // Overall stats
    private Long totalEvents;
    private Long totalCreators;

    // Status breakdown
    private Long publishedEvents;
    private Long cancelledEvents;
    private Long completedEvents;
    private Long draftEvents;

    // Activity stats
    private Long totalViews;
    private Long totalRegistrations;

    // Time-based stats
    private Long eventsThisWeek;
    private Long eventsThisMonth;
    private Long upcomingEvents;
    private Long newCreatorsThisMonth;
}
