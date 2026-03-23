package lk.iit.nextora.module.meeting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.enums.MeetingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for meeting details.
 * Contains all meeting information for both students and lecturers.
 *
 * <p>Uses @JsonInclude to omit null values for cleaner API responses.</p>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingResponse {

    private Long id;

    // ==================== Request Details ====================

    private String subject;
    private MeetingType meetingType;
    private String meetingTypeDisplayName;
    private String description;
    private LocalDateTime preferredDateTime;
    private Integer preferredDurationMinutes;
    private Integer priority;
    private String priorityDisplayName;

    // ==================== Status ====================

    private MeetingStatus status;
    private String statusDisplayName;

    // ==================== Student Information ====================

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentBatch;
    private String studentProgram;

    // ==================== Lecturer Information ====================

    private Long lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    private String lecturerDepartment;
    private String lecturerDesignation;
    private String lecturerOfficeLocation;
    private String lecturerProfileImageUrl;

    // ==================== Schedule Details (Set by Lecturer) ====================

    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private Integer durationMinutes;
    private Boolean isOnline;
    private String meetingLink;
    private String meetingPlatform;
    private String location;

    // ==================== Response Details ====================

    private String lecturerResponse;
    private LocalDateTime respondedAt;
    private Long responseTimeHours;

    // ==================== Cancellation Details ====================

    private String cancellationReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;

    // ==================== Completion Details ====================

    private String meetingNotes;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer actualDurationMinutes;
    private Boolean followUpRequired;
    private String followUpNotes;

    // ==================== Feedback Details ====================

    private Integer studentRating;
    private String studentFeedback;
    private LocalDateTime feedbackSubmittedAt;

    // ==================== Recurring Meeting ====================

    private Long parentMeetingId;
    private String recurrencePattern;
    private LocalDateTime recurrenceEndDate;
    private Boolean isRecurring;

    // ==================== Attachment ====================

    private String attachmentUrl;
    private String attachmentName;

    // ==================== Metadata ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Computed Status Flags ====================

    /**
     * Whether the meeting can be joined now (within time window)
     */
    private Boolean canJoin;

    /**
     * Whether the meeting can be cancelled
     */
    private Boolean canCancel;

    /**
     * Whether the meeting can be rescheduled
     */
    private Boolean canReschedule;

    /**
     * Whether feedback can be submitted
     */
    private Boolean canSubmitFeedback;

    /**
     * Whether this is a high priority meeting
     */
    private Boolean isHighPriority;
}
