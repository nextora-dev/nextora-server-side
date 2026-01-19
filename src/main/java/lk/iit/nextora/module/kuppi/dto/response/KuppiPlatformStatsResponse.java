package lk.iit.nextora.module.kuppi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for platform-wide Kuppi statistics (for Admin/Super Admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiPlatformStatsResponse {

    // Overall stats
    private Long totalSessions;
    private Long totalNotes;
    private Long totalParticipants;
    private Long totalKuppiStudents;

    // Status breakdown
    private Long completedSessions;
    private Long cancelledSessions;

    // Activity stats
    private Long totalViews;
    private Long totalDownloads;
    private Double averagePlatformRating;

    // Time-based stats
    private Long sessionsThisWeek;
    private Long sessionsThisMonth;
    private Long newKuppiStudentsThisMonth;
}

