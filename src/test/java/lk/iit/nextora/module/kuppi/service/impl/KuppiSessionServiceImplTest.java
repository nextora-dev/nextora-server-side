package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.infrastructure.notification.service.KuppiNotificationService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiAnalyticsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiPlatformStatsResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.mapper.KuppiMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiNoteRepository;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import lk.iit.nextora.module.kuppi.service.KuppiSessionService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiSessionServiceImpl Unit Tests")
class KuppiSessionServiceImplTest {

    @Mock private KuppiSessionRepository sessionRepository;
    @Mock private KuppiNoteRepository noteRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SecurityService securityService;
    @Mock private KuppiMapper kuppiMapper;
    @Mock private KuppiNotificationService kuppiNotificationService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private KuppiSessionServiceImpl service;

    // ============================================================
    // getPublicSessions
    // ============================================================

    @Nested
    @DisplayName("getPublicSessions")
    class GetPublicSessionsTests {

        @Test
        @DisplayName("Should return paginated public sessions")
        void getPublicSessions_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            KuppiSession session = KuppiSession.builder().id(1L).title("Math").build();
            Page<KuppiSession> page = new PageImpl<>(List.of(session), pageable, 1);
            when(sessionRepository.findByStatusInAndIsDeletedFalse(anyList(), eq(pageable)))
                    .thenReturn(page);
            when(kuppiMapper.toResponseList(List.of(session)))
                    .thenReturn(List.of(KuppiSessionResponse.builder().id(1L).build()));

            // When
            PagedResponse<KuppiSessionResponse> result = service.getPublicSessions(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }
    }

    // ============================================================
    // getSessionById
    // ============================================================

    @Nested
    @DisplayName("getSessionById")
    class GetSessionByIdTests {

        @Test
        @DisplayName("Should return session and increment view count")
        void getSessionById_incrementsViewCount() {
            // Given
            KuppiSession session = KuppiSession.builder().id(5L).viewCount(10L).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));
            when(sessionRepository.save(session)).thenReturn(session);
            when(kuppiMapper.toResponse(session))
                    .thenReturn(KuppiSessionResponse.builder().id(5L).build());

            // When
            KuppiSessionResponse result = service.getSessionById(5L);

            // Then
            assertThat(session.getViewCount()).isEqualTo(11L);
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("Should throw when session not found")
        void getSessionById_notFound_throws() {
            // Given
            when(sessionRepository.findByIdWithHost(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.getSessionById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // searchSessions, searchBySubject, searchByHostName, searchByDateRange
    // ============================================================

    @Nested
    @DisplayName("Search Operations")
    class SearchTests {

        @Test
        @DisplayName("searchSessions should delegate to repository")
        void searchSessions_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.searchByKeyword(eq("math"), anyList(), eq(pageable)))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result = service.searchSessions("math", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("searchBySubject should delegate to repository")
        void searchBySubject_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.searchBySubject(eq("Physics"), anyList(), eq(pageable)))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result = service.searchBySubject("Physics", pageable);

            // Then
            verify(sessionRepository).searchBySubject(eq("Physics"), anyList(), eq(pageable));
        }

        @Test
        @DisplayName("searchByHostName should delegate to repository")
        void searchByHostName_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.searchByHostName(eq("John"), anyList(), eq(pageable)))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result = service.searchByHostName("John", pageable);

            // Then
            verify(sessionRepository).searchByHostName(eq("John"), anyList(), eq(pageable));
        }

        @Test
        @DisplayName("searchByDateRange should delegate to repository")
        void searchByDateRange_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.findByDateRange(eq(start), eq(end), anyList(), eq(pageable)))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result =
                    service.searchByDateRange(start, end, pageable);

            // Then
            verify(sessionRepository).findByDateRange(eq(start), eq(end), anyList(), eq(pageable));
        }

        @Test
        @DisplayName("getUpcomingSessions should delegate to repository")
        void getUpcomingSessions_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.findUpcomingSessions(any(), anyList(), eq(pageable)))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result = service.getUpcomingSessions(pageable);

            // Then
            verify(sessionRepository).findUpcomingSessions(any(), anyList(), eq(pageable));
        }
    }

    // ============================================================
    // createSession
    // ============================================================

    @Nested
    @DisplayName("createSession")
    class CreateSessionTests {

        @Test
        @DisplayName("Should create session without files successfully")
        void createSession_noFiles_createsSession() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(true);
            when(host.getFirstName()).thenReturn("John");
            when(host.getLastName()).thenReturn("Doe");
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("Math Kuppi").subject("Math")
                    .scheduledStartTime(start).scheduledEndTime(end)
                    .liveLink("https://meet.google.com/abc").build();

            KuppiSession session = KuppiSession.builder()
                    .id(1L).title("Math Kuppi").viewCount(0L)
                    .notes(new HashSet<>()).build();
            when(kuppiMapper.toEntity(request)).thenReturn(session);
            when(sessionRepository.save(any())).thenReturn(session);
            KuppiSessionResponse response = KuppiSessionResponse.builder().id(1L).build();
            when(kuppiMapper.toResponse(session)).thenReturn(response);

            // When
            KuppiSessionResponse result = service.createSession(request, null);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(session.getHost()).isEqualTo(host);
            assertThat(session.getStatus()).isEqualTo(KuppiSessionStatus.SCHEDULED);
            verify(kuppiNotificationService).notifyNewKuppiSession(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should create session with file uploads")
        void createSession_withFiles_uploadsAndCreatesNotes() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(true);
            when(host.getFirstName()).thenReturn("John");
            when(host.getLastName()).thenReturn("Doe");
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("Session").subject("Math")
                    .scheduledStartTime(start).scheduledEndTime(end)
                    .liveLink("link").build();

            KuppiSession session = KuppiSession.builder()
                    .id(1L).title("Session").viewCount(0L)
                    .notes(new HashSet<>()).build();
            when(kuppiMapper.toEntity(request)).thenReturn(session);
            when(sessionRepository.save(any())).thenReturn(session);

            MultipartFile[] files = {
                    new MockMultipartFile("f", "notes.pdf", "application/pdf", "pdf".getBytes())
            };

            when(s3Service.uploadFile(any(), eq("kuppi-sessions"))).thenReturn("s3-key");
            when(s3Service.getPublicUrl("s3-key")).thenReturn("https://s3/notes.pdf");

            KuppiNote savedNote = KuppiNote.builder().id(10L).build();
            when(noteRepository.save(any(KuppiNote.class))).thenReturn(savedNote);

            KuppiSessionResponse response = KuppiSessionResponse.builder().id(1L).build();
            when(kuppiMapper.toResponse(session)).thenReturn(response);

            // When
            KuppiSessionResponse result = service.createSession(request, files);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            verify(s3Service).uploadFile(any(), eq("kuppi-sessions"));
            verify(noteRepository).save(any(KuppiNote.class));
        }

        @Test
        @DisplayName("Should throw when not a Kuppi Student")
        void createSession_notKuppiStudent_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("T").subject("S")
                    .scheduledStartTime(LocalDateTime.now().plusDays(1))
                    .scheduledEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                    .build();

            // When & Then
            assertThatThrownBy(() -> service.createSession(request, null))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Only Kuppi Students");
        }

        @Test
        @DisplayName("Should throw when start time is after end time")
        void createSession_invalidSchedule_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            LocalDateTime start = LocalDateTime.now().plusDays(2);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("T").subject("S")
                    .scheduledStartTime(start).scheduledEndTime(end).build();

            // When & Then
            assertThatThrownBy(() -> service.createSession(request, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("before end time");
        }

        @Test
        @DisplayName("Should throw when start time is in the past")
        void createSession_pastStartTime_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);
            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("T").subject("S")
                    .scheduledStartTime(start).scheduledEndTime(end).build();

            // When & Then
            assertThatThrownBy(() -> service.createSession(request, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("Should throw for invalid file type and cleanup uploaded files")
        void createSession_invalidFileType_throwsAndCleansUp() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(host));

            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            CreateKuppiSessionRequest request = CreateKuppiSessionRequest.builder()
                    .title("T").subject("S")
                    .scheduledStartTime(start).scheduledEndTime(end).build();

            KuppiSession session = KuppiSession.builder()
                    .id(1L).viewCount(0L).notes(new HashSet<>()).build();
            when(kuppiMapper.toEntity(request)).thenReturn(session);

            MultipartFile[] files = {
                    new MockMultipartFile("f", "bad.exe", "application/octet-stream", "c".getBytes())
            };

            // When & Then
            assertThatThrownBy(() -> service.createSession(request, files))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");
        }
    }

    // ============================================================
    // cancelSession
    // ============================================================

    @Nested
    @DisplayName("cancelSession")
    class CancelSessionTests {

        @Test
        @DisplayName("Should cancel session and send notification")
        void cancelSession_validOwner_cancels() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).title("Session")
                    .subject("Math").status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When
            service.cancelSession(5L, "Schedule conflict");

            // Then
            assertThat(session.getStatus()).isEqualTo(KuppiSessionStatus.CANCELLED);
            assertThat(session.getCancellationReason()).isEqualTo("Schedule conflict");
            verify(sessionRepository).save(session);
            verify(kuppiNotificationService).notifyKuppiSessionCancelled(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw when not session owner")
        void cancelSession_notOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student otherHost = mock(Student.class);
            when(otherHost.getId()).thenReturn(99L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(otherHost).status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.cancelSession(5L, "reason"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Should throw when session already cancelled")
        void cancelSession_alreadyCancelled_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).status(KuppiSessionStatus.CANCELLED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.cancelSession(5L, "reason"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already cancelled");
        }

        @Test
        @DisplayName("Should throw when session is completed")
        void cancelSession_completed_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).status(KuppiSessionStatus.COMPLETED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.cancelSession(5L, "reason"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("completed session");
        }
    }

    // ============================================================
    // rescheduleSession
    // ============================================================

    @Nested
    @DisplayName("rescheduleSession")
    class RescheduleSessionTests {

        @Test
        @DisplayName("Should reschedule session and send notification")
        void rescheduleSession_validOwner_reschedules() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).title("Session")
                    .subject("Math").status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            LocalDateTime newStart = LocalDateTime.now().plusDays(2);
            LocalDateTime newEnd = newStart.plusHours(2);
            when(sessionRepository.save(session)).thenReturn(session);
            when(kuppiMapper.toResponse(session))
                    .thenReturn(KuppiSessionResponse.builder().id(5L).build());

            // When
            KuppiSessionResponse result = service.rescheduleSession(5L, newStart, newEnd);

            // Then
            assertThat(session.getScheduledStartTime()).isEqualTo(newStart);
            assertThat(session.getScheduledEndTime()).isEqualTo(newEnd);
            verify(kuppiNotificationService).notifyKuppiSessionRescheduled(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw when rescheduling cancelled session")
        void rescheduleSession_cancelled_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).status(KuppiSessionStatus.CANCELLED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.rescheduleSession(5L,
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cancelled");
        }
    }

    // ============================================================
    // getMySessions
    // ============================================================

    @Nested
    @DisplayName("getMySessions")
    class GetMySessionsTests {

        @Test
        @DisplayName("Should return current user's sessions")
        void getMySessions_returnsPaged() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiSession> page = new PageImpl<>(List.of(), pageable, 0);
            when(sessionRepository.findByHostIdAndIsDeletedFalse(1L, pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiSessionResponse> result = service.getMySessions(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    // ============================================================
    // getMyAnalytics
    // ============================================================

    @Nested
    @DisplayName("getMyAnalytics")
    class GetMyAnalyticsTests {

        @Test
        @DisplayName("Should return aggregated analytics")
        void getMyAnalytics_returnsAggregated() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);

            KuppiSession completed = KuppiSession.builder()
                    .id(1L).status(KuppiSessionStatus.COMPLETED).viewCount(50L)
                    .scheduledStartTime(LocalDateTime.now().minusDays(1)).build();
            KuppiSession upcoming = KuppiSession.builder()
                    .id(2L).status(KuppiSessionStatus.SCHEDULED).viewCount(10L)
                    .scheduledStartTime(LocalDateTime.now().plusDays(1)).build();

            when(sessionRepository.findByHostIdAndIsDeletedFalse(1L))
                    .thenReturn(List.of(completed, upcoming));
            when(noteRepository.getTotalViewsByUploader(1L)).thenReturn(100L);
            when(noteRepository.countByUploadedByIdAndIsDeletedFalse(1L)).thenReturn(5L);

            // When
            KuppiAnalyticsResponse result = service.getMyAnalytics();

            // Then
            assertThat(result.getTotalSessions()).isEqualTo(2L);
            assertThat(result.getCompletedSessions()).isEqualTo(1L);
            assertThat(result.getUpcomingSessions()).isEqualTo(1L);
            assertThat(result.getTotalSessionViews()).isEqualTo(60L);
            assertThat(result.getTotalNotes()).isEqualTo(5L);
            assertThat(result.getTotalNoteViews()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should handle null total note views")
        void getMyAnalytics_nullNoteViews_returnsZero() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(sessionRepository.findByHostIdAndIsDeletedFalse(1L)).thenReturn(List.of());
            when(noteRepository.getTotalViewsByUploader(1L)).thenReturn(null);
            when(noteRepository.countByUploadedByIdAndIsDeletedFalse(1L)).thenReturn(0L);

            // When
            KuppiAnalyticsResponse result = service.getMyAnalytics();

            // Then
            assertThat(result.getTotalNoteViews()).isEqualTo(0L);
        }
    }

    // ============================================================
    // getPlatformStats
    // ============================================================

    @Nested
    @DisplayName("getPlatformStats")
    class GetPlatformStatsTests {

        @Test
        @DisplayName("Should return platform statistics")
        void getPlatformStats_returnsStats() {
            // Given
            when(sessionRepository.count()).thenReturn(100L);
            when(sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.COMPLETED)).thenReturn(80L);
            when(sessionRepository.countByStatusAndIsDeletedFalse(KuppiSessionStatus.CANCELLED)).thenReturn(5L);
            when(noteRepository.count()).thenReturn(200L);

            // When
            KuppiPlatformStatsResponse result = service.getPlatformStats();

            // Then
            assertThat(result.getTotalSessions()).isEqualTo(100L);
            assertThat(result.getCompletedSessions()).isEqualTo(80L);
            assertThat(result.getCancelledSessions()).isEqualTo(5L);
            assertThat(result.getTotalNotes()).isEqualTo(200L);
        }
    }

    // ============================================================
    // softDeleteSession
    // ============================================================

    @Nested
    @DisplayName("softDeleteSession")
    class SoftDeleteSessionTests {

        @Test
        @DisplayName("Should soft delete session and its notes")
        void softDeleteSession_deletesNotesAndSession() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).status(KuppiSessionStatus.SCHEDULED).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            KuppiNote note = KuppiNote.builder().id(10L).fileUrl("url").build();
            when(noteRepository.findBySessionIdAndIsDeletedFalse(5L)).thenReturn(List.of(note));

            // When
            service.softDeleteSession(5L);

            // Then
            assertThat(session.getIsDeleted()).isTrue();
            verify(noteRepository).save(note);
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("Should throw when deleting live session")
        void softDeleteSession_liveSession_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student host = mock(Student.class);
            when(host.getId()).thenReturn(1L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(host).status(KuppiSessionStatus.LIVE).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.softDeleteSession(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("currently live");
        }

        @Test
        @DisplayName("Should throw when not session owner")
        void softDeleteSession_notOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student otherHost = mock(Student.class);
            when(otherHost.getId()).thenReturn(99L);

            KuppiSession session = KuppiSession.builder()
                    .id(5L).host(otherHost).build();
            when(sessionRepository.findByIdWithHost(5L)).thenReturn(Optional.of(session));

            // When & Then
            assertThatThrownBy(() -> service.softDeleteSession(5L))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    // ============================================================
    // permanentlyDeleteSession
    // ============================================================

    @Nested
    @DisplayName("permanentlyDeleteSession")
    class PermanentlyDeleteSessionTests {

        @Test
        @DisplayName("Should permanently delete session, notes, and S3 files")
        void permanentlyDeleteSession_deletesAll() {
            // Given
            KuppiSession session = KuppiSession.builder()
                    .id(5L).fileUrl("https://s3/file.pdf").build();
            when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));

            KuppiNote note = KuppiNote.builder().id(10L).fileUrl("note-url").build();
            when(noteRepository.findBySessionIdAndIsDeletedFalse(5L)).thenReturn(List.of(note));

            // When
            service.permanentlyDeleteSession(5L);

            // Then
            verify(noteRepository).delete(note);
            verify(sessionRepository).delete(session);
        }

        @Test
        @DisplayName("Should throw when session not found")
        void permanentlyDeleteSession_notFound_throws() {
            // Given
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.permanentlyDeleteSession(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
