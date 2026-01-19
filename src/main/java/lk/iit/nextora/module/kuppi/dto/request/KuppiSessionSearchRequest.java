package lk.iit.nextora.module.kuppi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for searching Kuppi sessions
 * Used by: All students
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiSessionSearchRequest {

    private String keyword;
    private String subject;
    private String hostName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}

