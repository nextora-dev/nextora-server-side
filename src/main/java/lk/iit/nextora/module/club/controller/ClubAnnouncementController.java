package lk.iit.nextora.module.club.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.request.CreateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.request.UpdateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lk.iit.nextora.module.club.service.ClubAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for Club Announcement management operations.
 * Supports creating, reading, updating, deleting, pinning announcements.
 */
@RestController
@RequestMapping(ApiConstants.CLUB_ANNOUNCEMENTS)
@RequiredArgsConstructor
@Tag(name = "Club Announcements", description = "Club announcement management endpoints")
public class ClubAnnouncementController {

    private final ClubAnnouncementService announcementService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create announcement", description = "Create a new club announcement with optional attachment (Club officers only)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:CREATE')")
    public ApiResponse<ClubAnnouncementResponse> createAnnouncement(
            @RequestParam("clubId") Long clubId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "priority", defaultValue = "NORMAL") String priority,
            @RequestParam(value = "isPinned", defaultValue = "false") Boolean isPinned,
            @RequestParam(value = "isMembersOnly", defaultValue = "false") Boolean isMembersOnly,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {

        CreateAnnouncementRequest request = CreateAnnouncementRequest.builder()
                .clubId(clubId)
                .title(title)
                .content(content)
                .priority(ClubAnnouncement.AnnouncementPriority.valueOf(priority))
                .isPinned(isPinned)
                .isMembersOnly(isMembersOnly)
                .build();

        ClubAnnouncementResponse response = announcementService.createAnnouncement(request, attachment);
        return ApiResponse.success("Announcement created successfully", response);
    }

    @PutMapping(value = ApiConstants.CLUB_ANNOUNCEMENT_BY_ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update announcement", description = "Update announcement details (Club officers only)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:UPDATE')")
    public ApiResponse<ClubAnnouncementResponse> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "isPinned", required = false) Boolean isPinned,
            @RequestParam(value = "isMembersOnly", required = false) Boolean isMembersOnly,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment) {

        UpdateAnnouncementRequest request = UpdateAnnouncementRequest.builder()
                .title(title)
                .content(content)
                .priority(priority != null ? ClubAnnouncement.AnnouncementPriority.valueOf(priority) : null)
                .isPinned(isPinned)
                .isMembersOnly(isMembersOnly)
                .build();

        ClubAnnouncementResponse response = announcementService.updateAnnouncement(announcementId, request, attachment);
        return ApiResponse.success("Announcement updated successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_ANNOUNCEMENT_BY_ID)
    @Operation(summary = "Get announcement", description = "Get announcement details (increments view count)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:READ')")
    public ApiResponse<ClubAnnouncementResponse> getAnnouncementById(@PathVariable Long announcementId) {
        ClubAnnouncementResponse response = announcementService.getAnnouncementById(announcementId);
        return ApiResponse.success("Announcement retrieved successfully", response);
    }

    @DeleteMapping(ApiConstants.CLUB_ANNOUNCEMENT_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete announcement", description = "Soft delete an announcement (Club officers only)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:DELETE')")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        announcementService.deleteAnnouncement(announcementId);
        return ApiResponse.success("Announcement deleted successfully", null);
    }

    @GetMapping(ApiConstants.CLUB_ANNOUNCEMENTS_BY_CLUB)
    @Operation(summary = "Get club announcements", description = "Get all announcements for a club (members see all, non-members see public)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:READ')")
    public ApiResponse<PagedResponse<ClubAnnouncementResponse>> getAnnouncementsByClub(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubAnnouncementResponse> response = announcementService.getAnnouncementsByClub(clubId, pageable);
        return ApiResponse.success("Announcements retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_ANNOUNCEMENTS_PUBLIC)
    @Operation(summary = "Get public announcements", description = "Get only public announcements for a club")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:READ')")
    public ApiResponse<PagedResponse<ClubAnnouncementResponse>> getPublicAnnouncements(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubAnnouncementResponse> response = announcementService.getPublicAnnouncementsByClub(clubId, pageable);
        return ApiResponse.success("Public announcements retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_ANNOUNCEMENTS_PINNED)
    @Operation(summary = "Get pinned announcements", description = "Get pinned announcements for a club")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:READ')")
    public ApiResponse<PagedResponse<ClubAnnouncementResponse>> getPinnedAnnouncements(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubAnnouncementResponse> response = announcementService.getPinnedAnnouncementsByClub(clubId, pageable);
        return ApiResponse.success("Pinned announcements retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_ANNOUNCEMENTS_SEARCH)
    @Operation(summary = "Search announcements", description = "Search announcements by keyword")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:READ')")
    public ApiResponse<PagedResponse<ClubAnnouncementResponse>> searchAnnouncements(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubAnnouncementResponse> response = announcementService.searchAnnouncements(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @PutMapping(ApiConstants.CLUB_ANNOUNCEMENT_PIN)
    @Operation(summary = "Pin announcement", description = "Pin an announcement to top (Club officers only)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:UPDATE')")
    public ApiResponse<ClubAnnouncementResponse> pinAnnouncement(@PathVariable Long announcementId) {
        ClubAnnouncementResponse response = announcementService.pinAnnouncement(announcementId);
        return ApiResponse.success("Announcement pinned successfully", response);
    }

    @PutMapping(ApiConstants.CLUB_ANNOUNCEMENT_UNPIN)
    @Operation(summary = "Unpin announcement", description = "Unpin an announcement (Club officers only)")
    @PreAuthorize("hasAuthority('CLUB_ANNOUNCEMENT:UPDATE')")
    public ApiResponse<ClubAnnouncementResponse> unpinAnnouncement(@PathVariable Long announcementId) {
        ClubAnnouncementResponse response = announcementService.unpinAnnouncement(announcementId);
        return ApiResponse.success("Announcement unpinned successfully", response);
    }
}

