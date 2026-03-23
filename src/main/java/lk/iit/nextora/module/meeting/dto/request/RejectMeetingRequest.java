package lk.iit.nextora.module.meeting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for a lecturer to reject a meeting request.
 *
 * This is sent by the LECTURER when declining a meeting request.
 * A reason should be provided to help the student understand why.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectMeetingRequest {

    /**
     * Reason for rejecting the meeting request
     */
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String reason;
}
