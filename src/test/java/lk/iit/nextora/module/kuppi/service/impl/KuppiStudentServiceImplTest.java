package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentDetailResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiStudentServiceImpl Unit Tests")
class KuppiStudentServiceImplTest {

    @Mock private StudentRepository studentRepository;
    @Mock private KuppiSessionRepository sessionRepository;
    @Mock private KuppiNoteRepository noteRepository;

    @InjectMocks
    private KuppiStudentServiceImpl service;

    private Student createMockStudent(Long id) {
        Student student = mock(Student.class);
        when(student.getId()).thenReturn(id);
        when(student.getStudentId()).thenReturn("STU" + id);
        when(student.getFirstName()).thenReturn("John");
        when(student.getLastName()).thenReturn("Doe");
        when(student.getEmail()).thenReturn("john" + id + "@iit.ac.lk");
        when(student.getIsActive()).thenReturn(true);
        return student;
    }

    // ============================================================
    // getAllKuppiStudents
    // ============================================================

    @Nested
    @DisplayName("getAllKuppiStudents")
    class GetAllKuppiStudentsTests {

        @Test
        @DisplayName("Should return paginated kuppi students with stats")
        void getAllKuppiStudents_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Student student = createMockStudent(1L);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
            when(studentRepository.findAllKuppiStudents(pageable)).thenReturn(page);

            when(sessionRepository.countByHostIdAndIsDeletedFalse(1L)).thenReturn(5L);
            when(sessionRepository.getTotalViewsByHost(1L)).thenReturn(100L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(1L, KuppiSessionStatus.SCHEDULED))
                    .thenReturn(2L);

            // When
            PagedResponse<KuppiStudentResponse> result = service.getAllKuppiStudents(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            KuppiStudentResponse resp = result.getContent().get(0);
            assertThat(resp.getTotalSessionsHosted()).isEqualTo(5L);
            assertThat(resp.getTotalViews()).isEqualTo(100L);
            assertThat(resp.getUpcomingSessions()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle empty result")
        void getAllKuppiStudents_empty_returnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(), pageable, 0);
            when(studentRepository.findAllKuppiStudents(pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiStudentResponse> result = service.getAllKuppiStudents(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null total views")
        void getAllKuppiStudents_nullViews_returnsZero() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Student student = createMockStudent(1L);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
            when(studentRepository.findAllKuppiStudents(pageable)).thenReturn(page);

            when(sessionRepository.countByHostIdAndIsDeletedFalse(1L)).thenReturn(0L);
            when(sessionRepository.getTotalViewsByHost(1L)).thenReturn(null);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(1L, KuppiSessionStatus.SCHEDULED))
                    .thenReturn(0L);

            // When
            PagedResponse<KuppiStudentResponse> result = service.getAllKuppiStudents(pageable);

            // Then
            assertThat(result.getContent().get(0).getTotalViews()).isEqualTo(0L);
        }
    }

    // ============================================================
    // getKuppiStudentById
    // ============================================================

    @Nested
    @DisplayName("getKuppiStudentById")
    class GetKuppiStudentByIdTests {

        @Test
        @DisplayName("Should return detailed student response with stats")
        void getKuppiStudentById_returnsDetailedResponse() {
            // Given
            Student student = createMockStudent(5L);
            when(student.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 0, 0));
            when(studentRepository.findKuppiStudentById(5L)).thenReturn(Optional.of(student));

            when(sessionRepository.countByHostIdAndIsDeletedFalse(5L)).thenReturn(10L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(5L, KuppiSessionStatus.COMPLETED))
                    .thenReturn(7L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(5L, KuppiSessionStatus.LIVE))
                    .thenReturn(1L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(5L, KuppiSessionStatus.SCHEDULED))
                    .thenReturn(2L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(5L, KuppiSessionStatus.CANCELLED))
                    .thenReturn(0L);
            when(sessionRepository.getTotalViewsByHost(5L)).thenReturn(500L);
            when(noteRepository.countByUploadedByIdAndIsDeletedFalse(5L)).thenReturn(15L);
            when(sessionRepository.findRecentSessionsByHost(eq(5L), any())).thenReturn(List.of());
            when(sessionRepository.findUpcomingSessionsByHost(eq(5L), any(), any(), any()))
                    .thenReturn(List.of());

            // When
            KuppiStudentDetailResponse result = service.getKuppiStudentById(5L);

            // Then
            assertThat(result.getTotalSessionsHosted()).isEqualTo(10L);
            assertThat(result.getCompletedSessions()).isEqualTo(7L);
            assertThat(result.getLiveSessions()).isEqualTo(1L);
            assertThat(result.getScheduledSessions()).isEqualTo(2L);
            assertThat(result.getTotalViews()).isEqualTo(500L);
            assertThat(result.getTotalNotesUploaded()).isEqualTo(15L);
            assertThat(result.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should include recent and upcoming sessions in response")
        void getKuppiStudentById_includesRecentAndUpcoming() {
            // Given
            Student student = createMockStudent(5L);
            when(student.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 0, 0));
            when(studentRepository.findKuppiStudentById(5L)).thenReturn(Optional.of(student));

            when(sessionRepository.countByHostIdAndIsDeletedFalse(5L)).thenReturn(0L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(eq(5L), any())).thenReturn(0L);
            when(sessionRepository.getTotalViewsByHost(5L)).thenReturn(null);
            when(noteRepository.countByUploadedByIdAndIsDeletedFalse(5L)).thenReturn(0L);

            KuppiSession recentSession = KuppiSession.builder()
                    .id(10L).title("Recent Session").subject("Math")
                    .status(KuppiSessionStatus.COMPLETED).viewCount(20L)
                    .scheduledStartTime(LocalDateTime.now().minusDays(1))
                    .scheduledEndTime(LocalDateTime.now().minusDays(1).plusHours(2))
                    .build();
            when(sessionRepository.findRecentSessionsByHost(eq(5L), any()))
                    .thenReturn(List.of(recentSession));

            KuppiSession upcomingSession = KuppiSession.builder()
                    .id(20L).title("Upcoming Session").subject("Physics")
                    .status(KuppiSessionStatus.SCHEDULED).viewCount(5L)
                    .scheduledStartTime(LocalDateTime.now().plusDays(1))
                    .scheduledEndTime(LocalDateTime.now().plusDays(1).plusHours(2))
                    .build();
            when(sessionRepository.findUpcomingSessionsByHost(eq(5L), any(), any(), any()))
                    .thenReturn(List.of(upcomingSession));

            // When
            KuppiStudentDetailResponse result = service.getKuppiStudentById(5L);

            // Then
            assertThat(result.getRecentSessions()).hasSize(1);
            assertThat(result.getRecentSessions().get(0).getTitle()).isEqualTo("Recent Session");
            assertThat(result.getUpcomingSessions()).hasSize(1);
            assertThat(result.getUpcomingSessions().get(0).getTitle()).isEqualTo("Upcoming Session");
        }

        @Test
        @DisplayName("Should throw when student not found")
        void getKuppiStudentById_notFound_throws() {
            // Given
            when(studentRepository.findKuppiStudentById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.getKuppiStudentById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Kuppi Student");
        }

        @Test
        @DisplayName("Should handle null total views in detail response")
        void getKuppiStudentById_nullViews_returnsZero() {
            // Given
            Student student = createMockStudent(5L);
            when(student.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 0, 0));
            when(studentRepository.findKuppiStudentById(5L)).thenReturn(Optional.of(student));

            when(sessionRepository.countByHostIdAndIsDeletedFalse(5L)).thenReturn(0L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(eq(5L), any())).thenReturn(0L);
            when(sessionRepository.getTotalViewsByHost(5L)).thenReturn(null);
            when(noteRepository.countByUploadedByIdAndIsDeletedFalse(5L)).thenReturn(0L);
            when(sessionRepository.findRecentSessionsByHost(eq(5L), any())).thenReturn(List.of());
            when(sessionRepository.findUpcomingSessionsByHost(eq(5L), any(), any(), any()))
                    .thenReturn(List.of());

            // When
            KuppiStudentDetailResponse result = service.getKuppiStudentById(5L);

            // Then
            assertThat(result.getTotalViews()).isEqualTo(0L);
        }
    }

    // ============================================================
    // searchKuppiStudentsByName
    // ============================================================

    @Nested
    @DisplayName("searchKuppiStudentsByName")
    class SearchByNameTests {

        @Test
        @DisplayName("Should search students by name and return paged response")
        void searchByName_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Student student = createMockStudent(1L);
            Page<Student> page = new PageImpl<>(List.of(student), pageable, 1);
            when(studentRepository.searchKuppiStudentsByName("John", pageable)).thenReturn(page);

            when(sessionRepository.countByHostIdAndIsDeletedFalse(1L)).thenReturn(0L);
            when(sessionRepository.getTotalViewsByHost(1L)).thenReturn(0L);
            when(sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(1L, KuppiSessionStatus.SCHEDULED))
                    .thenReturn(0L);

            // When
            PagedResponse<KuppiStudentResponse> result =
                    service.searchKuppiStudentsByName("John", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(studentRepository).searchKuppiStudentsByName("John", pageable);
        }
    }

    // ============================================================
    // searchKuppiStudentsBySubject
    // ============================================================

    @Nested
    @DisplayName("searchKuppiStudentsBySubject")
    class SearchBySubjectTests {

        @Test
        @DisplayName("Should search students by subject")
        void searchBySubject_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(), pageable, 0);
            when(studentRepository.searchKuppiStudentsBySubject("Math", pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiStudentResponse> result =
                    service.searchKuppiStudentsBySubject("Math", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(studentRepository).searchKuppiStudentsBySubject("Math", pageable);
        }
    }

    // ============================================================
    // getKuppiStudentsByFaculty
    // ============================================================

    @Nested
    @DisplayName("getKuppiStudentsByFaculty")
    class GetByFacultyTests {

        @Test
        @DisplayName("Should return students by faculty")
        void getByFaculty_validFaculty_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> page = new PageImpl<>(List.of(), pageable, 0);
            when(studentRepository.findKuppiStudentsByFaculty(FacultyType.COMPUTING, pageable))
                    .thenReturn(page);

            // When
            PagedResponse<KuppiStudentResponse> result =
                    service.getKuppiStudentsByFaculty("COMPUTING", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(studentRepository).findKuppiStudentsByFaculty(FacultyType.COMPUTING, pageable);
        }

        @Test
        @DisplayName("Should throw for invalid faculty name")
        void getByFaculty_invalidFaculty_throws() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> service.getKuppiStudentsByFaculty("INVALID_FACULTY", pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Faculty");
        }
    }
}
