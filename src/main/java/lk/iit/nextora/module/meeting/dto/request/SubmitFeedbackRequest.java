package lk.iit.nextora.module.meeting.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for a student to submit feedback after a meeting.
 *
 * <p>This allows students to rate their meeting experience and provide
 * feedback to help improve the system and lecturer-student interactions.</p>
 *
 * <h4>Example Usage:</h4>
 * <pre>
 * {
 *   "rating": 5,
 *   "feedback": "Very helpful meeting. The lecturer explained everything clearly."
 * }
 * </pre>
 *
 * @author Nextora Development Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitFeedbackRequest {

    /**
     * Rating from 1-5 stars
     * 1 = Very Poor, 2 = Poor, 3 = Average, 4 = Good, 5 = Excellent
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    /**
     * Optional feedback comment
     */
    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    private String feedback;
}
