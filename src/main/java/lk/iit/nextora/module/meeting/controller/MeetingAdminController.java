package lk.iit.nextora.module.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.dto.response.MeetingStatisticsResponse;
import lk.iit.nextora.module.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Admin Meeting operations.
 *
 * Provides admin-only endpoints for:
 * - Viewing all meetings across the platform
 * - Force canceling any meeting
 * - Permanently deleting meetings
 * - Platform-wide and lecturer-specific statistics
 *
 * All endpoints require admin permissions.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.MEETING_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Meeting Admin", description = "Admin endpoints for meeting management")
public class MeetingAdminController {

    private final MeetingService meetingService;

    // ==================== Admin View Operations ====================

    @GetMapping
    @Operation(summary = "Get all meetings", description = "Get all meetings across the platform (admin view)")
    @PreAuthorize("hasAuthority('MEETING:ADMIN_READ')")
    public ApiResponse<PagedResponse<MeetingResponse>> getAllMeetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin fetching all meetings");
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getAllMeetings(pageable);
        return ApiResponse.success("All meetings retrieved successfully", response);
    }

    // ==================== Admin Cancel/Delete Operations ====================

    @PostMapping(ApiConstants.MEETING_ADMIN_CANCEL)
    @Operation(summary = "Force cancel meeting", description = "Admin force cancels any meeting")
    @PreAuthorize("hasAuthority('MEETING:ADMIN_CANCEL')")
    public ApiResponse<Void> adminCancelMeeting(
            @PathVariable Long meetingId,
            @RequestParam String reason) {

        log.info("Admin force canceling meeting {} with reason: {}", meetingId, reason);
        meetingService.adminCancelMeeting(meetingId, reason);
        return ApiResponse.success("Meeting cancelled successfully by admin");
    }

    @DeleteMapping(ApiConstants.MEETING_ADMIN_PERMANENT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Permanently delete meeting", description = "Permanently delete a meeting (hard delete)")
    @PreAuthorize("hasAuthority('MEETING:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteMeeting(@PathVariable Long meetingId) {
        log.info("Admin permanently deleting meeting {}", meetingId);
        meetingService.permanentlyDeleteMeeting(meetingId);
        return ApiResponse.success("Meeting permanently deleted");
    }

    // ==================== Statistics Operations ====================

    @GetMapping(ApiConstants.MEETING_ADMIN_STATISTICS)
    @Operation(summary = "Get platform statistics", description = "Get platform-wide meeting statistics")
    @PreAuthorize("hasAuthority('MEETING:VIEW_STATISTICS')")
    public ApiResponse<MeetingStatisticsResponse> getPlatformStatistics() {
        log.info("Admin fetching platform-wide meeting statistics");
        MeetingStatisticsResponse response = meetingService.getPlatformStatistics();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_ADMIN_LECTURER_STATISTICS)
    @Operation(summary = "Get lecturer statistics", description = "Get meeting statistics for a specific lecturer")
    @PreAuthorize("hasAuthority('MEETING:VIEW_STATISTICS')")
    public ApiResponse<MeetingStatisticsResponse> getLecturerStatistics(@PathVariable Long lecturerId) {
        log.info("Admin fetching meeting statistics for lecturer {}", lecturerId);
        MeetingStatisticsResponse response = meetingService.getLecturerStatistics(lecturerId);
        return ApiResponse.success("Lecturer statistics retrieved successfully", response);
    }
}
