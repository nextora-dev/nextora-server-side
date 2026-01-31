package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.NoteDownloadResponse;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for Kuppi note operations
 * Handles endpoints for Normal Students and Kuppi Students
 */
@RestController
@RequestMapping(ApiConstants.KUPPI_NOTES)
@RequiredArgsConstructor
@Tag(name = "Kuppi Notes", description = "Kuppi notes management endpoints")
public class KuppiNoteController {

    private final KuppiNoteService noteService;

    // ==================== Normal Student Endpoints ====================

    @GetMapping
    @Operation(summary = "Get all notes", description = "View all notes")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:READ')")
    public ApiResponse<PagedResponse<KuppiNoteResponse>> getApprovedNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiNoteResponse> response = noteService.getApprovedNotes(pageable);
        return ApiResponse.success("Notes retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_NOTE_SESSION)
    @Operation(summary = "Get notes for session", description = "View notes for a specific session")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:READ')")
    public ApiResponse<List<KuppiNoteResponse>> getNotesForSession(@PathVariable Long sessionId) {
        List<KuppiNoteResponse> response = noteService.getNotesForSession(sessionId);
        return ApiResponse.success("Notes retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_NOTE_BY_ID)
    @Operation(summary = "Get note by ID", description = "View a specific note")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:READ')")
    public ApiResponse<KuppiNoteResponse> getNoteById(@PathVariable Long noteId) {
        KuppiNoteResponse response = noteService.getNoteById(noteId);
        return ApiResponse.success("Note retrieved successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_DOWNLOAD)
    @Operation(summary = "Download note", description = "Download a note (if allowed)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:DOWNLOAD')")
    public ApiResponse<KuppiNoteResponse> downloadNote(@PathVariable Long noteId) {
        KuppiNoteResponse response = noteService.downloadNote(noteId);
        return ApiResponse.success("Note download recorded", response);
    }

    @GetMapping(ApiConstants.KUPPI_SEARCH)
    @Operation(summary = "Search notes", description = "Search notes by keyword")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:SEARCH')")
    public ApiResponse<PagedResponse<KuppiNoteResponse>> searchNotes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiNoteResponse> response = noteService.searchNotes(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    // ==================== Kuppi Student Endpoints ====================

    @PostMapping(value = ApiConstants.KUPPI_NOTE_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload note with file", description = "Upload a new note with file to S3 (Kuppi Students only)")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KuppiNoteResponse> uploadNoteWithFile(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "allowDownload", defaultValue = "true") Boolean allowDownload,
            @RequestParam("file") MultipartFile file) {

        CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                .title(title)
                .description(description)
                .sessionId(sessionId)
                .allowDownload(allowDownload)
                .build();

        KuppiNoteResponse response = noteService.uploadNoteWithFile(request, file);
        return ApiResponse.success("Note with file uploaded successfully to S3", response);
    }

    @GetMapping(ApiConstants.KUPPI_NOTE_DOWNLOAD_FILE)
    @Operation(summary = "Download note file", description = "Download the actual note file from S3")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:DOWNLOAD')")
    public ResponseEntity<byte[]> downloadNoteFile(@PathVariable Long noteId) {
        NoteDownloadResponse download = noteService.downloadNoteFile(noteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(download.getContentType()));
        headers.setContentDispositionFormData("attachment", download.getFileName());
        headers.setContentLength(download.getFileContent().length);

        return new ResponseEntity<>(download.getFileContent(), headers, HttpStatus.OK);
    }

    @PutMapping(value = ApiConstants.KUPPI_NOTE_UPDATE_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update note with file", description = "Update own note with new file to S3")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:UPDATE')")
    public ApiResponse<KuppiNoteResponse> updateNoteWithFile(
            @PathVariable Long noteId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "allowDownload", required = false) Boolean allowDownload,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        UpdateKuppiNoteRequest request = UpdateKuppiNoteRequest.builder()
                .title(title)
                .description(description)
                .allowDownload(allowDownload)
                .build();

        KuppiNoteResponse response = noteService.updateNoteWithFile(noteId, request, file);
        return ApiResponse.success("Note updated successfully", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_NOTE_BY_ID)
    @Operation(summary = "Delete note", description = "Delete own note")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:DELETE')")
    public ApiResponse<Void> deleteNote(@PathVariable Long noteId) {
        noteService.deleteNote(noteId);
        return ApiResponse.success("Note deleted successfully");
    }

    @GetMapping(ApiConstants.KUPPI_MY)
    @Operation(summary = "Get my notes", description = "Get own uploaded notes")
    @PreAuthorize("hasAuthority('KUPPI_NOTE:READ')")
    public ApiResponse<PagedResponse<KuppiNoteResponse>> getMyNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiNoteResponse> response = noteService.getMyNotes(pageable);
        return ApiResponse.success("Your notes retrieved successfully", response);
    }
}

