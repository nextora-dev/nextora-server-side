package lk.iit.nextora.module.kuppi.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.NoteDownloadResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Kuppi note operations
 */
public interface KuppiNoteService {

    // ==================== Normal Student Operations ====================

    /**
     * View all approved notes
     */
    PagedResponse<KuppiNoteResponse> getApprovedNotes(Pageable pageable);

    /**
     * View notes for a session
     */
    List<KuppiNoteResponse> getNotesForSession(Long sessionId);

    /**
     * View a specific note (increments view count)
     */
    KuppiNoteResponse getNoteById(Long noteId);

    /**
     * Download a note (increments download count if allowed)
     */
    KuppiNoteResponse downloadNote(Long noteId);

    /**
     * Search notes by title
     */
    PagedResponse<KuppiNoteResponse> searchNotes(String keyword, Pageable pageable);

    // ==================== Kuppi Student Operations ====================

    /**
     * Upload a new note with file to S3
     */
    KuppiNoteResponse uploadNoteWithFile(CreateKuppiNoteRequest request, MultipartFile file);

    /**
     * Download note file from S3
     */
    NoteDownloadResponse downloadNoteFile(Long noteId);

    /**
     * Update own note with new file to S3
     */
    KuppiNoteResponse updateNoteWithFile(Long noteId, UpdateKuppiNoteRequest request, MultipartFile file);

    /**
     * Delete own note (soft delete)
     */
    void deleteNote(Long noteId);

    /**
     * Get own uploaded notes
     */
    PagedResponse<KuppiNoteResponse> getMyNotes(Pageable pageable);

    // ==================== Admin Operations ====================

    /**
     * Edit any note (admin override)
     */
    KuppiNoteResponse adminUpdateNote(Long noteId, UpdateKuppiNoteRequest request);

    /**
     * Remove/delete any note
     */
    void adminDeleteNote(Long noteId);

    // ==================== Super Admin Operations ====================

    /**
     * Permanently delete a note
     */
    void permanentlyDeleteNote(Long noteId);
}

