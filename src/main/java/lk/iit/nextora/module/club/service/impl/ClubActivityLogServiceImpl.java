package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubActivityLogRepository;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for club activity logging (audit trail).
 * Logs are written asynchronously in a separate transaction to avoid
 * impacting the main business transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClubActivityLogServiceImpl implements ClubActivityLogService {

    private final ClubActivityLogRepository activityLogRepository;
    private final ClubMapper clubMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Club club, ClubActivityLog.ActivityType activityType, String description,
                    Long performedByUserId, String performedByName,
                    Long targetUserId, String targetUserName,
                    Long relatedEntityId, String relatedEntityType, String metadata) {
        try {
            ClubActivityLog activityLog = ClubActivityLog.builder()
                    .club(club)
                    .activityType(activityType)
                    .description(description)
                    .performedByUserId(performedByUserId)
                    .performedByName(performedByName)
                    .targetUserId(targetUserId)
                    .targetUserName(targetUserName)
                    .relatedEntityId(relatedEntityId)
                    .relatedEntityType(relatedEntityType)
                    .metadata(metadata)
                    .build();

            activityLogRepository.save(activityLog);
            log.debug("Activity logged: {} for club {} by user {}", activityType, club.getId(), performedByUserId);
        } catch (Exception e) {
            // Never let logging failure break the main flow
            log.error("Failed to log activity: {} for club {}: {}", activityType, club.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ClubActivityLogResponse> getActivityLogs(Long clubId, Pageable pageable) {
        Page<ClubActivityLog> logs = activityLogRepository.findByClubIdOrderByCreatedAtDesc(clubId, pageable);
        return toPagedResponse(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ClubActivityLogResponse> getActivityLogsByType(Long clubId, ClubActivityLog.ActivityType type, Pageable pageable) {
        Page<ClubActivityLog> logs = activityLogRepository.findByClubIdAndType(clubId, type, pageable);
        return toPagedResponse(logs);
    }

    private PagedResponse<ClubActivityLogResponse> toPagedResponse(Page<ClubActivityLog> page) {
        return PagedResponse.<ClubActivityLogResponse>builder()
                .content(clubMapper.toActivityLogResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public void deleteByClubId(Long clubId) {
        activityLogRepository.deleteByClubId(clubId);
        log.info("Permanently deleted all activity logs for club: {}", clubId);
    }
}

