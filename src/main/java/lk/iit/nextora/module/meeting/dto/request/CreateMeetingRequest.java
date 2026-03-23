package lk.iit.nextora.module.meeting.dto.request;

import jakarta.validation.constraints.*;
import lk.iit.nextora.common.enums.MeetingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for a student to request a meeting with a lecturer.
 *
 * <p>This is sent by the STUDENT when initiating a meeting request.
 * The lecturer ID is required, other fields help the lecturer understand
 * what the meeting is about and when the student is available.</p>
 *
 * <h4>Example Usage:</h4>
 * <pre>
 * {
 *   "lecturerId": 1,
 *   "subject": "Project Proposal Discussion",
 *   "meetingType": "PROJECT_DISCUSSION",
 *   "description": "I would like to discuss my final year project proposal",
 *   "preferredDateTime": "2026-02-15T10:00:00",
 *   "preferredDurationMinutes": 30,
 *   "priority": 2
 * }
 * </pre>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingRequest {

    /**
     * The ID of the lecturer the student wants to meet
     */
    @NotNull(message = "Lecturer ID is required")
    private Long lecturerId;

    /**
     * Brief subject/title of the meeting
     */
    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    /**
     * Type/purpose of the meeting
     */
    @NotNull(message = "Meeting type is required")
    private MeetingType meetingType;

    /**
     * Detailed description of what the student wants to discuss
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Student's preferred date/time for the meeting (optional suggestion)
     */
    private LocalDateTime preferredDateTime;

    /**
     * Student's preferred meeting duration in minutes (default: 30)
     * Must be in increments typically used: 15, 30, 45, 60, 90, 120
     */
    @Min(value = 15, message = "Minimum meeting duration is 15 minutes")
    @Max(value = 120, message = "Maximum meeting duration is 120 minutes")
    @Builder.Default
    private Integer preferredDurationMinutes = 30;

    /**
     * Priority level of the meeting request.
     * 1 = Low (can wait), 2 = Normal, 3 = High, 4 = Urgent (needs immediate attention)
     */
    @Min(value = 1, message = "Priority must be between 1 and 4")
    @Max(value = 4, message = "Priority must be between 1 and 4")
    @Builder.Default
    private Integer priority = 2;

    /**
     * URL to any attachment (optional) - e.g., project proposal, assignment
     */
    @Size(max = 500, message = "Attachment URL must not exceed 500 characters")
    private String attachmentUrl;

    /**
     * Name of the attachment file (optional)
     */
    @Size(max = 200, message = "Attachment name must not exceed 200 characters")
    private String attachmentName;
}
