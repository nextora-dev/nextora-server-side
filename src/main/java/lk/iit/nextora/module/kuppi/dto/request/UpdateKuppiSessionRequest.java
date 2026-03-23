package lk.iit.nextora.module.kuppi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for updating a Kuppi session
 * Used by: Kuppi Students (own sessions), Admin, Super Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKuppiSessionRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    @Size(max = 500, message = "Live link must not exceed 500 characters")
    private String liveLink;

    @Size(max = 200, message = "Meeting platform must not exceed 200 characters")
    private String meetingPlatform;
}


