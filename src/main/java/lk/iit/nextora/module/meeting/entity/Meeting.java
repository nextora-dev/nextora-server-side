package lk.iit.nextora.module.meeting.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.enums.MeetingType;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity representing a meeting request between a Student and a Lecturer (Academic Staff).
 *
 * <h3>MEETING WORKFLOW:</h3>
 * <pre>
 * ┌─────────────┐     Request      ┌─────────────┐
 * │   STUDENT   │ ───────────────► │   PENDING   │
 * └─────────────┘                  └──────┬──────┘
 *                                         │
 *                            ┌────────────┴────────────┐
 *                            ▼                         ▼
 *                     ┌────────────┐           ┌────────────┐
 *                     │  ACCEPTED  │           │  REJECTED  │
 *                     └─────┬──────┘           └────────────┘
 *                           │
 *                           ▼ (Lecturer sets time/location)
 *                     ┌────────────┐
 *                     │ SCHEDULED  │◄─────┐
 *                     └─────┬──────┘      │ Reschedule
 *                           │             │
 *                           ▼             │
 *                     ┌────────────┐──────┘
 *                     │IN_PROGRESS │
 *                     └─────┬──────┘
 *                           │
 *                           ▼
 *                     ┌────────────┐
 *                     │ COMPLETED  │
 *                     └────────────┘
 * </pre>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Student initiates meeting request with preferred time</li>
 *   <li>Lecturer accepts/rejects with schedule and location</li>
 *   <li>Automatic notifications to student on status changes</li>
 *   <li>Calendar integration for lecturer's schedule</li>
 *   <li>Support for both online (Zoom, Meet) and in-person meetings</li>
 *   <li>Meeting feedback/rating system</li>
 *   <li>Priority levels for urgent requests</li>
 *   <li>Recurring meeting support</li>
 * </ul>
 *
 * @author Nextora Development Team
 * @version 2.0
 * @since 1.0
 */
@Entity
@Table(name = "meetings", indexes = {
        @Index(name = "idx_meeting_student", columnList = "student_id"),
        @Index(name = "idx_meeting_lecturer", columnList = "lecturer_id"),
        @Index(name = "idx_meeting_status", columnList = "status"),
        @Index(name = "idx_meeting_scheduled_time", columnList = "scheduled_start_time"),
        @Index(name = "idx_meeting_priority", columnList = "priority"),
        @Index(name = "idx_meeting_created", columnList = "created_at")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Meeting extends BaseEntity {

    // ==================== REQUEST DETAILS (Set by Student) ====================

    /**
     * The student who is requesting the meeting
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * The lecturer (academic staff) the student wants to meet
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private AcademicStaff lecturer;

    /**
     * Subject/title of the meeting - brief description
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Purpose/type of the meeting
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MeetingType meetingType = MeetingType.ACADEMIC_GUIDANCE;

    /**
     * Detailed description of what the student wants to discuss
     */
    @Column(length = 2000)
    private String description;

    /**
     * Student's preferred date/time for the meeting (optional, just a suggestion)
     */
    @Column(name = "preferred_date_time")
    private LocalDateTime preferredDateTime;

    /**
     * Student's preferred duration in minutes (e.g., 15, 30, 45, 60)
     */
    @Column(name = "preferred_duration_minutes")
    @Builder.Default
    private Integer preferredDurationMinutes = 30;

    /**
     * Priority level (1=LOW, 2=NORMAL, 3=HIGH, 4=URGENT)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 2;

    // ==================== MEETING STATUS ====================

    /**
     * Current status of the meeting request
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PENDING;

    // ==================== SCHEDULE DETAILS (Set by Lecturer when accepting) ====================

    /**
     * Scheduled start time (set by lecturer when accepting)
     */
    @Column(name = "scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    /**
     * Scheduled end time (set by lecturer when accepting)
     */
    @Column(name = "scheduled_end_time")
    private LocalDateTime scheduledEndTime;

    /**
     * Whether this is an online meeting (true) or in-person meeting (false)
     */
    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = true;

    /**
     * For online meetings - link to Google Meet, Zoom, Teams, etc.
     */
    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    /**
     * Name of the online meeting platform (e.g., "Google Meet", "Zoom")
     */
    @Column(name = "meeting_platform", length = 100)
    private String meetingPlatform;

    /**
     * For in-person meetings - the venue/location (e.g., "Room 301, Building A")
     */
    @Column(length = 300)
    private String location;

    // ==================== RESPONSE DETAILS ====================

    /**
     * Lecturer's response message (e.g., reason for rejection or additional info)
     */
    @Column(name = "lecturer_response", length = 1000)
    private String lecturerResponse;

    /**
     * Timestamp when the lecturer responded (accepted/rejected)
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // ==================== CANCELLATION DETAILS ====================

    /**
     * Reason for cancellation
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Who cancelled the meeting (STUDENT, LECTURER, or ADMIN)
     */
    @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;

    /**
     * Timestamp when meeting was cancelled
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ==================== COMPLETION DETAILS ====================

    /**
     * Meeting notes/summary (can be added after meeting)
     */
    @Column(name = "meeting_notes", length = 4000)
    private String meetingNotes;

    /**
     * Timestamp when meeting actually started
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Timestamp when meeting actually ended
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Follow-up required flag
     */
    @Column(name = "follow_up_required", nullable = false)
    @Builder.Default
    private Boolean followUpRequired = false;

    /**
     * Follow-up notes/action items
     */
    @Column(name = "follow_up_notes", length = 2000)
    private String followUpNotes;

    // ==================== FEEDBACK/RATING ====================

    /**
     * Student's rating of the meeting (1-5 stars)
     */
    @Column(name = "student_rating")
    private Integer studentRating;

    /**
     * Student's feedback comment
     */
    @Column(name = "student_feedback", length = 1000)
    private String studentFeedback;

    /**
     * Timestamp when student provided feedback
     */
    @Column(name = "feedback_submitted_at")
    private LocalDateTime feedbackSubmittedAt;

    // ==================== RECURRING MEETING ====================

    /**
     * ID of the parent meeting (if this is part of a recurring series)
     */
    @Column(name = "parent_meeting_id")
    private Long parentMeetingId;

    /**
     * Recurrence pattern (e.g., "WEEKLY", "BIWEEKLY", "MONTHLY")
     */
    @Column(name = "recurrence_pattern", length = 20)
    private String recurrencePattern;

    /**
     * End date for recurring meetings
     */
    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    // ==================== CALENDAR INTEGRATION ====================

    /**
     * External calendar event ID (for Google Calendar, Outlook, etc.)
     */
    @Column(name = "calendar_event_id", length = 200)
    private String calendarEventId;

    /**
     * Whether the calendar has been synced
     */
    @Column(name = "calendar_synced", nullable = false)
    @Builder.Default
    private Boolean calendarSynced = false;

    // ==================== NOTIFICATION TRACKING ====================

    /**
     * Whether the student has been notified of the response
     */
    @Column(name = "student_notified", nullable = false)
    @Builder.Default
    private Boolean studentNotified = false;

    /**
     * Whether a reminder has been sent (24 hours before)
     */
    @Column(name = "reminder_sent", nullable = false)
    @Builder.Default
    private Boolean reminderSent = false;

    /**
     * Whether a 1-hour reminder has been sent
     */
    @Column(name = "final_reminder_sent", nullable = false)
    @Builder.Default
    private Boolean finalReminderSent = false;

    // ==================== ATTACHMENT ====================

    /**
     * URL to any attached document (e.g., project proposal, assignment)
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * Name of the attached file
     */
    @Column(name = "attachment_name", length = 200)
    private String attachmentName;

    // ==================== HELPER METHODS ====================

    /**
     * Check if the meeting can be joined (within time window and scheduled)
     * Allows joining 10 minutes before scheduled start until scheduled end
     */
    public boolean isJoinable() {
        if (scheduledStartTime == null || scheduledEndTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinWindowStart = scheduledStartTime.minusMinutes(10);
        return now.isAfter(joinWindowStart) &&
               now.isBefore(scheduledEndTime) &&
               (status == MeetingStatus.SCHEDULED || status == MeetingStatus.IN_PROGRESS);
    }

    /**
     * Check if the meeting request is still pending
     */
    public boolean isPending() {
        return status == MeetingStatus.PENDING;
    }

    /**
     * Check if the meeting can be cancelled
     */
    public boolean canBeCancelled() {
        return status == MeetingStatus.PENDING ||
               status == MeetingStatus.ACCEPTED ||
               status == MeetingStatus.SCHEDULED ||
               status == MeetingStatus.RESCHEDULED;
    }

    /**
     * Check if the meeting can be rescheduled
     */
    public boolean canBeRescheduled() {
        return status == MeetingStatus.SCHEDULED || status == MeetingStatus.RESCHEDULED;
    }

    /**
     * Check if feedback can be submitted (only after completion)
     */
    public boolean canSubmitFeedback() {
        return status == MeetingStatus.COMPLETED && studentRating == null;
    }

    /**
     * Get the scheduled duration in minutes
     */
    public Integer getDurationMinutes() {
        if (scheduledStartTime != null && scheduledEndTime != null) {
            return (int) Duration.between(scheduledStartTime, scheduledEndTime).toMinutes();
        }
        return preferredDurationMinutes;
    }

    /**
     * Get actual duration in minutes (after meeting is completed)
     */
    public Integer getActualDurationMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return (int) Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return null;
    }

    /**
     * Check if this is a high priority meeting
     */
    public boolean isHighPriority() {
        return priority != null && priority >= 3;
    }

    /**
     * Check if this is a recurring meeting
     */
    public boolean isRecurring() {
        return recurrencePattern != null && !recurrencePattern.isEmpty();
    }

    /**
     * Get response time in hours (time from request to lecturer response)
     */
    public Long getResponseTimeHours() {
        if (getCreatedAt() != null && respondedAt != null) {
            return Duration.between(getCreatedAt(), respondedAt).toHours();
        }
        return null;
    }

    /**
     * Get priority display name
     */
    public String getPriorityDisplayName() {
        return switch (priority) {
            case 1 -> "Low";
            case 2 -> "Normal";
            case 3 -> "High";
            case 4 -> "Urgent";
            default -> "Normal";
        };
    }
}
