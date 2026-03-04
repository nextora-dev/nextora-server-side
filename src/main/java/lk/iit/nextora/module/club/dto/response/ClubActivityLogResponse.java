package lk.iit.nextora.module.club.dto.response;

import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for club activity log entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubActivityLogResponse {

    private Long id;
    private Long clubId;
    private String clubName;
    private ClubActivityLog.ActivityType activityType;
    private String description;
    private Long performedByUserId;
    private String performedByName;
    private Long targetUserId;
    private String targetUserName;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String metadata;
    private LocalDateTime createdAt;
}

