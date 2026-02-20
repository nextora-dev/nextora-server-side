package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.request.ReviewKuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationStatsResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiApplication;
import lk.iit.nextora.module.kuppi.mapper.KuppiApplicationMapper;
import lk.iit.nextora.module.kuppi.repository.KuppiApplicationRepository;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Kuppi Student application operations.
 * Handles the complete workflow for students applying to become Kuppi Students.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KuppiApplicationServiceImpl implements KuppiApplicationService {

    private final KuppiApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;
    private final KuppiApplicationMapper applicationMapper;

    // ==================== Student Operations ====================

    @Override
    @Transactional
    public KuppiApplicationResponse submitApplication(KuppiApplicationRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        Student student = findStudentById(currentUserId);

        // Validate student can apply
        validateCanApply(student);

        // Create application
        KuppiApplication application = applicationMapper.toEntity(request, student);
        application.setSubmittedAt(LocalDateTime.now());

        application = applicationRepository.save(application);

        log.info("Kuppi Student application submitted by student {} (ID: {})",
                student.getStudentId(), application.getId());

        return applicationMapper.toResponse(application);
    }

    @Override
    public List<KuppiApplicationResponse> getMyApplications() {
        Long currentUserId = securityService.getCurrentUserId();
        List<KuppiApplication> applications = applicationRepository.findByStudentId(currentUserId);

        return applications.stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KuppiApplicationResponse getMyActiveApplication() {
        Long currentUserId = securityService.getCurrentUserId();

        return applicationRepository.findActiveApplicationByStudentId(currentUserId)
                .map(applicationMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public void cancelMyApplication(Long applicationId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiApplication application = findApplicationById(applicationId);

        // Validate ownership
        if (!application.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only cancel your own application");
        }

        // Validate can be cancelled
        if (!application.canBeCancelled()) {
            throw new BadRequestException("Application cannot be cancelled in its current state: "
                    + application.getStatus().getDisplayName());
        }

        application.cancel();
        applicationRepository.save(application);

        log.info("Kuppi Student application {} cancelled by student {}", applicationId, currentUserId);
    }

    @Override
    public boolean canApply() {
        Long currentUserId = securityService.getCurrentUserId();

        // Check if user is a student
        if (isCurrentUserNotStudent()) {
            return false;
        }

        Student student = findStudentById(currentUserId);

        // Check if already a Kuppi Student
        if (student.hasKuppiCapability()) {
            return false;
        }

        // Check if has active application
        return !applicationRepository.hasActiveApplication(currentUserId);
    }

    @Override
    public boolean isKuppiStudent() {
        Long currentUserId = securityService.getCurrentUserId();

        if (isCurrentUserNotStudent()) {
            return false;
        }

        Student student = findStudentById(currentUserId);
        return student.hasKuppiCapability();
    }

    // ==================== Admin/Academic Staff Operations ====================

    @Override
    public PagedResponse<KuppiApplicationResponse> getAllApplications(Pageable pageable) {
        Page<KuppiApplication> applications = applicationRepository.findAll(pageable);
        return toPagedResponse(applications);
    }

    @Override
    public PagedResponse<KuppiApplicationResponse> getApplicationsByStatus(
            KuppiApplicationStatus status, Pageable pageable) {
        Page<KuppiApplication> applications = applicationRepository
                .findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(status, pageable);
        return toPagedResponse(applications);
    }

    @Override
    public PagedResponse<KuppiApplicationResponse> getPendingApplications(Pageable pageable) {
        Page<KuppiApplication> applications = applicationRepository.findPendingApplications(pageable);
        return toPagedResponse(applications);
    }

    @Override
    public PagedResponse<KuppiApplicationResponse> getActiveApplications(Pageable pageable) {
        Page<KuppiApplication> applications = applicationRepository.findActiveApplications(pageable);
        return toPagedResponse(applications);
    }

    @Override
    public KuppiApplicationResponse getApplicationById(Long applicationId) {
        KuppiApplication application = findApplicationById(applicationId);
        return applicationMapper.toResponse(application);
    }

    @Override
    public PagedResponse<KuppiApplicationResponse> searchApplications(String keyword, Pageable pageable) {
        Page<KuppiApplication> applications = applicationRepository.searchApplications(keyword, pageable);
        return toPagedResponse(applications);
    }

    @Override
    @Transactional
    public KuppiApplicationResponse approveApplication(Long applicationId, ReviewKuppiApplicationRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BaseUser reviewer = findUserById(currentUserId);
        KuppiApplication application = findApplicationById(applicationId);

        // Validate application can be approved
        if (!application.canBeApproved()) {
            throw new BadRequestException("Application cannot be approved in its current state: "
                    + application.getStatus().getDisplayName());
        }

        // Approve application
        application.approve(reviewer, request.getReviewNotes());
        applicationRepository.save(application);

        // Grant KUPPI_STUDENT role to the student
        Student student = application.getStudent();
        grantKuppiStudentRole(student, application);

        log.info("Kuppi Student application {} approved by {} for student {}",
                applicationId, currentUserId, student.getStudentId());

        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public KuppiApplicationResponse rejectApplication(Long applicationId, ReviewKuppiApplicationRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        BaseUser reviewer = findUserById(currentUserId);
        KuppiApplication application = findApplicationById(applicationId);

        // Validate application can be rejected
        if (!application.canBeRejected()) {
            throw new BadRequestException("Application cannot be rejected in its current state: "
                    + application.getStatus().getDisplayName());
        }

        // Validate rejection reason
        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BadRequestException("Rejection reason is required");
        }

        // Reject application
        application.reject(reviewer, request.getRejectionReason(), request.getReviewNotes());
        applicationRepository.save(application);

        log.info("Kuppi Student application {} rejected by {} for student {}",
                applicationId, currentUserId, application.getStudent().getStudentId());

        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public KuppiApplicationResponse markUnderReview(Long applicationId) {
        Long currentUserId = securityService.getCurrentUserId();
        BaseUser reviewer = findUserById(currentUserId);
        KuppiApplication application = findApplicationById(applicationId);

        // Validate application status
        if (application.getStatus() != KuppiApplicationStatus.PENDING) {
            throw new BadRequestException("Only pending applications can be marked as under review");
        }

        application.markUnderReview(reviewer);
        applicationRepository.save(application);

        log.info("Kuppi Student application {} marked as under review by {}", applicationId, currentUserId);

        return applicationMapper.toResponse(application);
    }

    @Override
    public KuppiApplicationStatsResponse getApplicationStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalKuppiStudents = studentRepository.countByStudentRoleTypesContaining(StudentRoleType.KUPPI_STUDENT);

        return KuppiApplicationStatsResponse.builder()
                .totalApplications(applicationRepository.countTotalApplications())
                .pendingApplications(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.PENDING))
                .underReviewApplications(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.UNDER_REVIEW))
                .approvedApplications(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.APPROVED))
                .rejectedApplications(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.REJECTED))
                .cancelledApplications(applicationRepository.countByStatusAndIsDeletedFalse(KuppiApplicationStatus.CANCELLED))
                .applicationsToday(applicationRepository.countApplicationsSubmittedToday(startOfDay))
                .totalKuppiStudents(totalKuppiStudents)
                .build();
    }

    // ==================== Super Admin Operations ====================

    @Override
    @Transactional
    public void permanentlyDeleteApplication(Long applicationId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiApplication", "id", applicationId));

        applicationRepository.delete(application);

        log.info("Kuppi Student application {} permanently deleted by super admin {}",
                applicationId, currentUserId);
    }

    @Override
    @Transactional
    public void revokeKuppiStudentRole(Long studentId, String reason) {
        Long currentUserId = securityService.getCurrentUserId();
        Student student = findStudentById(studentId);

        // Check if student has Kuppi capability
        if (!student.hasKuppiCapability()) {
            throw new BadRequestException("Student is not a Kuppi Student");
        }

        // Remove KUPPI_STUDENT role
        student.removeRoleType(StudentRoleType.KUPPI_STUDENT);
        student.removeRoleType(StudentRoleType.SENIOR_KUPPI); // Also remove deprecated role if exists

        // Clear Kuppi-specific fields
        student.setKuppiSubjects(null);
        student.setKuppiExperienceLevel(null);
        student.setKuppiAvailability(null);

        studentRepository.save(student);

        log.info("Kuppi Student role revoked from student {} by super admin {}. Reason: {}",
                student.getStudentId(), currentUserId, reason);
    }

    // ==================== Helper Methods ====================

    private KuppiApplication findApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("KuppiApplication", "id", applicationId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private BaseUser findUserById(Long userId) {
        // For reviewers (Admin/Academic Staff), get current authenticated user
        return securityService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private boolean isCurrentUserNotStudent() {
        return securityService.getCurrentUser()
                .map(user -> user.getRole() != UserRole.ROLE_STUDENT)
                .orElse(true);
    }

    private void validateCanApply(Student student) {
        // Check if already a Kuppi Student
        if (student.hasKuppiCapability()) {
            throw new BadRequestException("You are already a Kuppi Student");
        }

        // Check if has active application
        if (applicationRepository.hasActiveApplication(student.getId())) {
            throw new BadRequestException("You already have a pending or under review application");
        }
    }

    private void grantKuppiStudentRole(Student student, KuppiApplication application) {
        // Add KUPPI_STUDENT role to student's role types
        student.addRoleType(StudentRoleType.KUPPI_STUDENT);

        // Set Kuppi-specific fields from application
        // IMPORTANT: Create new HashSet to avoid shared collection reference error
        if (application != null) {
            if (application.getSubjectsToTeach() != null) {
                student.setKuppiSubjects(new HashSet<>(application.getSubjectsToTeach()));
            }
            student.setKuppiExperienceLevel(application.getPreferredExperienceLevel());
            student.setKuppiAvailability(application.getAvailability());
        }

        studentRepository.save(student);

        log.info("KUPPI_STUDENT role granted to student {}", student.getStudentId());
    }

    private PagedResponse<KuppiApplicationResponse> toPagedResponse(Page<KuppiApplication> applications) {
        List<KuppiApplicationResponse> content = applications.getContent().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<KuppiApplicationResponse>builder()
                .content(content)
                .pageNumber(applications.getNumber())
                .pageSize(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .first(applications.isFirst())
                .last(applications.isLast())
                .empty(applications.isEmpty())
                .build();
    }
}



