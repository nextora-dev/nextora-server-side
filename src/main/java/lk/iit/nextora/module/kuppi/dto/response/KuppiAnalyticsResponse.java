package lk.iit.nextora.module.kuppi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Kuppi analytics (for Kuppi Students to view their stats)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiAnalyticsResponse {

    // Session stats
    private Long totalSessions;
    private Long completedSessions;
    private Long upcomingSessions;
    private Long totalSessionViews;

    // Note stats
    private Long totalNotes;
    private Long totalNoteViews;

    // Most viewed session
    private Long mostViewedSessionId;
    private String mostViewedSessionTitle;
    private Long mostViewedSessionViews;

    // Most downloaded note
    private Long mostDownloadedNoteId;
    private String mostDownloadedNoteTitle;
    private Long mostDownloadedNoteDownloads;
}

