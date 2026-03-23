package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.*;
import lk.iit.nextora.module.kuppi.dto.response.*;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Controller for Kuppi session operations
 * Handles endpoints for Normal Students and Kuppi Students
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.KUPPI_SESSIONS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Sessions", description = "Kuppi session management endpoints")
public class KuppiSessionController {

    private final KuppiSessionService sessionService;

    // ==================== Normal Student Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all sessions", description = "View all approved Kuppi sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getPublicSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledStartTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<KuppiSessionResponse> response = sessionService.getPublicSessions(pageable);
        return ApiResponse.success("Sessions retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SESSION_BY_ID)
    @Operation(summary = "Get session by ID", description = "View a specific Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiSessionResponse> getSessionById(@PathVariable Long sessionId) {
        KuppiSessionResponse response = sessionService.getSessionById(sessionId);
        return ApiResponse.success("Session retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH)
    @Operation(summary = "Search sessions", description = "Search sessions by keyword")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchSessions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchSessions(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_SUBJECT)
    @Operation(summary = "Search by subject", description = "Search sessions by subject")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchBySubject(
            @RequestParam String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchBySubject(subject, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_HOST)
    @Operation(summary = "Search by host", description = "Search sessions by host/lecturer name")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchByHostName(
            @RequestParam String hostName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchByHostName(hostName, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH_DATE)
    @Operation(summary = "Search by date range", description = "Search sessions by date range")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.searchByDateRange(startDate, endDate, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_UPCOMING)
    @Operation(summary = "Get upcoming sessions", description = "Get all upcoming Kuppi sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getUpcomingSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.getUpcomingSessions(pageable);
        return ApiResponse.success("Upcoming sessions retrieved successfully", response);
    }

    // ==================== Kuppi Student Endpoints ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create session with file(s)", description = "Create a new Kuppi session with one or more uploaded files (Kuppi Students only)")
    @PreAuthorize("hasAuthority('KUPPI:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KuppiSessionResponse> createSession(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "subject") String subject,
            @RequestParam(value = "scheduledStartTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledStartTime,
            @RequestParam(value = "scheduledEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledEndTime,
            @RequestParam(value = "liveLink") String liveLink,
            @RequestParam(value = "meetingPlatform", required = false) String meetingPlatform,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "files[]", required = false) MultipartFile[] filesArray,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        // Normalize files when client uses files[] naming
        int filesCount = (files != null) ? files.length : 0;
        int filesArrayCount = (filesArray != null) ? filesArray.length : 0;
        log.info("createSessionwithFile called: files.length={}, files[][].length={}, singleFilePresent={}", filesCount, filesArrayCount, (file != null && !file.isEmpty()));
        if ((files == null || files.length == 0) && filesArray != null && filesArray.length > 0) {
            files = filesArray;
        }
        // normalize single file to files array if needed
        if ((files == null || files.length == 0) && file != null && !file.isEmpty()) {
            files = new MultipartFile[]{file};
        }

        CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                .title(title)
                .description(description)
                .subject(subject)
                .scheduledStartTime(scheduledStartTime)
                .scheduledEndTime(scheduledEndTime)
                .liveLink(liveLink)
                .meetingPlatform(meetingPlatform)
                .build();

        KuppiSessionResponse response = sessionService.createSession(request, files);
        return ApiResponse.success("Session created successfully", response);
    }

    @PutMapping(path = ApiConstants.KUPPI_SESSION_BY_ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update session with file(s)", description = "Update an existing Kuppi session and optionally replace/upload one or more files")
    @PreAuthorize("hasAuthority('KUPPI:UPDATE')")
    public ApiResponse<KuppiSessionResponse> updateSession(
            @PathVariable Long sessionId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "scheduledStartTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledStartTime,
            @RequestParam(value = "scheduledEndTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledEndTime,
            @RequestParam(value = "liveLink", required = false) String liveLink,
            @RequestParam(value = "meetingPlatform", required = false) String meetingPlatform,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "files[]", required = false) MultipartFile[] filesArray,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeNoteIds", required = false) java.util.List<Long> removeNoteIds) {

        int filesCountUp = (files != null) ? files.length : 0;
        int filesArrayCountUp = (filesArray != null) ? filesArray.length : 0;
        log.info("updateSessionWithFile called for session {}: files.length={}, files[][].length={}, singleFilePresent={}, removeNoteIds={}", sessionId, filesCountUp, filesArrayCountUp, (file != null && !file.isEmpty()), removeNoteIds);
        // Normalize files when client uses files[] naming
        if ((files == null || files.length == 0) && filesArray != null && filesArray.length > 0) {
            files = filesArray;
        }
        if ((files == null || files.length == 0) && file != null && !file.isEmpty()) {
            files = new MultipartFile[]{file};
        }

        UpdateKuppiSessionRequest request = UpdateKuppiSessionRequest.builder()
                .title(title)
                .description(description)
                .subject(subject)
                .scheduledStartTime(scheduledStartTime)
                .scheduledEndTime(scheduledEndTime)
                .liveLink(liveLink)
                .meetingPlatform(meetingPlatform)
                .build();

        KuppiSessionResponse response = sessionService.updateSession(sessionId, request, files, removeNoteIds);
        return ApiResponse.success("Session updated successfully", response);
    }

    @PostMapping(ApiConstants.KUPPI_CANCEL)
    @Operation(summary = "Cancel session", description = "Cancel own Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:CANCEL')")
    public ApiResponse<Void> cancelSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        sessionService.cancelSession(sessionId, reason);
        return ApiResponse.success("Session cancelled successfully");
    }

    @PostMapping(ApiConstants.KUPPI_RESCHEDULE)
    @Operation(summary = "Reschedule session", description = "Reschedule own Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:RESCHEDULE')")
    public ApiResponse<KuppiSessionResponse> rescheduleSession(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndTime) {
        KuppiSessionResponse response = sessionService.rescheduleSession(sessionId, newStartTime, newEndTime);
        return ApiResponse.success("Session rescheduled successfully", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_SESSION_BY_ID)
    @Operation(summary = "Soft delete session", description = "Soft-delete the session and remove associated note files from storage (owner only)")
    @PreAuthorize("hasAuthority('KUPPI:DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> softDeleteSession(@PathVariable Long sessionId) {
        sessionService.softDeleteSession(sessionId);
        return ApiResponse.success("Session soft-deleted and files removed successfully");
    }

    @GetMapping(ApiConstants.KUPPI_MY)
    @Operation(summary = "Get my sessions", description = "Get own created sessions")
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiSessionResponse>> getMySessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiSessionResponse> response = sessionService.getMySessions(pageable);
        return ApiResponse.success("Your sessions retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_ANALYTICS)
    @Operation(summary = "Get my analytics", description = "Get analytics for own sessions and notes")
    @PreAuthorize("hasAuthority('KUPPI:VIEW_ANALYTICS')")
    public ApiResponse<KuppiAnalyticsResponse> getMyAnalytics() {
        KuppiAnalyticsResponse response = sessionService.getMyAnalytics();
        return ApiResponse.success("Analytics retrieved successfully", response);
    }
}
