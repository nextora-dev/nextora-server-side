package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing the status/lifecycle of a meeting request.
 *
 * Flow: PENDING -> ACCEPTED/REJECTED
 *       ACCEPTED -> SCHEDULED -> IN_PROGRESS -> COMPLETED
 *       Any status can go to CANCELLED
 *
 * PENDING - Student has requested a meeting, awaiting lecturer response
 * ACCEPTED - Lecturer has accepted the request (but not yet scheduled time/location)
 * REJECTED - Lecturer has rejected the meeting request
 * SCHEDULED - Meeting time and location have been set
 * IN_PROGRESS - Meeting is currently happening
 * COMPLETED - Meeting has ended
 * CANCELLED - Meeting was cancelled by either party
 * RESCHEDULED - Meeting was rescheduled to a new time
 */
@Getter
public enum MeetingStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled");

    private final String displayName;

    MeetingStatus(String displayName) {
        this.displayName = displayName;
    }
}
