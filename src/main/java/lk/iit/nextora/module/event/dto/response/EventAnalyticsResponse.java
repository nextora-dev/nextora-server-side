package lk.iit.nextora.module.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Event analytics (for event creators to view their stats)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAnalyticsResponse {

    // Event stats
    private Long totalEvents;
    private Long publishedEvents;
    private Long upcomingEvents;
    private Long completedEvents;
    private Long cancelledEvents;
    private Long totalViews;

    // Most viewed event
    private Long mostViewedEventId;
    private String mostViewedEventTitle;
    private Long mostViewedEventViews;
}
