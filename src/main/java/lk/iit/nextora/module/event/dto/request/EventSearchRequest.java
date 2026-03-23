package lk.iit.nextora.module.event.dto.request;

import lk.iit.nextora.common.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for searching events
 * Used by: All users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchRequest {

    private String keyword;
    private String location;
    private String creatorName;
    private EventType eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
