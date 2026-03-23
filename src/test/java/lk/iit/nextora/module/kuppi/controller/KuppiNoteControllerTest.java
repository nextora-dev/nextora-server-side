package lk.iit.nextora.module.kuppi.controller;

import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.NoteDownloadResponse;
import lk.iit.nextora.module.kuppi.service.KuppiNoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiNoteController Unit Tests")
class KuppiNoteControllerTest {

    @Mock private KuppiNoteService noteService;

    @InjectMocks
    private KuppiNoteController controller;

    // ============================================================
    // GET APPROVED NOTES
    // ============================================================

    @Nested
    @DisplayName("GET / (approved notes)")
    class GetApprovedNotesTests {

        @Test
        @DisplayName("Should return paginated approved notes")
        void getApprovedNotes_returnsPaginatedResult() {
            // Given
            PagedResponse<KuppiNoteResponse> paged = PagedResponse.<KuppiNoteResponse>builder()
                    .content(List.of(KuppiNoteResponse.builder().id(1L).build()))
                    .totalElements(1L).pageNumber(0).pageSize(10)
                    .totalPages(1).first(true).last(true).empty(false)
                    .build();
            when(noteService.getApprovedNotes(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiNoteResponse>> result = controller.getApprovedNotes(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getContent()).hasSize(1);
        }
    }

    // ============================================================
    // GET NOTES FOR SESSION
    // ============================================================

    @Nested
    @DisplayName("GET /session/{sessionId}")
    class GetNotesForSessionTests {

        @Test
        @DisplayName("Should return notes for a specific session")
        void getNotesForSession_returnsList() {
            // Given
            List<KuppiNoteResponse> notes = List.of(
                    KuppiNoteResponse.builder().id(1L).sessionId(5L).build());
            when(noteService.getNotesForSession(5L)).thenReturn(notes);

            // When
            ApiResponse<List<KuppiNoteResponse>> result = controller.getNotesForSession(5L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).hasSize(1);
        }
    }

    // ============================================================
    // GET NOTE BY ID
    // ============================================================

    @Nested
    @DisplayName("GET /{noteId}")
    class GetNoteByIdTests {

        @Test
        @DisplayName("Should return note by ID")
        void getNoteById_returnsNote() {
            // Given
            KuppiNoteResponse response = KuppiNoteResponse.builder().id(3L).title("Lecture Notes").build();
            when(noteService.getNoteById(3L)).thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.getNoteById(3L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTitle()).isEqualTo("Lecture Notes");
        }
    }

    // ============================================================
    // DOWNLOAD NOTE
    // ============================================================

    @Nested
    @DisplayName("GET /{noteId}/download")
    class DownloadNoteTests {

        @Test
        @DisplayName("Should download note and return response")
        void downloadNote_returnsNoteResponse() {
            // Given
            KuppiNoteResponse response = KuppiNoteResponse.builder().id(3L).downloadCount(1L).build();
            when(noteService.downloadNote(3L)).thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.downloadNote(3L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getDownloadCount()).isEqualTo(1L);
        }
    }

    // ============================================================
    // DOWNLOAD NOTE FILE
    // ============================================================

    @Nested
    @DisplayName("GET /{noteId}/download-file")
    class DownloadNoteFileTests {

        @Test
        @DisplayName("Should return file as byte array with proper headers")
        void downloadNoteFile_returnsFileContent() {
            // Given
            byte[] fileContent = "PDF content".getBytes();
            NoteDownloadResponse download = NoteDownloadResponse.builder()
                    .noteId(3L)
                    .fileName("lecture.pdf")
                    .contentType("application/pdf")
                    .fileSize((long) fileContent.length)
                    .fileContent(fileContent)
                    .build();
            when(noteService.downloadNoteFile(3L)).thenReturn(download);

            // When
            ResponseEntity<byte[]> result = controller.downloadNoteFile(3L);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(fileContent);
            assertThat(result.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
        }
    }

    // ============================================================
    // POST /upload - uploadNoteWithFile
    // ============================================================

    @Nested
    @DisplayName("POST /upload - uploadNoteWithFile")
    class UploadNoteWithFileTests {

        @Test
        @DisplayName("Should upload note with file and return response")
        void uploadNoteWithFile_returnsCreated() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "lecture.pdf", "application/pdf", "pdf content".getBytes());
            KuppiNoteResponse response = KuppiNoteResponse.builder()
                    .id(1L).title("Math Notes").fileName("lecture.pdf").build();
            when(noteService.uploadNoteWithFile(any(), any(MultipartFile.class))).thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.uploadNoteWithFile(
                    "Math Notes", "Notes for chapter 1", 5L, true, file);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTitle()).isEqualTo("Math Notes");
            assertThat(result.getMessage()).contains("uploaded");
            verify(noteService).uploadNoteWithFile(argThat(req ->
                    "Math Notes".equals(req.getTitle()) &&
                    req.getSessionId() == 5L &&
                    req.getAllowDownload()
            ), eq(file));
        }

        @Test
        @DisplayName("Should upload note without session ID")
        void uploadNoteWithFile_noSessionId_delegatesToService() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "notes.pdf", "application/pdf", "pdf".getBytes());
            KuppiNoteResponse response = KuppiNoteResponse.builder().id(1L).build();
            when(noteService.uploadNoteWithFile(any(), any(MultipartFile.class))).thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.uploadNoteWithFile(
                    "Notes", null, null, true, file);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(noteService).uploadNoteWithFile(argThat(req ->
                    req.getSessionId() == null
            ), eq(file));
        }
    }

    // ============================================================
    // PUT /{noteId}/upload - updateNoteWithFile
    // ============================================================

    @Nested
    @DisplayName("PUT /{noteId}/upload - updateNoteWithFile")
    class UpdateNoteWithFileTests {

        @Test
        @DisplayName("Should update note with new file")
        void updateNoteWithFile_returnsUpdated() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "updated.pdf", "application/pdf", "pdf".getBytes());
            KuppiNoteResponse response = KuppiNoteResponse.builder()
                    .id(3L).title("Updated Notes").build();
            when(noteService.updateNoteWithFile(eq(3L), any(), any(MultipartFile.class)))
                    .thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.updateNoteWithFile(
                    3L, "Updated Notes", "New description", false, file);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getTitle()).isEqualTo("Updated Notes");
        }

        @Test
        @DisplayName("Should update note without file")
        void updateNoteWithFile_noFile_delegatesToService() {
            // Given
            KuppiNoteResponse response = KuppiNoteResponse.builder()
                    .id(3L).title("Updated Title").build();
            when(noteService.updateNoteWithFile(eq(3L), any(), isNull())).thenReturn(response);

            // When
            ApiResponse<KuppiNoteResponse> result = controller.updateNoteWithFile(
                    3L, "Updated Title", null, null, null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(noteService).updateNoteWithFile(eq(3L), any(), isNull());
        }
    }

    // ============================================================
    // SEARCH NOTES
    // ============================================================

    @Nested
    @DisplayName("GET /search")
    class SearchNotesTests {

        @Test
        @DisplayName("Should search notes by keyword")
        void searchNotes_delegatesToService() {
            // Given
            PagedResponse<KuppiNoteResponse> paged = PagedResponse.<KuppiNoteResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(noteService.searchNotes(eq("math"), any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiNoteResponse>> result =
                    controller.searchNotes("math", 0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(noteService).searchNotes(eq("math"), any());
        }
    }

    // ============================================================
    // SOFT DELETE NOTE
    // ============================================================

    @Nested
    @DisplayName("DELETE /{noteId}")
    class SoftDeleteNoteTests {

        @Test
        @DisplayName("Should soft delete note and return success")
        void softDeleteNote_returnsSuccess() {
            // Given
            doNothing().when(noteService).softDeleteNote(3L);

            // When
            ApiResponse<Void> result = controller.softDeleteNote(3L);

            // Then
            assertThat(result.isSuccess()).isTrue();
            verify(noteService).softDeleteNote(3L);
        }
    }

    // ============================================================
    // GET MY NOTES
    // ============================================================

    @Nested
    @DisplayName("GET /my")
    class GetMyNotesTests {

        @Test
        @DisplayName("Should return current user's notes")
        void getMyNotes_delegatesToService() {
            // Given
            PagedResponse<KuppiNoteResponse> paged = PagedResponse.<KuppiNoteResponse>builder()
                    .content(List.of()).totalElements(0L).pageNumber(0).pageSize(10)
                    .totalPages(0).first(true).last(true).empty(true).build();
            when(noteService.getMyNotes(any())).thenReturn(paged);

            // When
            ApiResponse<PagedResponse<KuppiNoteResponse>> result = controller.getMyNotes(0, 10);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }
    }
}
