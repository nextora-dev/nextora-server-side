package lk.iit.nextora.module.club.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import org.springframework.data.domain.Pageable;

/**
 * Service for recording and querying club activity logs (audit trail).
 */
public interface ClubActivityLogService {

    void log(Club club, ClubActivityLog.ActivityType activityType, String description,
             Long performedByUserId, String performedByName,
             Long targetUserId, String targetUserName,
             Long relatedEntityId, String relatedEntityType, String metadata);

    default void log(Club club, ClubActivityLog.ActivityType activityType, String description,
                     Long performedByUserId, String performedByName) {
        log(club, activityType, description, performedByUserId, performedByName, null, null, null, null, null);
    }

    PagedResponse<ClubActivityLogResponse> getActivityLogs(Long clubId, Pageable pageable);

    PagedResponse<ClubActivityLogResponse> getActivityLogsByType(Long clubId, ClubActivityLog.ActivityType type, Pageable pageable);

    /**
     * Permanently delete all activity logs for a club.
     * Used during permanent club deletion (Super Admin only).
     */
    void deleteByClubId(Long clubId);
}

