package lk.iit.nextora.module.kuppi.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.request.ReviewKuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationStatsResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Kuppi Student application operations.
 * Handles the workflow for students applying to become Kuppi Students.
 */
public interface KuppiApplicationService {

    // ==================== Student Operations ====================

    /**
     * Submit a new Kuppi Student application
     * Only students without active applications can apply
     */
    KuppiApplicationResponse submitApplication(KuppiApplicationRequest request);

    /**
     * Get current user's applications
     */
    List<KuppiApplicationResponse> getMyApplications();

    /**
     * Get current user's active application (pending/under review)
     */
    KuppiApplicationResponse getMyActiveApplication();

    /**
     * Cancel own application (only if pending/under review)
     */
    void cancelMyApplication(Long applicationId);

    /**
     * Check if current user can apply (no active application exists)
     */
    boolean canApply();

    /**
     * Check if current user is already a Kuppi Student
     */
    boolean isKuppiStudent();

    // ==================== Admin/Academic Staff Operations ====================

    /**
     * Get all applications with pagination
     */
    PagedResponse<KuppiApplicationResponse> getAllApplications(Pageable pageable);

    /**
     * Get applications by status
     */
    PagedResponse<KuppiApplicationResponse> getApplicationsByStatus(KuppiApplicationStatus status, Pageable pageable);

    /**
     * Get pending applications (awaiting review)
     */
    PagedResponse<KuppiApplicationResponse> getPendingApplications(Pageable pageable);

    /**
     * Get active applications (pending + under review)
     */
    PagedResponse<KuppiApplicationResponse> getActiveApplications(Pageable pageable);

    /**
     * Get application by ID
     */
    KuppiApplicationResponse getApplicationById(Long applicationId);

    /**
     * Search applications by student name/email/ID
     */
    PagedResponse<KuppiApplicationResponse> searchApplications(String keyword, Pageable pageable);

    /**
     * Approve an application - grants KUPPI_STUDENT role to the student
     */
    KuppiApplicationResponse approveApplication(Long applicationId, ReviewKuppiApplicationRequest request);

    /**
     * Reject an application
     */
    KuppiApplicationResponse rejectApplication(Long applicationId, ReviewKuppiApplicationRequest request);

    /**
     * Mark application as under review
     */
    KuppiApplicationResponse markUnderReview(Long applicationId);

    /**
     * Get application statistics
     */
    KuppiApplicationStatsResponse getApplicationStats();

    // ==================== Super Admin Operations ====================

    /**
     * Permanently delete an application (Super Admin only)
     */
    void permanentlyDeleteApplication(Long applicationId);

    /**
     * Revoke Kuppi Student role from a student
     */
    void revokeKuppiStudentRole(Long studentId, String reason);
}
