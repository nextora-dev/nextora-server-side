package lk.iit.nextora.module.meeting.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for a lecturer to accept a meeting request and schedule it.
 *
 * This is sent by the LECTURER when accepting a meeting request.
 * The lecturer must set the time, location, and meeting link (if online).
 * This will trigger a notification to the student and update the lecturer's calendar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptMeetingRequest {

    /**
     * Scheduled start time for the meeting
     */
    @NotNull(message = "Scheduled start time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledStartTime;

    /**
     * Scheduled end time for the meeting
     */
    @NotNull(message = "Scheduled end time is required")
    @Future(message = "Scheduled end time must be in the future")
    private LocalDateTime scheduledEndTime;

    /**
     * Whether this is an online meeting (true) or in-person (false)
     */
    @NotNull(message = "Please specify if this is an online meeting")
    private Boolean isOnline;

    /**
     * Link for online meetings (required if isOnline is true)
     */
    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    private String meetingLink;

    /**
     * Platform name for online meetings (e.g., "Google Meet", "Zoom")
     */
    @Size(max = 100, message = "Meeting platform must not exceed 100 characters")
    private String meetingPlatform;

    /**
     * Physical location for in-person meetings (required if isOnline is false)
     */
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    /**
     * Optional message from the lecturer to the student
     */
    @Size(max = 1000, message = "Response message must not exceed 1000 characters")
    private String responseMessage;
}
