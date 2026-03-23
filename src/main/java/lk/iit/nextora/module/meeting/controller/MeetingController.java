package lk.iit.nextora.module.meeting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.meeting.dto.request.*;
import lk.iit.nextora.module.meeting.dto.response.MeetingResponse;
import lk.iit.nextora.module.meeting.dto.response.MeetingStatisticsResponse;
import lk.iit.nextora.module.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Meeting operations (Student-Lecturer meetings).
 *
 * MEETING FLOW:
 * 1. Student requests meeting with a lecturer (POST /meetings)
 * 2. Lecturer views pending requests (GET /meetings/lecturer/pending)
 * 3. Lecturer accepts with time/location (POST /meetings/{id}/accept)
 *    OR rejects with reason (POST /meetings/{id}/reject)
 * 4. Student receives notification about the decision
 * 5. Lecturer's calendar is automatically updated
 *
 * Endpoints are organized by role:
 * - Student endpoints: Create request, view own meetings, cancel
 * - Lecturer endpoints: View requests, accept/reject, reschedule, cancel
 * - Common endpoints: View meeting details, start/complete meeting
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.MEETINGS)
@RequiredArgsConstructor
@Tag(name = "Meetings", description = "Student-Lecturer meeting management endpoints")
public class MeetingController {

    private final MeetingService meetingService;
    private final S3Service s3Service;
    private final AcademicStaffRepository academicStaffRepository;
    private final SecurityService securityService;

    // ==================== Student Endpoints ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request meeting", description = "Student requests a meeting with a lecturer")
    @PreAuthorize("hasAuthority('MEETING:CREATE')")
    public ApiResponse<MeetingResponse> createMeetingRequest(@Valid @RequestBody CreateMeetingRequest request) {
        log.info("Creating meeting request with lecturer {}", request.getLecturerId());
        MeetingResponse response = meetingService.createMeetingRequest(request);
        return ApiResponse.success("Meeting request submitted successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_MY_REQUESTS)
    @Operation(summary = "Get my meeting requests", description = "Get all meeting requests made by the current student")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<PagedResponse<MeetingResponse>> getMyMeetingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMyMeetingRequests(pageable);
        return ApiResponse.success("Meeting requests retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_MY_UPCOMING)
    @Operation(summary = "Get my upcoming meetings", description = "Get upcoming scheduled meetings for the current student")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<PagedResponse<MeetingResponse>> getMyUpcomingMeetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMyUpcomingMeetings(pageable);
        return ApiResponse.success("Upcoming meetings retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_MY_SEARCH)
    @Operation(summary = "Search my meetings", description = "Search student's meetings by keyword")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<PagedResponse<MeetingResponse>> searchMyMeetings(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.searchMyMeetings(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @PostMapping(ApiConstants.MEETING_CANCEL_BY_STUDENT)
    @Operation(summary = "Cancel meeting request", description = "Student cancels their meeting request")
    @PreAuthorize("hasAuthority('MEETING:CANCEL')")
    public ApiResponse<Void> cancelMeetingByStudent(
            @PathVariable Long meetingId,
            @RequestParam String reason) {

        meetingService.cancelMeetingRequestByStudent(meetingId, reason);
        return ApiResponse.success("Meeting request cancelled successfully");
    }

    @PostMapping("/{meetingId}/feedback")
    @Operation(summary = "Submit feedback", description = "Student submits feedback after a completed meeting")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<MeetingResponse> submitFeedback(
            @PathVariable Long meetingId,
            @Valid @RequestBody SubmitFeedbackRequest request) {

        log.info("Submitting feedback for meeting {}", meetingId);
        MeetingResponse response = meetingService.submitFeedback(meetingId, request);
        return ApiResponse.success("Feedback submitted successfully", response);
    }

    @GetMapping("/my/status/{status}")
    @Operation(summary = "Get my meetings by status", description = "Get student's meetings filtered by status")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<PagedResponse<MeetingResponse>> getMyMeetingsByStatus(
            @PathVariable MeetingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMyMeetingRequestsByStatus(status, pageable);
        return ApiResponse.success("Meetings retrieved successfully", response);
    }

    // ==================== Lecturer Endpoints ====================

    @GetMapping(ApiConstants.MEETING_PENDING)
    @Operation(summary = "Get pending requests", description = "Get pending meeting requests awaiting lecturer's response")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getPendingRequests(pageable);
        return ApiResponse.success("Pending requests retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_PENDING_COUNT)
    @Operation(summary = "Get pending requests count", description = "Get count of pending meeting requests (for notification badge)")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<Long> getPendingRequestsCount() {
        long count = meetingService.getPendingRequestsCount();
        return ApiResponse.success("Pending requests count retrieved", count);
    }

    @GetMapping(ApiConstants.MEETING_LECTURER_ALL)
    @Operation(summary = "Get all my requests", description = "Get all meeting requests for the current lecturer")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getAllMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getAllMyRequests(pageable);
        return ApiResponse.success("Meeting requests retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_LECTURER_UPCOMING)
    @Operation(summary = "Get upcoming meetings as lecturer", description = "Get upcoming scheduled meetings for the current lecturer")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getUpcomingMeetingsAsLecturer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMyUpcomingMeetingsAsLecturer(pageable);
        return ApiResponse.success("Upcoming meetings retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_LECTURER_CALENDAR)
    @Operation(summary = "Get calendar meetings", description = "Get meetings for lecturer's calendar within a date range")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<List<MeetingResponse>> getCalendarMeetings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<MeetingResponse> response = meetingService.getCalendarMeetings(startDate, endDate);
        return ApiResponse.success("Calendar meetings retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_LECTURER_SEARCH)
    @Operation(summary = "Search meetings as lecturer", description = "Search lecturer's meetings by keyword")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> searchMeetingsAsLecturer(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.searchMyMeetingsAsLecturer(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.MEETING_LECTURER_STATISTICS)
    @Operation(summary = "Get my statistics", description = "Get meeting statistics for the current lecturer")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingStatisticsResponse> getMyStatistics() {
        MeetingStatisticsResponse response = meetingService.getMyStatistics();
        return ApiResponse.success("Statistics retrieved successfully", response);
    }

    @GetMapping("/lecturer/high-priority")
    @Operation(summary = "Get high priority requests", description = "Get high priority (3+) pending meeting requests")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getHighPriorityRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getHighPriorityRequests(pageable);
        return ApiResponse.success("High priority requests retrieved successfully", response);
    }

    @GetMapping("/lecturer/follow-up")
    @Operation(summary = "Get meetings requiring follow-up", description = "Get completed meetings that require follow-up")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getMeetingsRequiringFollowUp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMeetingsRequiringFollowUp(pageable);
        return ApiResponse.success("Follow-up meetings retrieved successfully", response);
    }

    @GetMapping("/lecturer/status/{status}")
    @Operation(summary = "Get lecturer's meetings by status", description = "Get lecturer's meetings filtered by status")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<PagedResponse<MeetingResponse>> getMyMeetingsByStatusAsLecturer(
            @PathVariable MeetingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<MeetingResponse> response = meetingService.getMyRequestsByStatus(status, pageable);
        return ApiResponse.success("Meetings retrieved successfully", response);
    }

    @PostMapping(ApiConstants.MEETING_ACCEPT)
    @Operation(summary = "Accept meeting request", description = "Lecturer accepts a meeting request and sets time/location")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingResponse> acceptMeetingRequest(
            @PathVariable Long meetingId,
            @Valid @RequestBody AcceptMeetingRequest request) {

        log.info("Accepting meeting request {}", meetingId);
        MeetingResponse response = meetingService.acceptMeetingRequest(meetingId, request);
        return ApiResponse.success("Meeting request accepted and scheduled", response);
    }

    @PostMapping(ApiConstants.MEETING_REJECT)
    @Operation(summary = "Reject meeting request", description = "Lecturer rejects a meeting request with reason")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingResponse> rejectMeetingRequest(
            @PathVariable Long meetingId,
            @Valid @RequestBody RejectMeetingRequest request) {

        log.info("Rejecting meeting request {}", meetingId);
        MeetingResponse response = meetingService.rejectMeetingRequest(meetingId, request);
        return ApiResponse.success("Meeting request rejected", response);
    }

    @PostMapping(ApiConstants.MEETING_RESCHEDULE)
    @Operation(summary = "Reschedule meeting", description = "Lecturer reschedules an existing meeting")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingResponse> rescheduleMeeting(
            @PathVariable Long meetingId,
            @Valid @RequestBody RescheduleMeetingRequest request) {

        log.info("Rescheduling meeting {}", meetingId);
        MeetingResponse response = meetingService.rescheduleMeeting(meetingId, request);
        return ApiResponse.success("Meeting rescheduled successfully", response);
    }

    @PostMapping(ApiConstants.MEETING_CANCEL_BY_LECTURER)
    @Operation(summary = "Cancel meeting", description = "Lecturer cancels a scheduled meeting")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<Void> cancelMeetingByLecturer(
            @PathVariable Long meetingId,
            @RequestParam String reason) {

        meetingService.cancelMeetingByLecturer(meetingId, reason);
        return ApiResponse.success("Meeting cancelled successfully");
    }

    @PostMapping(ApiConstants.MEETING_ADD_NOTES)
    @Operation(summary = "Add meeting notes", description = "Lecturer adds notes to a meeting with optional follow-up")
    @PreAuthorize("hasAuthority('MEETING:ADD_NOTES')")
    public ApiResponse<MeetingResponse> addMeetingNotes(
            @PathVariable Long meetingId,
            @RequestParam String notes,
            @RequestParam(defaultValue = "false") boolean followUpRequired,
            @RequestParam(required = false) String followUpNotes) {

        MeetingResponse response = meetingService.addMeetingNotes(meetingId, notes, followUpRequired, followUpNotes);
        return ApiResponse.success("Meeting notes added successfully", response);
    }

    // ==================== Common Endpoints ====================

    @GetMapping(ApiConstants.MEETING_BY_ID)
    @Operation(summary = "Get meeting by ID", description = "Get detailed information about a specific meeting")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<MeetingResponse> getMeetingById(@PathVariable Long meetingId) {
        MeetingResponse response = meetingService.getMeetingById(meetingId);
        return ApiResponse.success("Meeting retrieved successfully", response);
    }

    @PostMapping(ApiConstants.MEETING_START)
    @Operation(summary = "Start meeting", description = "Start a scheduled meeting")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingResponse> startMeeting(@PathVariable Long meetingId) {
        MeetingResponse response = meetingService.startMeeting(meetingId);
        return ApiResponse.success("Meeting started successfully", response);
    }

    @PostMapping(ApiConstants.MEETING_COMPLETE)
    @Operation(summary = "Complete meeting", description = "Mark a meeting as completed")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<MeetingResponse> completeMeeting(@PathVariable Long meetingId) {
        MeetingResponse response = meetingService.completeMeeting(meetingId);
        return ApiResponse.success("Meeting completed successfully", response);
    }

    // ==================== Lecturer Profile Image ====================

    @PostMapping(value = "/lecturer/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload lecturer profile image", description = "Lecturer uploads their profile image to S3")
    @PreAuthorize("hasAuthority('MEETING:MANAGE')")
    public ApiResponse<Map<String, String>> uploadLecturerProfileImage(@RequestParam("file") MultipartFile file) {
        Long currentUserId = securityService.getCurrentUserId();

        if (file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must not exceed 5MB");
        }

        AcademicStaff lecturer = academicStaffRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer", "id", currentUserId));

        String imageUrl = s3Service.uploadFilePublic(file, "meetings/lecturer-profiles/" + currentUserId);
        lecturer.setProfileImageUrl(imageUrl);
        academicStaffRepository.save(lecturer);

        log.info("Lecturer {} uploaded profile image", currentUserId);
        return ApiResponse.success("Profile image uploaded successfully", Map.of("imageUrl", imageUrl));
    }

    // ==================== Meeting Attachment Upload ====================

    @PostMapping(value = "/{meetingId}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload meeting attachment", description = "Upload an attachment for a meeting request")
    @PreAuthorize("hasAuthority('MEETING:READ')")
    public ApiResponse<Map<String, String>> uploadMeetingAttachment(
            @PathVariable Long meetingId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size must not exceed 10MB");
        }

        String attachmentUrl = s3Service.uploadFilePublic(file, "meetings/attachments/" + meetingId);
        meetingService.updateAttachment(meetingId, attachmentUrl, file.getOriginalFilename());

        log.info("Attachment uploaded for meeting {}", meetingId);
        return ApiResponse.success("Attachment uploaded successfully",
                Map.of("attachmentUrl", attachmentUrl, "fileName", file.getOriginalFilename()));
    }
}
