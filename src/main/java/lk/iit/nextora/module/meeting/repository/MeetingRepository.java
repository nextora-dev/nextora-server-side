package lk.iit.nextora.module.meeting.repository;

import lk.iit.nextora.common.enums.MeetingStatus;
import lk.iit.nextora.common.enums.MeetingType;
import lk.iit.nextora.module.meeting.entity.Meeting;
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
 * Repository for Meeting entity.
 * Provides database access methods for student-lecturer meeting operations.
 *
 * <h3>Query Categories:</h3>
 * <ul>
 *   <li>Basic Finders - Find by ID with eager loading</li>
 *   <li>Student Queries - Queries for student's meeting requests</li>
 *   <li>Lecturer Queries - Queries for lecturer's meeting management</li>
 *   <li>Status-based Queries - For scheduled tasks and filtering</li>
 *   <li>Search Queries - Full-text search functionality</li>
 *   <li>Count Queries - For statistics and dashboard</li>
 *   <li>Validation Queries - Conflict detection</li>
 *   <li>Admin Queries - Platform-wide queries</li>
 * </ul>
 *
 * @author Nextora Development Team
 * @version 2.0
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // ==================== Basic Finders ====================

    /**
     * Find meeting by ID (not deleted)
     */
    Optional<Meeting> findByIdAndIsDeletedFalse(Long id);

    /**
     * Find meeting by ID with student and lecturer eagerly loaded
     */
    @Query("SELECT m FROM Meeting m " +
           "LEFT JOIN FETCH m.student " +
           "LEFT JOIN FETCH m.lecturer " +
           "WHERE m.id = :id AND m.isDeleted = false")
    Optional<Meeting> findByIdWithDetails(@Param("id") Long id);

    // ==================== Student Queries ====================

    /**
     * Find all meetings requested by a student
     */
    Page<Meeting> findByStudentIdAndIsDeletedFalse(Long studentId, Pageable pageable);

    /**
     * Find meetings by student and status
     */
    Page<Meeting> findByStudentIdAndStatusAndIsDeletedFalse(Long studentId, MeetingStatus status, Pageable pageable);

    /**
     * Find meetings by student and status list
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.student.id = :studentId AND m.status IN :statuses AND m.isDeleted = false")
    Page<Meeting> findByStudentIdAndStatusIn(@Param("studentId") Long studentId,
                                              @Param("statuses") List<MeetingStatus> statuses,
                                              Pageable pageable);

    /**
     * Find upcoming scheduled meetings for a student
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.student.id = :studentId " +
           "AND m.scheduledStartTime > :now " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND m.isDeleted = false " +
           "ORDER BY m.scheduledStartTime ASC")
    Page<Meeting> findUpcomingMeetingsForStudent(@Param("studentId") Long studentId,
                                                  @Param("now") LocalDateTime now,
                                                  Pageable pageable);

    // ==================== Lecturer Queries ====================

    /**
     * Find all meeting requests for a lecturer
     */
    Page<Meeting> findByLecturerIdAndIsDeletedFalse(Long lecturerId, Pageable pageable);

    /**
     * Find meetings by lecturer and status
     */
    Page<Meeting> findByLecturerIdAndStatusAndIsDeletedFalse(Long lecturerId, MeetingStatus status, Pageable pageable);

    /**
     * Find pending meeting requests for a lecturer (awaiting response)
     * Ordered by priority (highest first) then by creation date
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.status = 'PENDING' " +
           "AND m.isDeleted = false " +
           "ORDER BY m.priority DESC, m.createdAt ASC")
    Page<Meeting> findPendingRequestsForLecturer(@Param("lecturerId") Long lecturerId, Pageable pageable);

    /**
     * Find high priority pending requests for a lecturer (priority >= 3)
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.status = 'PENDING' " +
           "AND m.priority >= 3 " +
           "AND m.isDeleted = false " +
           "ORDER BY m.priority DESC, m.createdAt ASC")
    Page<Meeting> findHighPriorityRequestsForLecturer(@Param("lecturerId") Long lecturerId, Pageable pageable);

    /**
     * Find upcoming scheduled meetings for a lecturer
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.scheduledStartTime > :now " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND m.isDeleted = false " +
           "ORDER BY m.scheduledStartTime ASC")
    Page<Meeting> findUpcomingMeetingsForLecturer(@Param("lecturerId") Long lecturerId,
                                                   @Param("now") LocalDateTime now,
                                                   Pageable pageable);

    /**
     * Find meetings for lecturer's calendar (scheduled meetings within date range)
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.scheduledStartTime BETWEEN :startDate AND :endDate " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED', 'IN_PROGRESS', 'COMPLETED') " +
           "AND m.isDeleted = false " +
           "ORDER BY m.scheduledStartTime ASC")
    List<Meeting> findLecturerCalendarMeetings(@Param("lecturerId") Long lecturerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find meetings requiring follow-up for a lecturer
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.followUpRequired = true " +
           "AND m.status = 'COMPLETED' " +
           "AND m.isDeleted = false " +
           "ORDER BY m.actualEndTime DESC")
    Page<Meeting> findMeetingsRequiringFollowUp(@Param("lecturerId") Long lecturerId, Pageable pageable);

    // ==================== Status-based Queries ====================

    /**
     * Find all meetings by status
     */
    Page<Meeting> findByStatusAndIsDeletedFalse(MeetingStatus status, Pageable pageable);

    /**
     * Find meetings that should be auto-started (scheduled time has passed)
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.scheduledStartTime <= :now " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND m.isDeleted = false")
    List<Meeting> findMeetingsToStart(@Param("now") LocalDateTime now);

    /**
     * Find meetings that should be auto-completed (end time has passed)
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.scheduledEndTime <= :now " +
           "AND m.status = 'IN_PROGRESS' " +
           "AND m.isDeleted = false")
    List<Meeting> findMeetingsToComplete(@Param("now") LocalDateTime now);

    /**
     * Find meetings needing 24-hour reminder
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.scheduledStartTime BETWEEN :now AND :reminderTime " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND m.reminderSent = false " +
           "AND m.isDeleted = false")
    List<Meeting> findMeetingsNeedingReminder(@Param("now") LocalDateTime now,
                                               @Param("reminderTime") LocalDateTime reminderTime);

    /**
     * Find meetings needing 1-hour reminder
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.scheduledStartTime BETWEEN :now AND :reminderTime " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND m.finalReminderSent = false " +
           "AND m.isDeleted = false")
    List<Meeting> findMeetingsNeedingFinalReminder(@Param("now") LocalDateTime now,
                                                    @Param("reminderTime") LocalDateTime reminderTime);

    // ==================== Search Queries ====================

    /**
     * Search meetings by subject for a student
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.student.id = :studentId " +
           "AND (LOWER(m.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Meeting> searchBySubjectForStudent(@Param("studentId") Long studentId,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);

    /**
     * Search meetings by subject or student name for a lecturer
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND (LOWER(m.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(m.student.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(m.student.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Meeting> searchForLecturer(@Param("lecturerId") Long lecturerId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    // ==================== Count Queries ====================

    /**
     * Count meetings by student
     */
    long countByStudentIdAndIsDeletedFalse(Long studentId);

    /**
     * Count meetings by lecturer
     */
    long countByLecturerIdAndIsDeletedFalse(Long lecturerId);

    /**
     * Count meetings by status for a lecturer
     */
    long countByLecturerIdAndStatusAndIsDeletedFalse(Long lecturerId, MeetingStatus status);

    /**
     * Count meetings by meeting type for a lecturer
     */
    long countByLecturerIdAndMeetingTypeAndIsDeletedFalse(Long lecturerId, MeetingType meetingType);

    /**
     * Count online/in-person meetings for a lecturer (only scheduled/completed)
     */
    @Query("SELECT COUNT(m) FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.isOnline = :isOnline " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED', 'COMPLETED') " +
           "AND m.isDeleted = false")
    long countByLecturerIdAndIsOnline(@Param("lecturerId") Long lecturerId,
                                       @Param("isOnline") Boolean isOnline);

    /**
     * Count pending requests for a lecturer
     */
    @Query("SELECT COUNT(m) FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.status = 'PENDING' " +
           "AND m.isDeleted = false")
    long countPendingRequestsForLecturer(@Param("lecturerId") Long lecturerId);

    /**
     * Count meetings requiring follow-up for a lecturer
     */
    @Query("SELECT COUNT(m) FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.followUpRequired = true " +
           "AND m.status = 'COMPLETED' " +
           "AND m.isDeleted = false")
    long countMeetingsRequiringFollowUp(@Param("lecturerId") Long lecturerId);

    // ==================== Validation Queries ====================

    /**
     * Check if lecturer has conflicting meeting at the given time
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED', 'IN_PROGRESS') " +
           "AND m.isDeleted = false " +
           "AND ((m.scheduledStartTime <= :startTime AND m.scheduledEndTime > :startTime) " +
           "     OR (m.scheduledStartTime < :endTime AND m.scheduledEndTime >= :endTime) " +
           "     OR (m.scheduledStartTime >= :startTime AND m.scheduledEndTime <= :endTime))")
    boolean hasConflictingMeeting(@Param("lecturerId") Long lecturerId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * Check if lecturer has conflicting meeting (excluding a specific meeting)
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.id != :excludeMeetingId " +
           "AND m.status IN ('SCHEDULED', 'RESCHEDULED', 'IN_PROGRESS') " +
           "AND m.isDeleted = false " +
           "AND ((m.scheduledStartTime <= :startTime AND m.scheduledEndTime > :startTime) " +
           "     OR (m.scheduledStartTime < :endTime AND m.scheduledEndTime >= :endTime) " +
           "     OR (m.scheduledStartTime >= :startTime AND m.scheduledEndTime <= :endTime))")
    boolean hasConflictingMeetingExcluding(@Param("lecturerId") Long lecturerId,
                                            @Param("excludeMeetingId") Long excludeMeetingId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    // ==================== Admin Queries ====================

    /**
     * Find all meetings (admin view)
     */
    Page<Meeting> findByIsDeletedFalse(Pageable pageable);

    /**
     * Count all meetings by status (platform-wide)
     */
    long countByStatusAndIsDeletedFalse(MeetingStatus status);

    /**
     * Count all meetings by meeting type (platform-wide)
     */
    long countByMeetingTypeAndIsDeletedFalse(MeetingType meetingType);

    // ==================== Feedback Queries ====================

    /**
     * Get average rating for a lecturer
     */
    @Query("SELECT AVG(m.studentRating) FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.studentRating IS NOT NULL " +
           "AND m.isDeleted = false")
    Double getAverageRatingForLecturer(@Param("lecturerId") Long lecturerId);

    /**
     * Count meetings with specific rating for a lecturer
     */
    @Query("SELECT COUNT(m) FROM Meeting m " +
           "WHERE m.lecturer.id = :lecturerId " +
           "AND m.studentRating = :rating " +
           "AND m.isDeleted = false")
    long countByLecturerIdAndRating(@Param("lecturerId") Long lecturerId, @Param("rating") Integer rating);
}
