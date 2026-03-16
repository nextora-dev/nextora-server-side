
package lk.iit.nextora.module.meeting.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for rescheduling an existing meeting.
 *
 * This can be used by the LECTURER to change the meeting time/location.
 * The student will be notified of the change.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleMeetingRequest {

    /**
     * New scheduled start time
     */
    @NotNull(message = "New scheduled start time is required")
    @Future(message = "New scheduled time must be in the future")
    private LocalDateTime scheduledStartTime;

    /**
     * New scheduled end time
     */
    @NotNull(message = "New scheduled end time is required")
    @Future(message = "New scheduled end time must be in the future")
    private LocalDateTime scheduledEndTime;

    /**
     * Whether this is an online meeting (optional - keeps existing if not provided)
     */
    private Boolean isOnline;

    /**
     * New meeting link (optional - keeps existing if not provided)
     */
    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    private String meetingLink;

    /**
     * New meeting platform (optional)
     */
    @Size(max = 100, message = "Meeting platform must not exceed 100 characters")
    private String meetingPlatform;

    /**
     * New physical location (optional - keeps existing if not provided)
     */
    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    /**
     * Reason for rescheduling (will be sent to student)
     */
    @Size(max = 500, message = "Reschedule reason must not exceed 500 characters")
    private String reason;
}
