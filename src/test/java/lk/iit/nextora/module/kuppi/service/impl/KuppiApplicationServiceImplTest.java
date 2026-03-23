package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ExperienceLevel;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.S3.S3Service;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.request.ReviewKuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationStatsResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiApplication;
import lk.iit.nextora.module.kuppi.mapper.KuppiApplicationMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiApplicationRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KuppiApplicationServiceImpl Unit Tests")
class KuppiApplicationServiceImplTest {

    @Mock private KuppiApplicationRepository applicationRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private SecurityService securityService;
    @Mock private KuppiApplicationMapper applicationMapper;
    @Mock private S3Service s3Service;

    @InjectMocks
    private KuppiApplicationServiceImpl service;

    // ============================================================
    // submitApplication
    // ============================================================

    @Nested
    @DisplayName("submitApplication")
    class SubmitApplicationTests {

        @Test
        @DisplayName("Should submit application successfully with file upload")
        void submitApplication_validRequest_savesAndReturns() {
            // Given
            Student student = mock(Student.class);
            when(student.getId()).thenReturn(1L);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(false);

            MockMultipartFile file = new MockMultipartFile(
                    "academicResults", "results.pdf", "application/pdf", "pdf content".getBytes());
            KuppiApplicationRequest request = KuppiApplicationRequest.builder()
                    .motivation("I want to teach").currentGpa(3.8)
                    .subjectsToTeach(Set.of("Math", "Physics"))
                    .build();

            KuppiApplication entity = KuppiApplication.builder().id(1L).build();
            when(applicationMapper.toEntity(request, student)).thenReturn(entity);
            when(s3Service.uploadFile(file, "kuppi-applications/academic-results")).thenReturn("s3-key");
            when(s3Service.getPublicUrl("s3-key")).thenReturn("https://s3.url/results.pdf");
            when(applicationRepository.save(any())).thenReturn(entity);

            KuppiApplicationResponse expectedResponse = KuppiApplicationResponse.builder().id(1L).build();
            when(applicationMapper.toResponse(entity)).thenReturn(expectedResponse);

            // When
            KuppiApplicationResponse result = service.submitApplication(request, file);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            verify(applicationRepository).save(argThat(app ->
                    "s3-key".equals(app.getAcademicResultsKey()) &&
                    "https://s3.url/results.pdf".equals(app.getAcademicResultsUrl()) &&
                    "results.pdf".equals(app.getAcademicResultsFileName())
            ));
        }

        @Test
        @DisplayName("Should reject when student is already a Kuppi Student")
        void submitApplication_alreadyKuppiStudent_throwsBadRequest() {
            // Given
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(true);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            KuppiApplicationRequest request = KuppiApplicationRequest.builder().build();
            MockMultipartFile file = new MockMultipartFile("f", "r.pdf", "application/pdf", "c".getBytes());

            // When & Then
            assertThatThrownBy(() -> service.submitApplication(request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already a Kuppi Student");
        }

        @Test
        @DisplayName("Should reject when student has active application")
        void submitApplication_hasActiveApplication_throwsBadRequest() {
            // Given
            Student student = mock(Student.class);
            when(student.getId()).thenReturn(1L);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(true);

            KuppiApplicationRequest request = KuppiApplicationRequest.builder().build();
            MockMultipartFile file = new MockMultipartFile("f", "r.pdf", "application/pdf", "c".getBytes());

            // When & Then
            assertThatThrownBy(() -> service.submitApplication(request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("pending or under review");
        }

        @Test
        @DisplayName("Should reject null file")
        void submitApplication_nullFile_throwsBadRequest() {
            // Given
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(student.getId()).thenReturn(1L);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(false);

            KuppiApplicationRequest request = KuppiApplicationRequest.builder().build();

            // When & Then
            assertThatThrownBy(() -> service.submitApplication(request, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("required");
        }

        @Test
        @DisplayName("Should reject file exceeding max size")
        void submitApplication_oversizedFile_throwsBadRequest() {
            // Given
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(student.getId()).thenReturn(1L);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(false);

            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile file = new MockMultipartFile(
                    "f", "results.pdf", "application/pdf", largeContent);

            KuppiApplicationRequest request = KuppiApplicationRequest.builder().build();

            // When & Then
            assertThatThrownBy(() -> service.submitApplication(request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("size");
        }

        @Test
        @DisplayName("Should reject invalid file content type")
        void submitApplication_invalidContentType_throwsBadRequest() {
            // Given
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(student.getId()).thenReturn(1L);
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(false);

            MockMultipartFile file = new MockMultipartFile(
                    "f", "results.exe", "application/octet-stream", "content".getBytes());

            KuppiApplicationRequest request = KuppiApplicationRequest.builder().build();

            // When & Then
            assertThatThrownBy(() -> service.submitApplication(request, file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid file type");
        }
    }

    // ============================================================
    // getMyApplications
    // ============================================================

    @Nested
    @DisplayName("getMyApplications")
    class GetMyApplicationsTests {

        @Test
        @DisplayName("Should return current user's applications")
        void getMyApplications_returnsList() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            KuppiApplication app = KuppiApplication.builder().id(1L).build();
            when(applicationRepository.findByStudentId(1L)).thenReturn(List.of(app));
            KuppiApplicationResponse response = KuppiApplicationResponse.builder().id(1L).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            List<KuppiApplicationResponse> result = service.getMyApplications();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }
    }

    // ============================================================
    // getMyActiveApplication
    // ============================================================

    @Nested
    @DisplayName("getMyActiveApplication")
    class GetMyActiveApplicationTests {

        @Test
        @DisplayName("Should return active application when exists")
        void getMyActiveApplication_exists_returnsResponse() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            KuppiApplication app = KuppiApplication.builder().id(1L).build();
            when(applicationRepository.findActiveApplicationByStudentId(1L))
                    .thenReturn(Optional.of(app));
            KuppiApplicationResponse response = KuppiApplicationResponse.builder().id(1L).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            KuppiApplicationResponse result = service.getMyActiveApplication();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return null when no active application")
        void getMyActiveApplication_noActive_returnsNull() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            when(applicationRepository.findActiveApplicationByStudentId(1L))
                    .thenReturn(Optional.empty());

            // When
            KuppiApplicationResponse result = service.getMyActiveApplication();

            // Then
            assertThat(result).isNull();
        }
    }

    // ============================================================
    // cancelMyApplication
    // ============================================================

    @Nested
    @DisplayName("cancelMyApplication")
    class CancelMyApplicationTests {

        @Test
        @DisplayName("Should cancel own application successfully")
        void cancelMyApplication_ownerAndCancellable_cancelsAndCleansUp() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student student = mock(Student.class);
            when(student.getId()).thenReturn(1L);

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).student(student)
                    .status(KuppiApplicationStatus.PENDING)
                    .academicResultsKey("some-key").build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            // When
            service.cancelMyApplication(5L);

            // Then
            assertThat(app.getStatus()).isEqualTo(KuppiApplicationStatus.CANCELLED);
            verify(applicationRepository).save(app);
            verify(s3Service).deleteFile("some-key");
        }

        @Test
        @DisplayName("Should throw when cancelling another user's application")
        void cancelMyApplication_notOwner_throwsUnauthorized() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student otherStudent = mock(Student.class);
            when(otherStudent.getId()).thenReturn(2L);

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).student(otherStudent).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            // When & Then
            assertThatThrownBy(() -> service.cancelMyApplication(5L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("your own");
        }

        @Test
        @DisplayName("Should throw when application cannot be cancelled")
        void cancelMyApplication_approved_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            Student student = mock(Student.class);
            when(student.getId()).thenReturn(1L);

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).student(student)
                    .status(KuppiApplicationStatus.APPROVED).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            // When & Then
            assertThatThrownBy(() -> service.cancelMyApplication(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot be cancelled");
        }
    }

    // ============================================================
    // canApply
    // ============================================================

    @Nested
    @DisplayName("canApply")
    class CanApplyTests {

        @Test
        @DisplayName("Should return true when student is eligible")
        void canApply_eligibleStudent_returnsTrue() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser studentUser = mock(BaseUser.class);
            when(studentUser.getRole()).thenReturn(UserRole.ROLE_STUDENT);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(studentUser));

            Student student = mock(Student.class);
            lenient().when(student.getId()).thenReturn(1L);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(false);

            // When
            boolean result = service.canApply();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-student users")
        void canApply_notStudent_returnsFalse() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser adminUser = mock(BaseUser.class);
            when(adminUser.getRole()).thenReturn(UserRole.ROLE_ADMIN);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(adminUser));

            // When
            boolean result = service.canApply();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when already a Kuppi Student")
        void canApply_alreadyKuppi_returnsFalse() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser studentUser = mock(BaseUser.class);
            when(studentUser.getRole()).thenReturn(UserRole.ROLE_STUDENT);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(studentUser));

            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            // When
            boolean result = service.canApply();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when has active application")
        void canApply_hasActiveApp_returnsFalse() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser studentUser = mock(BaseUser.class);
            when(studentUser.getRole()).thenReturn(UserRole.ROLE_STUDENT);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(studentUser));

            Student student = mock(Student.class);
            lenient().when(student.getId()).thenReturn(1L);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(applicationRepository.hasActiveApplication(1L)).thenReturn(true);

            // When
            boolean result = service.canApply();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // isKuppiStudent
    // ============================================================

    @Nested
    @DisplayName("isKuppiStudent")
    class IsKuppiStudentTests {

        @Test
        @DisplayName("Should return true when student has Kuppi capability")
        void isKuppiStudent_hasCapability_returnsTrue() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser studentUser = mock(BaseUser.class);
            when(studentUser.getRole()).thenReturn(UserRole.ROLE_STUDENT);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(studentUser));

            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(true);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            // When
            boolean result = service.isKuppiStudent();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-student user")
        void isKuppiStudent_notStudent_returnsFalse() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(1L);
            BaseUser adminUser = mock(BaseUser.class);
            when(adminUser.getRole()).thenReturn(UserRole.ROLE_ADMIN);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(adminUser));

            // When
            boolean result = service.isKuppiStudent();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // Admin Operations - getAllApplications, getApplicationsByStatus, etc.
    // ============================================================

    @Nested
    @DisplayName("Admin Operations")
    class AdminOperationsTests {

        @Test
        @DisplayName("getAllApplications should return paged response")
        void getAllApplications_returnsPaged() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            KuppiApplication app = KuppiApplication.builder().id(1L).build();
            Page<KuppiApplication> page = new PageImpl<>(List.of(app), pageable, 1);
            when(applicationRepository.findAll(pageable)).thenReturn(page);
            when(applicationMapper.toResponse(app))
                    .thenReturn(KuppiApplicationResponse.builder().id(1L).build());

            // When
            PagedResponse<KuppiApplicationResponse> result = service.getAllApplications(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getApplicationsByStatus should filter by status")
        void getApplicationsByStatus_filters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiApplication> page = new PageImpl<>(List.of(), pageable, 0);
            when(applicationRepository.findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(
                    KuppiApplicationStatus.PENDING, pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiApplicationResponse> result =
                    service.getApplicationsByStatus(KuppiApplicationStatus.PENDING, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("getPendingApplications should return pending apps")
        void getPendingApplications_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiApplication> page = new PageImpl<>(List.of(), pageable, 0);
            when(applicationRepository.findPendingApplications(pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiApplicationResponse> result = service.getPendingApplications(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(applicationRepository).findPendingApplications(pageable);
        }

        @Test
        @DisplayName("getActiveApplications should return active apps")
        void getActiveApplications_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiApplication> page = new PageImpl<>(List.of(), pageable, 0);
            when(applicationRepository.findActiveApplications(pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiApplicationResponse> result = service.getActiveApplications(pageable);

            // Then
            verify(applicationRepository).findActiveApplications(pageable);
        }

        @Test
        @DisplayName("getApplicationById should return application response")
        void getApplicationById_returnsResponse() {
            // Given
            KuppiApplication app = KuppiApplication.builder().id(5L).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
            KuppiApplicationResponse response = KuppiApplicationResponse.builder().id(5L).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            KuppiApplicationResponse result = service.getApplicationById(5L);

            // Then
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("getApplicationById should throw when not found")
        void getApplicationById_notFound_throws() {
            // Given
            when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.getApplicationById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("searchApplications should delegate to repository")
        void searchApplications_delegatesToRepo() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<KuppiApplication> page = new PageImpl<>(List.of(), pageable, 0);
            when(applicationRepository.searchApplications("john", pageable)).thenReturn(page);

            // When
            PagedResponse<KuppiApplicationResponse> result =
                    service.searchApplications("john", pageable);

            // Then
            verify(applicationRepository).searchApplications("john", pageable);
        }
    }

    // ============================================================
    // approveApplication
    // ============================================================

    @Nested
    @DisplayName("approveApplication")
    class ApproveApplicationTests {

        @Test
        @DisplayName("Should approve application and grant Kuppi role")
        void approveApplication_validState_approvesAndGrantsRole() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            Student student = mock(Student.class);
            when(student.getStudentId()).thenReturn("STU001");

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).student(student)
                    .status(KuppiApplicationStatus.PENDING)
                    .subjectsToTeach(new HashSet<>(Set.of("Math")))
                    .preferredExperienceLevel(ExperienceLevel.INTERMEDIATE)
                    .availability("Weekends")
                    .build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
            when(applicationRepository.save(any())).thenReturn(app);

            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            request.setReviewNotes("Good student");

            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.APPROVED).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            KuppiApplicationResponse result = service.approveApplication(5L, request);

            // Then
            assertThat(result.getStatus()).isEqualTo(KuppiApplicationStatus.APPROVED);
            verify(student).addRoleType(StudentRoleType.KUPPI_STUDENT);
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("Should throw when application cannot be approved")
        void approveApplication_alreadyApproved_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).status(KuppiApplicationStatus.APPROVED).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();

            // When & Then
            assertThatThrownBy(() -> service.approveApplication(5L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot be approved");
        }
    }

    // ============================================================
    // rejectApplication
    // ============================================================

    @Nested
    @DisplayName("rejectApplication")
    class RejectApplicationTests {

        @Test
        @DisplayName("Should reject application with reason and cleanup S3")
        void rejectApplication_validState_rejectsAndCleansUp() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            Student student = mock(Student.class);
            when(student.getStudentId()).thenReturn("STU001");

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).student(student)
                    .status(KuppiApplicationStatus.PENDING)
                    .academicResultsKey("some-s3-key").build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
            when(applicationRepository.save(any())).thenReturn(app);

            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            request.setRejectionReason("GPA too low");
            request.setReviewNotes("Minimum 3.0 required");

            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.REJECTED).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            KuppiApplicationResponse result = service.rejectApplication(5L, request);

            // Then
            assertThat(result.getStatus()).isEqualTo(KuppiApplicationStatus.REJECTED);
            verify(s3Service).deleteFile("some-s3-key");
        }

        @Test
        @DisplayName("Should throw when rejection reason is missing")
        void rejectApplication_noReason_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).status(KuppiApplicationStatus.PENDING).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            // No rejection reason

            // When & Then
            assertThatThrownBy(() -> service.rejectApplication(5L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Rejection reason is required");
        }

        @Test
        @DisplayName("Should throw when application cannot be rejected")
        void rejectApplication_alreadyRejected_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).status(KuppiApplicationStatus.REJECTED).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            ReviewKuppiApplicationRequest request = new ReviewKuppiApplicationRequest();
            request.setRejectionReason("reason");

            // When & Then
            assertThatThrownBy(() -> service.rejectApplication(5L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot be rejected");
        }
    }

    // ============================================================
    // markUnderReview
    // ============================================================

    @Nested
    @DisplayName("markUnderReview")
    class MarkUnderReviewTests {

        @Test
        @DisplayName("Should mark pending application as under review")
        void markUnderReview_pending_succeeds() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).status(KuppiApplicationStatus.PENDING).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));
            when(applicationRepository.save(any())).thenReturn(app);

            KuppiApplicationResponse response = KuppiApplicationResponse.builder()
                    .id(5L).status(KuppiApplicationStatus.UNDER_REVIEW).build();
            when(applicationMapper.toResponse(app)).thenReturn(response);

            // When
            KuppiApplicationResponse result = service.markUnderReview(5L);

            // Then
            assertThat(result.getStatus()).isEqualTo(KuppiApplicationStatus.UNDER_REVIEW);
        }

        @Test
        @DisplayName("Should throw when application is not pending")
        void markUnderReview_notPending_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            BaseUser reviewer = mock(BaseUser.class);
            when(securityService.getCurrentUser()).thenReturn(Optional.of(reviewer));

            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).status(KuppiApplicationStatus.APPROVED).build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            // When & Then
            assertThatThrownBy(() -> service.markUnderReview(5L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Only pending");
        }
    }

    // ============================================================
    // getApplicationStats
    // ============================================================

    @Nested
    @DisplayName("getApplicationStats")
    class GetApplicationStatsTests {

        @Test
        @DisplayName("Should return stats from repository counts")
        void getApplicationStats_returnsAggregatedStats() {
            // Given
            when(applicationRepository.countTotalApplications()).thenReturn(50L);
            when(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.PENDING)).thenReturn(10L);
            when(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.UNDER_REVIEW)).thenReturn(5L);
            when(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.APPROVED)).thenReturn(30L);
            when(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.REJECTED)).thenReturn(3L);
            when(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.CANCELLED)).thenReturn(2L);
            when(applicationRepository.countApplicationsSubmittedToday(any())).thenReturn(4L);
            when(studentRepository.countByStudentRoleTypesContaining(StudentRoleType.KUPPI_STUDENT)).thenReturn(25L);

            // When
            KuppiApplicationStatsResponse result = service.getApplicationStats();

            // Then
            assertThat(result.getTotalApplications()).isEqualTo(50L);
            assertThat(result.getPendingApplications()).isEqualTo(10L);
            assertThat(result.getApprovedApplications()).isEqualTo(30L);
            assertThat(result.getTotalKuppiStudents()).isEqualTo(25L);
            assertThat(result.getApplicationsToday()).isEqualTo(4L);
        }
    }

    // ============================================================
    // permanentlyDeleteApplication
    // ============================================================

    @Nested
    @DisplayName("permanentlyDeleteApplication")
    class PermanentlyDeleteApplicationTests {

        @Test
        @DisplayName("Should delete application and cleanup S3")
        void permanentlyDeleteApplication_deletesAndCleansUp() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            KuppiApplication app = KuppiApplication.builder()
                    .id(5L).academicResultsKey("results-key").build();
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

            // When
            service.permanentlyDeleteApplication(5L);

            // Then
            verify(s3Service).deleteFile("results-key");
            verify(applicationRepository).delete(app);
        }

        @Test
        @DisplayName("Should throw when application not found")
        void permanentlyDeleteApplication_notFound_throws() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.permanentlyDeleteApplication(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============================================================
    // revokeKuppiStudentRole
    // ============================================================

    @Nested
    @DisplayName("revokeKuppiStudentRole")
    class RevokeKuppiStudentRoleTests {

        @Test
        @DisplayName("Should revoke role and clear Kuppi fields")
        void revokeKuppiStudentRole_clearsRoleAndFields() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(true);
            when(student.getStudentId()).thenReturn("STU001");
            when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

            // When
            service.revokeKuppiStudentRole(5L, "Misconduct");

            // Then
            verify(student).removeRoleType(StudentRoleType.KUPPI_STUDENT);
            verify(student).removeRoleType(StudentRoleType.KUPPI_STUDENT);
            verify(student).setKuppiSubjects(null);
            verify(student).setKuppiExperienceLevel(null);
            verify(student).setKuppiAvailability(null);
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("Should throw when student is not a Kuppi Student")
        void revokeKuppiStudentRole_notKuppi_throwsBadRequest() {
            // Given
            when(securityService.getCurrentUserId()).thenReturn(10L);
            Student student = mock(Student.class);
            when(student.hasKuppiCapability()).thenReturn(false);
            when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

            // When & Then
            assertThatThrownBy(() -> service.revokeKuppiStudentRole(5L, "Reason"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not a Kuppi Student");
        }
    }
}
