package lk.iit.nextora.module.event.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an existing event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @FutureOrPresent(message = "Start time must not be in the past")
    private LocalDateTime startAt;

    @FutureOrPresent(message = "End time must not be in the past")
    private LocalDateTime endAt;

    @Size(max = 300, message = "Location must not exceed 300 characters")
    private String location;

    @Size(max = 300, message = "Venue must not exceed 300 characters")
    private String venue;

    private EventType eventType;

    private Integer maxAttendees;

    @Size(max = 500, message = "Registration link must not exceed 500 characters")
    private String registrationLink;
}
