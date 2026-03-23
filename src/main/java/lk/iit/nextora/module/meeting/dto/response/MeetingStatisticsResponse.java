package lk.iit.nextora.module.meeting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for meeting statistics.
 * Provides comprehensive analytics for lecturers and administrators.
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingStatisticsResponse {

    // ==================== Request Statistics ====================

    /**
     * Total number of meeting requests
     */
    private Long totalRequests;

    /**
     * Number of pending requests awaiting response
     */
    private Long pendingRequests;

    /**
     * Number of accepted/scheduled meetings
     */
    private Long scheduledMeetings;

    /**
     * Number of completed meetings
     */
    private Long completedMeetings;

    /**
     * Number of rejected requests
     */
    private Long rejectedRequests;

    /**
     * Number of cancelled meetings
     */
    private Long cancelledMeetings;

    /**
     * Number of rescheduled meetings
     */
    private Long rescheduledMeetings;

    // ==================== Time Statistics ====================

    /**
     * Average response time in hours (time from request to accept/reject)
     */
    private Double averageResponseTimeHours;

    /**
     * Average meeting duration in minutes
     */
    private Double averageMeetingDurationMinutes;

    /**
     * Total meeting hours conducted
     */
    private Double totalMeetingHours;

    // ==================== Meeting Type Distribution ====================

    private Long academicGuidanceMeetings;
    private Long projectDiscussionMeetings;
    private Long careerCounselingMeetings;
    private Long personalConsultationMeetings;
    private Long researchDiscussionMeetings;
    private Long otherMeetings;

    // ==================== Priority Distribution ====================

    /**
     * Number of low priority meetings
     */
    private Long lowPriorityMeetings;

    /**
     * Number of normal priority meetings
     */
    private Long normalPriorityMeetings;

    /**
     * Number of high priority meetings
     */
    private Long highPriorityMeetings;

    /**
     * Number of urgent priority meetings
     */
    private Long urgentPriorityMeetings;

    // ==================== Location Statistics ====================

    /**
     * Number of online meetings
     */
    private Long onlineMeetings;

    /**
     * Number of in-person meetings
     */
    private Long inPersonMeetings;

    // ==================== Feedback Statistics ====================

    /**
     * Average student rating (1-5)
     */
    private Double averageRating;

    /**
     * Number of meetings with feedback
     */
    private Long meetingsWithFeedback;

    /**
     * Rating distribution - 5 stars
     */
    private Long fiveStarRatings;

    /**
     * Rating distribution - 4 stars
     */
    private Long fourStarRatings;

    /**
     * Rating distribution - 3 stars
     */
    private Long threeStarRatings;

    /**
     * Rating distribution - 2 stars
     */
    private Long twoStarRatings;

    /**
     * Rating distribution - 1 star
     */
    private Long oneStarRatings;

    // ==================== Rates ====================

    /**
     * Acceptance rate (accepted / total * 100)
     */
    private Double acceptanceRate;

    /**
     * Completion rate (completed / scheduled * 100)
     */
    private Double completionRate;

    /**
     * Cancellation rate (cancelled / total * 100)
     */
    private Double cancellationRate;

    // ==================== Period Statistics ====================

    /**
     * Number of meetings this week
     */
    private Long meetingsThisWeek;

    /**
     * Number of meetings this month
     */
    private Long meetingsThisMonth;

    // ==================== Follow-up Statistics ====================

    /**
     * Number of meetings requiring follow-up
     */
    private Long meetingsRequiringFollowUp;
}
