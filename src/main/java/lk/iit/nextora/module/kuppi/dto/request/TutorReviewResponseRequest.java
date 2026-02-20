package lk.iit.nextora.module.kuppi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for tutor to respond to a review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorReviewResponseRequest {

    @NotBlank(message = "Response text is required")
    @Size(max = 1000, message = "Response must be at most 1000 characters")
    private String responseText;
}

