package lk.iit.nextora.module.intranet.service.impl;

import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.intranet.dto.*;
import lk.iit.nextora.module.intranet.entity.*;
import lk.iit.nextora.module.intranet.mapper.IntranetMapper;
import lk.iit.nextora.module.intranet.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntranetContentServiceImpl Unit Tests")
class IntranetContentServiceImplTest {

    @Mock private ProgramRepository programRepository;
    @Mock private StudentPolicyRepository policyRepository;
    @Mock private AcademicCalendarRepository calendarRepository;
    @Mock private IntranetMapper programMapper;
    @Mock private IntranetMapper policyMapper;
    @Mock private IntranetMapper calendarMapper;

    @InjectMocks private IntranetContentServiceImpl intranetService;

    // ============================================================
    // UNDERGRADUATE PROGRAMS TESTS
    // ============================================================

    @Nested
    @DisplayName("getAllUndergraduatePrograms")
    class GetAllUndergraduateProgramsTests {

        @Test
        @DisplayName("Should return all undergraduate programs")
        void getAllUndergraduatePrograms_success() {
            Program program = Program.builder()
                    .id(1L)
                    .programCode("CS101")
                    .programName("Bachelor of Computer Science")
                    .programLevel("UNDERGRADUATE")
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            ProgramResponse response = ProgramResponse.builder()
                    .id(1L)
                    .programCode("CS101")
                    .programName("Bachelor of Computer Science")
                    .build();

            when(programRepository.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("UNDERGRADUATE"))
                    .thenReturn(List.of(program));
            when(programMapper.toProgramSummaryResponseList(List.of(program)))
                    .thenReturn(List.of(response));

            List<ProgramResponse> result = intranetService.getAllUndergraduatePrograms();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProgramCode()).isEqualTo("CS101");
            verify(programRepository, times(1)).findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("UNDERGRADUATE");
        }

        @Test
        @DisplayName("Should return empty list when no undergraduate programs exist")
        void getAllUndergraduatePrograms_empty() {
            when(programRepository.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("UNDERGRADUATE"))
                    .thenReturn(List.of());
            when(programMapper.toProgramSummaryResponseList(List.of()))
                    .thenReturn(List.of());

            List<ProgramResponse> result = intranetService.getAllUndergraduatePrograms();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUndergraduateProgramBySlug")
    class GetUndergraduateProgramBySlugTests {

        @Test
        @DisplayName("Should get undergraduate program by slug")
        void getUndergraduateProgramBySlug_success() {
            String slug = "bsc-computer-science";
            Program program = Program.builder()
                    .id(1L)
                    .programSlug(slug)
                    .programLevel("UNDERGRADUATE")
                    .build();

            ProgramResponse response = ProgramResponse.builder()
                    .id(1L)
                    .programSlug(slug)
                    .build();

            when(programRepository.findByProgramSlugAndProgramLevelWithModules(slug, "UNDERGRADUATE"))
                    .thenReturn(Optional.of(program));
            when(programMapper.toProgramResponse(program)).thenReturn(response);

            ProgramResponse result = intranetService.getUndergraduateProgramBySlug(slug);

            assertThat(result).isNotNull();
            assertThat(result.getProgramSlug()).isEqualTo(slug);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when program not found")
        void getUndergraduateProgramBySlug_notFound() {
            String slug = "nonexistent";
            when(programRepository.findByProgramSlugAndProgramLevelWithModules(slug, "UNDERGRADUATE"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> intranetService.getUndergraduateProgramBySlug(slug))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // POSTGRADUATE PROGRAMS TESTS
    // ============================================================

    @Nested
    @DisplayName("getAllPostgraduatePrograms")
    class GetAllPostgraduateProgramsTests {

        @Test
        @DisplayName("Should return all postgraduate programs")
        void getAllPostgraduatePrograms_success() {
            Program program = Program.builder()
                    .id(2L)
                    .programCode("MS101")
                    .programName("Master of Science")
                    .programLevel("POSTGRADUATE")
                    .build();

            ProgramResponse response = ProgramResponse.builder()
                    .id(2L)
                    .programCode("MS101")
                    .programName("Master of Science")
                    .build();

            when(programRepository.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("POSTGRADUATE"))
                    .thenReturn(List.of(program));
            when(programMapper.toProgramSummaryResponseList(List.of(program)))
                    .thenReturn(List.of(response));

            List<ProgramResponse> result = intranetService.getAllPostgraduatePrograms();

            assertThat(result).hasSize(1);
            verify(programRepository).findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("POSTGRADUATE");
        }
    }

    @Nested
    @DisplayName("getPostgraduateProgramBySlug")
    class GetPostgraduateProgramBySlugTests {

        @Test
        @DisplayName("Should get postgraduate program by slug")
        void getPostgraduateProgramBySlug_success() {
            String slug = "msc-computer-science";
            Program program = Program.builder()
                    .id(2L)
                    .programSlug(slug)
                    .programLevel("POSTGRADUATE")
                    .build();

            ProgramResponse response = ProgramResponse.builder()
                    .id(2L)
                    .programSlug(slug)
                    .build();

            when(programRepository.findByProgramSlugAndProgramLevelWithModules(slug, "POSTGRADUATE"))
                    .thenReturn(Optional.of(program));
            when(programMapper.toProgramResponse(program)).thenReturn(response);

            ProgramResponse result = intranetService.getPostgraduateProgramBySlug(slug);

            assertThat(result).isNotNull();
        }
    }

    // ============================================================
    // STUDENT POLICIES TESTS
    // ============================================================

    @Nested
    @DisplayName("getAllStudentPolicies")
    class GetAllStudentPoliciesTests {

        @Test
        @DisplayName("Should return all student policies")
        void getAllStudentPolicies_success() {
            StudentPolicy policy = StudentPolicy.builder()
                    .id(1L)
                    .policyName("Code of Conduct")
                    .policySlug("code-of-conduct")
                    .build();

            StudentPolicyResponse response = StudentPolicyResponse.builder()
                    .id(1L)
                    .policyName("Code of Conduct")
                    .build();

            when(policyRepository.findAllByIsDeletedFalseAndIsActiveTrueOrderByPolicyNameAsc()).thenReturn(List.of(policy));
            when(policyMapper.toPolicySummaryResponseList(List.of(policy)))
                    .thenReturn(List.of(response));

            List<StudentPolicyResponse> result = intranetService.getAllStudentPolicies();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPolicyName()).isEqualTo("Code of Conduct");
        }

        @Test
        @DisplayName("Should return empty list when no policies exist")
        void getAllStudentPolicies_empty() {
            when(policyRepository.findAllByIsDeletedFalseAndIsActiveTrueOrderByPolicyNameAsc()).thenReturn(List.of());
            when(policyMapper.toPolicySummaryResponseList(List.of()))
                    .thenReturn(List.of());

            List<StudentPolicyResponse> result = intranetService.getAllStudentPolicies();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStudentPolicyBySlug")
    class GetStudentPolicyBySlugTests {

        @Test
        @DisplayName("Should get policy by slug")
        void getStudentPolicyBySlug_success() {
            String slug = "code-of-conduct";
            StudentPolicy policy = StudentPolicy.builder()
                    .id(1L)
                    .policySlug(slug)
                    .build();

            StudentPolicyResponse response = StudentPolicyResponse.builder()
                    .id(1L)
                    .policySlug(slug)
                    .build();

            when(policyRepository.findByPolicySlugAndIsDeletedFalse(slug))
                    .thenReturn(Optional.of(policy));
            when(policyMapper.toPolicyResponse(policy)).thenReturn(response);

            StudentPolicyResponse result = intranetService.getStudentPolicyBySlug(slug);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw when policy not found")
        void getStudentPolicyBySlug_notFound() {
            when(policyRepository.findByPolicySlugAndIsDeletedFalse("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> intranetService.getStudentPolicyBySlug("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // ACADEMIC CALENDARS TESTS
    // ============================================================

    @Nested
    @DisplayName("getAllAcademicCalendars")
    class GetAllAcademicCalendarsTests {

        @Test
        @DisplayName("Should return all academic calendars")
        void getAllAcademicCalendars_success() {
            AcademicCalendar calendar = AcademicCalendar.builder()
                    .id(1L)
                    .universityName("University of Colombo")
                    .universitySlug("uoc")
                    .build();

            AcademicCalendarResponse response = AcademicCalendarResponse.builder()
                    .id(1L)
                    .universityName("University of Colombo")
                    .build();

            when(calendarRepository.findAllByIsDeletedFalseAndIsActiveTrueOrderByUniversityNameAsc()).thenReturn(List.of(calendar));
            when(calendarMapper.toCalendarSummaryResponseList(List.of(calendar))).thenReturn(List.of(response));

            List<AcademicCalendarResponse> result = intranetService.getAllAcademicCalendars();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAcademicCalendarBySlug")
    class GetAcademicCalendarBySlugTests {

        @Test
        @DisplayName("Should get calendar by slug")
        void getAcademicCalendarBySlug_success() {
            String slug = "uoc";
            AcademicCalendar calendar = AcademicCalendar.builder()
                    .id(1L)
                    .universitySlug(slug)
                    .build();

            AcademicCalendarResponse response = AcademicCalendarResponse.builder()
                    .id(1L)
                    .universitySlug(slug)
                    .build();

            when(calendarRepository.findByUniversitySlugWithEvents(slug))
                    .thenReturn(Optional.of(calendar));
            when(calendarMapper.toCalendarResponse(calendar)).thenReturn(response);

            AcademicCalendarResponse result = intranetService.getAcademicCalendarBySlug(slug);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw when calendar not found")
        void getAcademicCalendarBySlug_notFound() {
            when(calendarRepository.findByUniversitySlugWithEvents("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> intranetService.getAcademicCalendarBySlug("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

