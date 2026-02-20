package lk.iit.nextora.module.event.dto.response;

import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Event details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String location;
    private String venue;
    private EventType eventType;
    private EventStatus status;
    private Long viewCount;
    private Integer maxAttendees;
    private String registrationLink;
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // Creator details
    private Long createdById;
    private String createdByName;
    private String createdByEmail;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;

    // Business logic flags
    private Boolean canEdit;
    private Boolean isVisible;
    private Boolean isUpcoming;
    private Boolean isOngoing;
}
