package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.module.auth.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends BaseUserRepository<Student> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);

    /**
     * Count students who have a specific role type
     */
    @Query("SELECT COUNT(s) FROM Student s JOIN s.studentRoleTypes rt WHERE rt = :roleType")
    long countByStudentRoleTypesContaining(@Param("roleType") StudentRoleType roleType);

    // ==================== Kuppi Student Queries ====================

    /**
     * Find all students who have KUPPI_STUDENT or SENIOR_KUPPI role type (active Kuppi students)
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.studentRoleTypes rt " +
           "WHERE (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isActive = true AND s.isDeleted = false " +
           "ORDER BY s.kuppiRating DESC NULLS LAST")
    Page<Student> findAllKuppiStudents(Pageable pageable);

    /**
     * Find Kuppi student by ID (must have KUPPI_STUDENT or SENIOR_KUPPI role)
     */
    @Query("SELECT s FROM Student s JOIN s.studentRoleTypes rt " +
           "WHERE s.id = :studentId " +
           "AND (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isDeleted = false")
    Optional<Student> findKuppiStudentById(@Param("studentId") Long studentId);

    /**
     * Search Kuppi students by name (first name or last name)
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.studentRoleTypes rt " +
           "WHERE (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isActive = true AND s.isDeleted = false " +
           "AND (LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Student> searchKuppiStudentsByName(@Param("name") String name, Pageable pageable);

    /**
     * Search Kuppi students by subject they teach
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.studentRoleTypes rt JOIN s.kuppiSubjects ks " +
           "WHERE (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isActive = true AND s.isDeleted = false " +
           "AND LOWER(ks) LIKE LOWER(CONCAT('%', :subject, '%'))")
    Page<Student> searchKuppiStudentsBySubject(@Param("subject") String subject, Pageable pageable);

    /**
     * Find Kuppi students by faculty
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.studentRoleTypes rt " +
           "WHERE (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isActive = true AND s.isDeleted = false " +
           "AND s.faculty = :faculty")
    Page<Student> findKuppiStudentsByFaculty(@Param("faculty") FacultyType faculty, Pageable pageable);

    /**
     * Find top-rated Kuppi students (ordered by rating descending)
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.studentRoleTypes rt " +
           "WHERE (rt = lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT " +
           "OR rt = lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI) " +
           "AND s.isActive = true AND s.isDeleted = false " +
           "AND s.kuppiRating IS NOT NULL " +
           "ORDER BY s.kuppiRating DESC")
    Page<Student> findTopRatedKuppiStudents(Pageable pageable);
}
