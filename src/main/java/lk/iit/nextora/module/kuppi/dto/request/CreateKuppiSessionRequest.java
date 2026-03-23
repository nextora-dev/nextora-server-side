package lk.iit.nextora.module.kuppi.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a Kuppi session
 * Users click on liveLink to join the session on external platform (Google Meet, Zoom, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKuppiSessionRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @NotNull(message = "Scheduled start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime scheduledStartTime;

    @NotNull(message = "Scheduled end time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime scheduledEndTime;

    @NotBlank(message = "Live link is required")
    @Size(max = 500, message = "Live link must not exceed 500 characters")
    private String liveLink;

    @Size(max = 200, message = "Meeting platform must not exceed 200 characters")
    private String meetingPlatform;
}