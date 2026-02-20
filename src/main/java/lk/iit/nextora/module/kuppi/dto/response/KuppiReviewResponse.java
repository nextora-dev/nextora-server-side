package lk.iit.nextora.module.kuppi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Kuppi Review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KuppiReviewResponse {

    private Long id;

    private Long sessionId;
    private String sessionTitle;

    private Long reviewerId;
    private String reviewerName;

    private Long tutorId;
    private String tutorName;

    private Integer rating;
    private String comment;

    private String tutorResponse;
    private LocalDateTime tutorResponseAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
