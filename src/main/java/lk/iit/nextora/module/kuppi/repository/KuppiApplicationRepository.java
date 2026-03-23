package lk.iit.nextora.module.kuppi.repository;

import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.kuppi.entity.KuppiApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Kuppi Student applications
 */
@Repository
public interface KuppiApplicationRepository extends JpaRepository<KuppiApplication, Long> {

    // ==================== Find by Student ====================

    /**
     * Find all applications by student
     */
    List<KuppiApplication> findByStudentOrderByCreatedAtDesc(Student student);

    /**
     * Find applications by student ID
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.student.id = :studentId ORDER BY ka.createdAt DESC")
    List<KuppiApplication> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find active (non-final) application by student
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.student.id = :studentId " +
            "AND ka.status IN ('PENDING', 'UNDER_REVIEW') " +
            "AND ka.isDeleted = false")
    Optional<KuppiApplication> findActiveApplicationByStudentId(@Param("studentId") Long studentId);

    /**
     * Check if student has pending or under review application
     */
    @Query("SELECT COUNT(ka) > 0 FROM KuppiApplication ka WHERE ka.student.id = :studentId " +
            "AND ka.status IN ('PENDING', 'UNDER_REVIEW') AND ka.isDeleted = false")
    boolean hasActiveApplication(@Param("studentId") Long studentId);

    /**
     * Check if student was ever approved as Kuppi Student
     */
    @Query("SELECT COUNT(ka) > 0 FROM KuppiApplication ka WHERE ka.student.id = :studentId " +
            "AND ka.status = 'APPROVED' AND ka.isDeleted = false")
    boolean hasApprovedApplication(@Param("studentId") Long studentId);

    // ==================== Find by Status ====================

    /**
     * Find all applications by status with pagination
     */
    Page<KuppiApplication> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            KuppiApplicationStatus status, Pageable pageable);

    /**
     * Find all pending applications
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.status = 'PENDING' " +
            "AND ka.isDeleted = false ORDER BY ka.createdAt ASC")
    Page<KuppiApplication> findPendingApplications(Pageable pageable);

    /**
     * Find all applications under review
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.status = 'UNDER_REVIEW' " +
            "AND ka.isDeleted = false ORDER BY ka.createdAt ASC")
    Page<KuppiApplication> findUnderReviewApplications(Pageable pageable);

    /**
     * Find pending and under review applications
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.status IN ('PENDING', 'UNDER_REVIEW') " +
            "AND ka.isDeleted = false ORDER BY ka.createdAt ASC")
    Page<KuppiApplication> findActiveApplications(Pageable pageable);

    // ==================== Statistics ====================

    /**
     * Count applications by status
     */
    long countByStatusAndIsDeletedFalse(KuppiApplicationStatus status);

    /**
     * Count total applications
     */
    @Query("SELECT COUNT(ka) FROM KuppiApplication ka WHERE ka.isDeleted = false")
    long countTotalApplications();

    /**
     * Count applications submitted today
     */
    @Query("SELECT COUNT(ka) FROM KuppiApplication ka WHERE ka.submittedAt >= :startOfDay " +
            "AND ka.isDeleted = false")
    long countApplicationsSubmittedToday(@Param("startOfDay") LocalDateTime startOfDay);

    // ==================== Search ====================

    /**
     * Search applications by student name or email
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.isDeleted = false AND " +
            "(LOWER(ka.student.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ka.student.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ka.student.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ka.student.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KuppiApplication> searchApplications(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Filter applications by status list
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.status IN :statuses " +
            "AND ka.isDeleted = false ORDER BY ka.createdAt DESC")
    Page<KuppiApplication> findByStatusIn(@Param("statuses") List<KuppiApplicationStatus> statuses, Pageable pageable);

    // ==================== By Reviewer ====================

    /**
     * Find applications reviewed by a specific user
     */
    @Query("SELECT ka FROM KuppiApplication ka WHERE ka.reviewedBy.id = :reviewerId " +
            "AND ka.isDeleted = false ORDER BY ka.reviewedAt DESC")
    Page<KuppiApplication> findByReviewerId(@Param("reviewerId") Long reviewerId, Pageable pageable);
}

