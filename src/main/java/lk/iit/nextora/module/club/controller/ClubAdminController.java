package lk.iit.nextora.module.club.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.request.ChangeMemberPositionRequest;
import lk.iit.nextora.module.club.dto.response.*;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.module.club.service.ClubAnnouncementService;
import lk.iit.nextora.module.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for club management operations.
 * Provides admin-only endpoints: statistics, activity logs, bulk operations,
 * force membership management, and platform-wide club oversight.
 */
@RestController
@RequestMapping(ApiConstants.CLUB_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Club Admin", description = "Admin endpoints for club management and oversight")
public class ClubAdminController {

    private final ClubService clubService;
    private final ClubActivityLogService activityLogService;
    private final ClubAnnouncementService announcementService;

    // ==================== Club Statistics ====================

    @GetMapping(ApiConstants.CLUB_ADMIN_STATS + "/{clubId}")
    @Operation(summary = "Get club statistics", description = "Get detailed statistics for a specific club")
    @PreAuthorize("hasAuthority('CLUB:VIEW_STATS')")
    public ApiResponse<ClubStatisticsResponse> getClubStatistics(@PathVariable Long clubId) {
        ClubStatisticsResponse response = clubService.getClubStatistics(clubId);
        return ApiResponse.success("Statistics retrieved successfully", response);
    }

    // ==================== Activity Logs ====================

    @GetMapping(ApiConstants.CLUB_ADMIN_ACTIVITY_LOG)
    @Operation(summary = "Get activity logs", description = "Get activity audit logs for a club")
    @PreAuthorize("hasAuthority('CLUB:VIEW_ACTIVITY_LOG')")
    public ApiResponse<PagedResponse<ClubActivityLogResponse>> getActivityLogs(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubActivityLogResponse> response = activityLogService.getActivityLogs(clubId, pageable);
        return ApiResponse.success("Activity logs retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_ADMIN_ACTIVITY_LOG + "/type/{type}")
    @Operation(summary = "Get activity logs by type", description = "Filter activity logs by activity type")
    @PreAuthorize("hasAuthority('CLUB:VIEW_ACTIVITY_LOG')")
    public ApiResponse<PagedResponse<ClubActivityLogResponse>> getActivityLogsByType(
            @PathVariable Long clubId,
            @PathVariable ClubActivityLog.ActivityType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubActivityLogResponse> response = activityLogService.getActivityLogsByType(clubId, type, pageable);
        return ApiResponse.success("Activity logs retrieved successfully", response);
    }

    // ==================== Membership Management ====================

    @PutMapping(ApiConstants.MEMBERSHIP_CHANGE_POSITION)
    @Operation(summary = "Change member position", description = "Change a member's position within the club (President/Admin only)")
    @PreAuthorize("hasAuthority('CLUB:MANAGE_MEMBERS')")
    public ApiResponse<ClubMembershipResponse> changeMemberPosition(
            @PathVariable Long membershipId,
            @Valid @RequestBody ChangeMemberPositionRequest request) {

        ClubMembershipResponse response = clubService.changeMemberPosition(
                membershipId, request.getNewPosition(), request.getReason());
        return ApiResponse.success("Member position changed successfully", response);
    }

    @PostMapping(ApiConstants.MEMBERSHIP_BULK_APPROVE)
    @Operation(summary = "Bulk approve memberships", description = "Approve multiple pending memberships at once")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<List<ClubMembershipResponse>> bulkApproveMemberships(
            @PathVariable Long clubId,
            @RequestBody List<Long> membershipIds) {

        List<ClubMembershipResponse> response = clubService.bulkApproveMemberships(clubId, membershipIds);
        return ApiResponse.success("Bulk approval completed: " + response.size() + " memberships approved", response);
    }

    // ==================== Club Settings ====================

    @PutMapping(ApiConstants.CLUB_TOGGLE_REGISTRATION)
    @Operation(summary = "Toggle registration", description = "Toggle club registration open/closed")
    @PreAuthorize("hasAuthority('CLUB:UPDATE')")
    public ApiResponse<ClubResponse> toggleRegistration(@PathVariable Long clubId) {
        ClubResponse response = clubService.toggleRegistration(clubId);
        return ApiResponse.success("Registration toggled successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_STATISTICS)
    @Operation(summary = "Get club statistics (officer)", description = "Get club statistics - accessible by club officers and admins")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<ClubStatisticsResponse> getClubStatsOfficer(@PathVariable Long clubId) {
        ClubStatisticsResponse response = clubService.getClubStatistics(clubId);
        return ApiResponse.success("Statistics retrieved successfully", response);
    }

    // ==================== Permanent Delete (Super Admin only) ====================

    @DeleteMapping(ApiConstants.CLUB_ADMIN_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Permanently delete club",
               description = "Permanently delete a club and ALL associated data (memberships, announcements, activity logs, elections). " +
                             "This action is IRREVERSIBLE. Super Admin only.")
    @PreAuthorize("hasAuthority('CLUB:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteClub(@PathVariable Long clubId) {
        clubService.permanentlyDeleteClub(clubId);
        return ApiResponse.success("Club and all associated data permanently deleted");
    }

    @DeleteMapping(ApiConstants.CLUB_ADMIN_ANNOUNCEMENT_PERMANENT_DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Permanently delete announcement",
               description = "Permanently delete an announcement and its S3 attachment from the database. " +
                             "This action is IRREVERSIBLE. Super Admin only.")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteAnnouncement(@PathVariable Long announcementId) {
        announcementService.permanentlyDeleteAnnouncement(announcementId);
        return ApiResponse.success("Announcement permanently deleted");
    }
}

