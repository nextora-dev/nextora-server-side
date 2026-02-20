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
import lk.iit.nextora.module.kuppi.service.KuppiStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Kuppi Student operations.
 * Provides methods to view and retrieve Kuppi Student information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KuppiStudentServiceImpl implements KuppiStudentService {

    private final StudentRepository studentRepository;
    private final KuppiSessionRepository sessionRepository;
    private final KuppiNoteRepository noteRepository;

    private static final int RECENT_SESSIONS_LIMIT = 5;
    private static final int UPCOMING_SESSIONS_LIMIT = 5;

    @Override
    public PagedResponse<KuppiStudentResponse> getAllKuppiStudents(Pageable pageable) {
        log.debug("Fetching all Kuppi students with pagination: page={}, size={}",
                  pageable.getPageNumber(), pageable.getPageSize());

        Page<Student> students = studentRepository.findAllKuppiStudents(pageable);
        return toPagedResponse(students);
    }

    @Override
    public KuppiStudentDetailResponse getKuppiStudentById(Long studentId) {
        log.debug("Fetching Kuppi student details for ID: {}", studentId);

        Student student = studentRepository.findKuppiStudentById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Kuppi Student", "id", studentId));

        return buildDetailedResponse(student);
    }

    @Override
    public PagedResponse<KuppiStudentResponse> searchKuppiStudentsByName(String name, Pageable pageable) {
        log.debug("Searching Kuppi students by name: {}", name);

        Page<Student> students = studentRepository.searchKuppiStudentsByName(name, pageable);
        return toPagedResponse(students);
    }

    @Override
    public PagedResponse<KuppiStudentResponse> searchKuppiStudentsBySubject(String subject, Pageable pageable) {
        log.debug("Searching Kuppi students by subject: {}", subject);

        Page<Student> students = studentRepository.searchKuppiStudentsBySubject(subject, pageable);
        return toPagedResponse(students);
    }

    @Override
    public PagedResponse<KuppiStudentResponse> getKuppiStudentsByFaculty(String faculty, Pageable pageable) {
        log.debug("Fetching Kuppi students by faculty: {}", faculty);

        FacultyType facultyType;
        try {
            facultyType = FacultyType.valueOf(faculty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Faculty", "name", faculty);
        }

        Page<Student> students = studentRepository.findKuppiStudentsByFaculty(facultyType, pageable);
        return toPagedResponse(students);
    }

    @Override
    public PagedResponse<KuppiStudentResponse> getTopRatedKuppiStudents(Pageable pageable) {
        log.debug("Fetching top-rated Kuppi students");

        Page<Student> students = studentRepository.findTopRatedKuppiStudents(pageable);
        return toPagedResponse(students);
    }

    // ==================== Private Helper Methods ====================

    private PagedResponse<KuppiStudentResponse> toPagedResponse(Page<Student> studentsPage) {
        List<KuppiStudentResponse> content = studentsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<KuppiStudentResponse>builder()
                .content(content)
                .pageNumber(studentsPage.getNumber())
                .pageSize(studentsPage.getSize())
                .totalElements(studentsPage.getTotalElements())
                .totalPages(studentsPage.getTotalPages())
                .first(studentsPage.isFirst())
                .last(studentsPage.isLast())
                .build();
    }

    private KuppiStudentResponse mapToResponse(Student student) {
        Long totalSessionsHosted = sessionRepository.countByHostIdAndIsDeletedFalse(student.getId());
        Long totalViews = sessionRepository.getTotalViewsByHost(student.getId());
        Long upcomingSessions = sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(
                student.getId(), KuppiSessionStatus.SCHEDULED);

        return KuppiStudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .email(student.getEmail())
                .profilePictureUrl(student.getProfilePictureUrl())
                .batch(student.getBatch())
                .program(student.getProgram())
                .faculty(student.getFaculty())
                .kuppiSubjects(student.getKuppiSubjects())
                .kuppiExperienceLevel(student.getKuppiExperienceLevel())
                .kuppiSessionsCompleted(student.getKuppiSessionsCompleted())
                .kuppiRating(student.getKuppiRating())
                .kuppiAvailability(student.getKuppiAvailability())
                .totalSessionsHosted(totalSessionsHosted)
                .totalViews(totalViews != null ? totalViews : 0L)
                .upcomingSessions(upcomingSessions)
                .isActive(student.getIsActive())
                .build();
    }

    private KuppiStudentDetailResponse buildDetailedResponse(Student student) {
        LocalDateTime now = LocalDateTime.now();

        // Get session statistics
        Long totalSessionsHosted = sessionRepository.countByHostIdAndIsDeletedFalse(student.getId());
        Long completedSessions = sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(
                student.getId(), KuppiSessionStatus.COMPLETED);
        Long liveSessions = sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(
                student.getId(), KuppiSessionStatus.LIVE);
        Long scheduledSessions = sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(
                student.getId(), KuppiSessionStatus.SCHEDULED);
        Long cancelledSessions = sessionRepository.countByHostIdAndStatusAndIsDeletedFalse(
                student.getId(), KuppiSessionStatus.CANCELLED);
        Long totalViews = sessionRepository.getTotalViewsByHost(student.getId());

        // Get notes statistics
        Long totalNotesUploaded = noteRepository.countByUploadedByIdAndIsDeletedFalse(student.getId());


        // Get recent sessions
        List<KuppiSession> recentSessionsList = sessionRepository.findRecentSessionsByHost(
                student.getId(), PageRequest.of(0, RECENT_SESSIONS_LIMIT));
        List<KuppiStudentDetailResponse.SessionSummary> recentSessions = recentSessionsList.stream()
                .map(this::mapToSessionSummary)
                .collect(Collectors.toList());

        // Get upcoming sessions
        List<KuppiSession> upcomingSessionsList = sessionRepository.findUpcomingSessionsByHost(
                student.getId(), now, KuppiSessionStatus.SCHEDULED, PageRequest.of(0, UPCOMING_SESSIONS_LIMIT));
        List<KuppiStudentDetailResponse.SessionSummary> upcomingSessions = upcomingSessionsList.stream()
                .map(this::mapToSessionSummary)
                .collect(Collectors.toList());

        return KuppiStudentDetailResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .email(student.getEmail())
                .profilePictureUrl(student.getProfilePictureUrl())
                .batch(student.getBatch())
                .program(student.getProgram())
                .faculty(student.getFaculty())
                .kuppiSubjects(student.getKuppiSubjects())
                .kuppiExperienceLevel(student.getKuppiExperienceLevel())
                .kuppiSessionsCompleted(student.getKuppiSessionsCompleted())
                .kuppiRating(student.getKuppiRating())
                .kuppiAvailability(student.getKuppiAvailability())
                .totalSessionsHosted(totalSessionsHosted)
                .completedSessions(completedSessions)
                .liveSessions(liveSessions)
                .scheduledSessions(scheduledSessions)
                .cancelledSessions(cancelledSessions)
                .totalViews(totalViews != null ? totalViews : 0L)
                .totalNotesUploaded(totalNotesUploaded)
                .recentSessions(recentSessions)
                .upcomingSessions(upcomingSessions)
                .memberSince(student.getCreatedAt())
                .isActive(student.getIsActive())
                .build();
    }

    private KuppiStudentDetailResponse.SessionSummary mapToSessionSummary(KuppiSession session) {
        return KuppiStudentDetailResponse.SessionSummary.builder()
                .id(session.getId())
                .title(session.getTitle())
                .subject(session.getSubject())
                .status(session.getStatus())
                .scheduledStartTime(session.getScheduledStartTime())
                .scheduledEndTime(session.getScheduledEndTime())
                .viewCount(session.getViewCount())
                .build();
    }
}
