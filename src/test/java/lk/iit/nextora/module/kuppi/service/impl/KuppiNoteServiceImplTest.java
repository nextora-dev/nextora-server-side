package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.NoteDownloadResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.mapper.KuppiMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiNoteRepository;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiNoteServiceImpl Unit Tests")
class KuppiNoteServiceImplTest {

    @Mock private KuppiNoteRepository noteRepository;
    @Mock private KuppiSessionRepository sessionRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SecurityService securityService;
    @Mock private KuppiMapper kuppiMapper;
    @Mock private S3Service s3Service;

    @InjectMocks
    private KuppiNoteServiceImpl service;

    // ============================================================
    // getApprovedNotes
    // ============================================================

    @Nested
    @DisplayName("getApprovedNotes")
    class GetApprovedNotesTests {

        @Test
        @DisplayName("Should return paginated approved notes")
        void getApprovedNotes_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            KuppiNote note = KuppiNote.builder().id(1L).title("Math Notes").build();
            Page<KuppiNote> page = new PageImpl<>(List.of(note), pageable, 1);
            when(noteRepository.findByIsDeletedFalse(pageable)).thenReturn(page);
            when(kuppiMapper.toNoteResponseList(List.of(note)))
                    .thenReturn(List.of(KuppiNoteResponse.builder().id(1L).build()));

            // When
            PagedResponse<KuppiNoteResponse> result = service.getApprovedNotes(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }
    }

    // ============================================================
    // getNotesForSession
    // ============================================================

    @Nested
    @DisplayName("getNotesForSession")
    class GetNotesForSessionTests {

        @Test
        @DisplayName("Should return notes for a specific session")
        void getNotesForSession_returnsList() {
            // Given
            KuppiNote note = KuppiNote.builder().id(1L).build();
            when(noteRepository.findApprovedNotesBySession(5L)).thenReturn(List.of(note));
            when(kuppiMapper.toNoteResponseList(List.of(note)))
                    .thenReturn(List.of(KuppiNoteResponse.builder().id(1L).build()));

            // When
            List<KuppiNoteResponse> result = service.getNotesForSession(5L);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    // ============================================================
    // getNoteById
    // ============================================================

    @Nested
    @DisplayName("getNoteById")
    class GetNoteByIdTests {

        @Test
        @DisplayName("Should return note and increment view count")
        void getNoteById_incrementsViewCount() {
            // Given
            KuppiNote note = KuppiNote.builder().id(3L).viewCount(5L).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));
            when(noteRepository.save(note)).thenReturn(note);
            when(kuppiMapper.toResponse(note))
                    .thenReturn(KuppiNoteResponse.builder().id(3L).viewCount(6L).build());

            // When
            KuppiNoteResponse result = service.getNoteById(3L);

            // Then
            assertThat(note.getViewCount()).isEqualTo(6L);
            verify(noteRepository).save(note);
        }

        @Test
        @DisplayName("Should throw when note not found")
        void getNoteById_notFound_throws() {
            // Given
            when(noteRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.getNoteById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // downloadNote
    // ============================================================

    @Nested
    @DisplayName("downloadNote")
    class DownloadNoteTests {

        @Test
        @DisplayName("Should increment download count and return note")
        void downloadNote_incrementsCount() {
            // Given
            KuppiNote note = KuppiNote.builder().id(3L).allowDownload(true).downloadCount(0L).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));
            when(noteRepository.save(note)).thenReturn(note);
            when(kuppiMapper.toResponse(note))
                    .thenReturn(KuppiNoteResponse.builder().id(3L).downloadCount(1L).build());

            // When
            KuppiNoteResponse result = service.downloadNote(3L);

            // Then
            assertThat(note.getDownloadCount()).isEqualTo(1L);
            verify(noteRepository).save(note);
        }

        @Test
        @DisplayName("Should throw when download not allowed")
        void downloadNote_notAllowed_throwsBadRequest() {
            // Given
            KuppiNote note = KuppiNote.builder().id(3L).allowDownload(false).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            // When & Then
            assertThatThrownBy(() -> service.downloadNote(3L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not available for download");
        }
    }

    // ============================================================
    // searchNotes
    // ============================================================

    @Nested
    @DisplayName("searchNotes")
    class SearchNotesTests {

        @Test
        @DisplayName("Should search notes by keyword")
        void searchNotes_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiNote> page = new PageImpl<>(List.of(), pageable, 0);
            when(noteRepository.searchByTitle("math", pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiNoteResponse> result = service.searchNotes("math", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(noteRepository).searchByTitle("math", pageable);
        }
    }

    // ============================================================
    // uploadNoteWithFile
    // ============================================================

    @Nested
    @DisplayName("uploadNoteWithFile")
    class UploadNoteWithFileTests {

        @Test
        @DisplayName("Should upload note with PDF file successfully")
        void uploadNoteWithFile_pdf_savesCorrectly() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "notes.pdf", "application/pdf", "pdf content".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                    .title("Math Notes").description("Chapter 1").allowDownload(true).build();

            KuppiNote entity = KuppiNote.builder().id(1L).build();
            when(kuppiMapper.toEntity(request)).thenReturn(entity);
            when(s3Service.uploadFile(file, "kuppi-notes")).thenReturn("s3-key");
            when(s3Service.getPublicUrl("s3-key")).thenReturn("https://s3.url/notes.pdf");
            when(noteRepository.save(any())).thenReturn(entity);

            KuppiNoteResponse response = KuppiNoteResponse.builder().id(1L).build();
            when(kuppiMapper.toResponse(entity)).thenReturn(response);

            // When
            KuppiNoteResponse result = service.uploadNoteWithFile(request, file);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            verify(noteRepository).save(argThat(n ->
                    "https://s3.url/notes.pdf".equals(n.getFileUrl()) &&
                    "notes.pdf".equals(n.getFileName()) &&
                    "PDF".equals(n.getFileType())
            ));
        }

        @Test
        @DisplayName("Should set file type to SLIDES for PowerPoint")
        void uploadNoteWithFile_pptx_setsSlideType() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "slides.pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "content".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                    .title("Slides").build();

            KuppiNote entity = KuppiNote.builder().id(1L).build();
            when(kuppiMapper.toEntity(request)).thenReturn(entity);
            when(s3Service.uploadFile(any(), any())).thenReturn("key");
            when(s3Service.getPublicUrl("key")).thenReturn("url");
            when(noteRepository.save(any())).thenReturn(entity);
            when(kuppiMapper.toResponse(entity)).thenReturn(KuppiNoteResponse.builder().id(1L).build());

            // When
            service.uploadNoteWithFile(request, file);

            // Then
            verify(noteRepository).save(argThat(n -> "SLIDES".equals(n.getFileType())));
        }

        @Test
        @DisplayName("Should set file type to IMAGE for image files")
        void uploadNoteWithFile_image_setsImageType() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", "img".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                    .title("Photo").build();

            KuppiNote entity = KuppiNote.builder().id(1L).build();
            when(kuppiMapper.toEntity(request)).thenReturn(entity);
            when(s3Service.uploadFile(any(), any())).thenReturn("key");
            when(s3Service.getPublicUrl("key")).thenReturn("url");
            when(noteRepository.save(any())).thenReturn(entity);
            when(kuppiMapper.toResponse(entity)).thenReturn(KuppiNoteResponse.builder().id(1L).build());

            // When
            service.uploadNoteWithFile(request, file);

            // Then
            verify(noteRepository).save(argThat(n -> "IMAGE".equals(n.getFileType())));
        }

        @Test
        @DisplayName("Should throw when not a Kuppi Student")
        void uploadNoteWithFile_notKuppiStudent_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            MockMultipartFile file = new MockMultipartFile("f", "n.pdf", "application/pdf", "c".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder().title("T").build();

            // When & Then
            assertThatThrownBy(() -> service.uploadNoteWithFile(request, file))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Only Kuppi Students");
        }

        @Test
        @DisplayName("Should throw when file is null")
        void uploadNoteWithFile_nullFile_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder().title("T").build();

            // When & Then
            assertThatThrownBy(() -> service.uploadNoteWithFile(request, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("File is required");
        }

        @Test
        @DisplayName("Should throw when file type is invalid")
        void uploadNoteWithFile_invalidType_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            MockMultipartFile file = new MockMultipartFile("f", "file.exe", "application/octet-stream", "c".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder().title("T").build();

            // When & Then
            assertThatThrownBy(() -> service.uploadNoteWithFile(request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");
        }

        @Test
        @DisplayName("Should link note to session when sessionId provided")
        void uploadNoteWithFile_withSessionId_linksToSession() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            lenient().when(uploader.getId()).thenReturn(1L);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);
            KuppiSession session = KuppiSession.builder().id(5L).host(host).build();
            when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));

            MockMultipartFile file = new MockMultipartFile("f", "n.pdf", "application/pdf", "c".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                    .title("T").sessionId(5L).build();

            KuppiNote entity = KuppiNote.builder().id(1L).build();
            when(kuppiMapper.toEntity(request)).thenReturn(entity);
            when(s3Service.uploadFile(any(), any())).thenReturn("key");
            when(s3Service.getPublicUrl("key")).thenReturn("url");
            when(noteRepository.save(any())).thenReturn(entity);
            when(kuppiMapper.toResponse(entity)).thenReturn(KuppiNoteResponse.builder().id(1L).build());

            // When
            service.uploadNoteWithFile(request, file);

            // Then
            verify(noteRepository).save(argThat(n -> n.getSession() == session));
        }

        @Test
        @DisplayName("Should throw when adding note to another user's session")
        void uploadNoteWithFile_notSessionOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(uploader));

            Student otherHost = mock(Student.class);
            when(otherHost.getId()).thenReturn(99L);
            KuppiSession session = KuppiSession.builder().id(5L).host(otherHost).build();
            when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));

            MockMultipartFile file = new MockMultipartFile("f", "n.pdf", "application/pdf", "c".getBytes());
            CreateKuppiNoteRequest request = CreateKuppiNoteRequest.builder()
                    .title("T").sessionId(5L).build();

            KuppiNote entity = KuppiNote.builder().id(1L).build();
            when(kuppiMapper.toEntity(request)).thenReturn(entity);
            when(s3Service.uploadFile(any(), any())).thenReturn("key");
            when(s3Service.getPublicUrl("key")).thenReturn("url");

            // When & Then
            assertThatThrownBy(() -> service.uploadNoteWithFile(request, file))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("your own sessions");
        }
    }

    // ============================================================
    // downloadNoteFile
    // ============================================================

    @Nested
    @DisplayName("downloadNoteFile")
    class DownloadNoteFileTests {

        @Test
        @DisplayName("Should download file and return NoteDownloadResponse")
        void downloadNoteFile_returnsFileContent() {
            // Given
            KuppiNote note = KuppiNote.builder()
                    .id(3L).allowDownload(true).downloadCount(0L)
                    .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/kuppi-notes/file.pdf")
                    .fileName("file.pdf").fileSize(1024L).fileType("PDF").build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));
            when(s3Service.getBucketName()).thenReturn("bucket");
            when(s3Service.fileExists("kuppi-notes/file.pdf")).thenReturn(true);
            byte[] content = "PDF content".getBytes();
            when(s3Service.downloadFile("kuppi-notes/file.pdf")).thenReturn(content);
            when(noteRepository.save(note)).thenReturn(note);

            // When
            NoteDownloadResponse result = service.downloadNoteFile(3L);

            // Then
            assertThat(result.getFileContent()).isEqualTo(content);
            assertThat(result.getFileName()).isEqualTo("file.pdf");
            assertThat(result.getContentType()).isEqualTo("application/pdf");
            assertThat(note.getDownloadCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when download not allowed")
        void downloadNoteFile_notAllowed_throwsBadRequest() {
            // Given
            KuppiNote note = KuppiNote.builder().id(3L).allowDownload(false).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            // When & Then
            assertThatThrownBy(() -> service.downloadNoteFile(3L))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should throw when file URL is empty")
        void downloadNoteFile_emptyUrl_throws() {
            // Given
            KuppiNote note = KuppiNote.builder().id(3L).allowDownload(true).fileUrl("").build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            // When & Then
            assertThatThrownBy(() -> service.downloadNoteFile(3L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when file not found in S3")
        void downloadNoteFile_notInS3_throws() {
            // Given
            KuppiNote note = KuppiNote.builder()
                    .id(3L).allowDownload(true).downloadCount(0L)
                    .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/key").build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));
            when(s3Service.getBucketName()).thenReturn("bucket");
            when(s3Service.fileExists("key")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> service.downloadNoteFile(3L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found in storage");
        }
    }

    // ============================================================
    // updateNoteWithFile
    // ============================================================

    @Nested
    @DisplayName("updateNoteWithFile")
    class UpdateNoteWithFileTests {

        @Test
        @DisplayName("Should update note with new file replacing old one")
        void updateNoteWithFile_withFile_replacesOld() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.getId()).thenReturn(1L);

            KuppiNote note = KuppiNote.builder()
                    .id(3L).uploadedBy(uploader)
                    .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/old-key")
                    .build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            MockMultipartFile newFile = new MockMultipartFile(
                    "file", "new.pdf", "application/pdf", "new content".getBytes());
            UpdateKuppiNoteRequest request = UpdateKuppiNoteRequest.builder()
                    .title("Updated Title").build();

            when(s3Service.getBucketName()).thenReturn("bucket");
            when(s3Service.fileExists("old-key")).thenReturn(true);
            when(s3Service.uploadFile(newFile, "kuppi-notes")).thenReturn("new-key");
            when(s3Service.getPublicUrl("new-key")).thenReturn("https://s3/new.pdf");
            when(noteRepository.save(any())).thenReturn(note);
            when(kuppiMapper.toResponse(note)).thenReturn(KuppiNoteResponse.builder().id(3L).build());

            // When
            KuppiNoteResponse result = service.updateNoteWithFile(3L, request, newFile);

            // Then
            verify(s3Service).deleteFile("old-key");
            assertThat(note.getFileUrl()).isEqualTo("https://s3/new.pdf");
            assertThat(note.getFileName()).isEqualTo("new.pdf");
            assertThat(note.getFileType()).isEqualTo("PDF");
        }

        @Test
        @DisplayName("Should update metadata only when no file provided")
        void updateNoteWithFile_noFile_updatesMetadataOnly() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.getId()).thenReturn(1L);

            KuppiNote note = KuppiNote.builder()
                    .id(3L).uploadedBy(uploader).fileUrl("existing-url").build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            UpdateKuppiNoteRequest request = UpdateKuppiNoteRequest.builder()
                    .title("Updated Title").build();

            when(noteRepository.save(any())).thenReturn(note);
            when(kuppiMapper.toResponse(note)).thenReturn(KuppiNoteResponse.builder().id(3L).build());

            // When
            service.updateNoteWithFile(3L, request, null);

            // Then
            verify(s3Service, never()).uploadFile(any(), any());
            verify(kuppiMapper).updateNoteFromRequest(request, note);
        }

        @Test
        @DisplayName("Should throw when not note owner")
        void updateNoteWithFile_notOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student otherUploader = mock(Student.class);
            when(otherUploader.getId()).thenReturn(99L);

            KuppiNote note = KuppiNote.builder().id(3L).uploadedBy(otherUploader).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            UpdateKuppiNoteRequest request = UpdateKuppiNoteRequest.builder().title("T").build();

            // When & Then
            assertThatThrownBy(() -> service.updateNoteWithFile(3L, request, null))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("your own notes");
        }

        @Test
        @DisplayName("Should throw for invalid file type on update")
        void updateNoteWithFile_invalidFileType_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.getId()).thenReturn(1L);

            KuppiNote note = KuppiNote.builder().id(3L).uploadedBy(uploader).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            MockMultipartFile file = new MockMultipartFile("f", "bad.exe", "application/octet-stream", "c".getBytes());
            UpdateKuppiNoteRequest request = UpdateKuppiNoteRequest.builder().build();

            // When & Then
            assertThatThrownBy(() -> service.updateNoteWithFile(3L, request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");
        }
    }

    // ============================================================
    // softDeleteNote
    // ============================================================

    @Nested
    @DisplayName("softDeleteNote")
    class SoftDeleteNoteTests {

        @Test
        @DisplayName("Should soft delete note and cleanup S3 file")
        void softDeleteNote_deletesFileAndSoftDeletes() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student uploader = mock(Student.class);
            when(uploader.getId()).thenReturn(1L);

            KuppiNote note = KuppiNote.builder()
                    .id(3L).uploadedBy(uploader)
                    .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/some-key")
                    .build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));
            when(s3Service.getBucketName()).thenReturn("bucket");
            when(s3Service.fileExists("some-key")).thenReturn(true);

            // When
            service.softDeleteNote(3L);

            // Then
            assertThat(note.getIsDeleted()).isTrue();
            verify(s3Service).deleteFile("some-key");
            verify(noteRepository).save(note);
        }

        @Test
        @DisplayName("Should throw when not note owner")
        void softDeleteNote_notOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student otherUploader = mock(Student.class);
            when(otherUploader.getId()).thenReturn(99L);

            KuppiNote note = KuppiNote.builder().id(3L).uploadedBy(otherUploader).build();
            when(noteRepository.findByIdWithDetails(3L)).thenReturn(Optional.of(note));

            // When & Then
            assertThatThrownBy(() -> service.softDeleteNote(3L))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ============================================================
    // permanentlyDeleteNote
    // ============================================================

    @Nested
    @DisplayName("permanentlyDeleteNote")
    class PermanentlyDeleteNoteTests {

        @Test
        @DisplayName("Should permanently delete note and cleanup S3")
        void permanentlyDeleteNote_deletesFromDbAndS3() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            KuppiNote note = KuppiNote.builder()
                    .id(3L).fileUrl("https://bucket.s3.us-east-1.amazonaws.com/key").build();
            when(noteRepository.findById(3L)).thenReturn(Optional.of(note));
            when(s3Service.getBucketName()).thenReturn("bucket");
            when(s3Service.fileExists("key")).thenReturn(true);

            // When
            service.permanentlyDeleteNote(3L);

            // Then
            verify(s3Service).deleteFile("key");
            verify(noteRepository).delete(note);
        }

        @Test
        @DisplayName("Should throw when note not found")
        void permanentlyDeleteNote_notFound_throws() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.permanentlyDeleteNote(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // getMyNotes
    // ============================================================

    @Nested
    @DisplayName("getMyNotes")
    class GetMyNotesTests {

        @Test
        @DisplayName("Should return current user's notes")
        void getMyNotes_returnsPaged() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiNote> page = new PageImpl<>(List.of(), pageable, 0);
            when(noteRepository.findByUploadedByIdAndIsDeletedFalse(1L, pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiNoteResponse> result = service.getMyNotes(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(noteRepository).findByUploadedByIdAndIsDeletedFalse(1L, pageable);
        }
    }
}
