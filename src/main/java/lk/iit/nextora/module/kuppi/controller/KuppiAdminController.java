package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.*;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Admin/Super Admin Kuppi operations
 * Handles content moderation, approval, and platform management
 */
@RestController
@RequestMapping(ApiConstants.KUPPI_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Kuppi Admin", description = "Admin endpoints for Kuppi management")
public class KuppiAdminController {

    private final KuppiSessionService sessionService;
    private final KuppiNoteService noteService;

    // ==================== Session Management ====================

    @PutMapping("/sessions/{sessionId}")
    @Operation(summary = "Edit session", description = "Edit any Kuppi session (admin override)")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_UPDATE')")
    public ApiResponse<KuppiSessionResponse> adminUpdateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateKuppiSessionRequest request) {
        KuppiSessionResponse response = sessionService.adminUpdateSession(sessionId, request);
        return ApiResponse.success("Session updated successfully", response);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Remove session", description = "Soft delete any Kuppi session")
    @PreAuthorize("hasAuthority('KUPPI:ADMIN_DELETE')")
    public ApiResponse<Void> adminDeleteSession(@PathVariable Long sessionId) {
        sessionService.adminDeleteSession(sessionId);
        return ApiResponse.success("Session removed successfully");
    }

    @DeleteMapping("/sessions/{sessionId}/permanent")
    @Operation(summary = "Permanently delete session", description = "Permanently delete a Kuppi session (Super Admin only)")
    @PreAuthorize("hasAuthority('KUPPI:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteSession(@PathVariable Long sessionId) {
        sessionService.permanentlyDeleteSession(sessionId);
        return ApiResponse.success("Session permanently deleted");
    }

    // ==================== Note Management ====================

    @PutMapping("/notes/{noteId}")
    @Operation(summary = "Edit note", description = "Edit any note (admin override)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:ADMIN_UPDATE')")
    public ApiResponse<KuppiNoteResponse> adminUpdateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateKuppiNoteRequest request) {
        KuppiNoteResponse response = noteService.adminUpdateNote(noteId, request);
        return ApiResponse.success("Note updated successfully", response);
    }

    @DeleteMapping("/notes/{noteId}")
    @Operation(summary = "Remove note", description = "Soft delete any note")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:ADMIN_DELETE')")
    public ApiResponse<Void> adminDeleteNote(@PathVariable Long noteId) {
        noteService.adminDeleteNote(noteId);
        return ApiResponse.success("Note removed successfully");
    }

    @DeleteMapping("/notes/{noteId}/permanent")
    @Operation(summary = "Permanently delete note", description = "Permanently delete a note (Super Admin only)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteNote(@PathVariable Long noteId) {
        noteService.permanentlyDeleteNote(noteId);
        return ApiResponse.success("Note permanently deleted");
    }

    // ==================== Platform Statistics ====================

    @GetMapping(ApiConstants.KUPPI_STATS)
    @Operation(summary = "Get platform statistics", description = "Get Kuppi platform usage statistics")
    @PreAuthorize("hasAuthority('KUPPI:VIEW_STATS')")
    public ApiResponse<KuppiPlatformStatsResponse> getPlatformStats() {
        KuppiPlatformStatsResponse response = sessionService.getPlatformStats();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }
}
