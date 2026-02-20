package lk.iit.nextora.module.kuppi.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentDetailResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Kuppi Student operations.
 * Provides methods to view and retrieve Kuppi Student information.
 */
public interface KuppiStudentService {

    /**
     * Get all active Kuppi students with pagination.
     * Returns basic information suitable for listing.
     *
     * @param pageable pagination parameters
     * @return paginated list of Kuppi students
     */
    PagedResponse<KuppiStudentResponse> getAllKuppiStudents(Pageable pageable);

    /**
     * Get detailed information about a specific Kuppi student by ID.
     * Includes statistics, recent sessions, and comprehensive profile.
     *
     * @param studentId the ID of the student
     * @return detailed Kuppi student information
     */
    KuppiStudentDetailResponse getKuppiStudentById(Long studentId);

    /**
     * Search Kuppi students by name.
     *
     * @param name search query (first name or last name)
     * @param pageable pagination parameters
     * @return paginated list of matching Kuppi students
     */
    PagedResponse<KuppiStudentResponse> searchKuppiStudentsByName(String name, Pageable pageable);

    /**
     * Search Kuppi students by subject they teach.
     *
     * @param subject the subject to search for
     * @param pageable pagination parameters
     * @return paginated list of Kuppi students teaching the subject
     */
    PagedResponse<KuppiStudentResponse> searchKuppiStudentsBySubject(String subject, Pageable pageable);

    /**
     * Get Kuppi students by faculty.
     *
     * @param faculty the faculty to filter by
     * @param pageable pagination parameters
     * @return paginated list of Kuppi students in the faculty
     */
    PagedResponse<KuppiStudentResponse> getKuppiStudentsByFaculty(String faculty, Pageable pageable);

    /**
     * Get top rated Kuppi students.
     *
     * @param pageable pagination parameters
     * @return paginated list of top-rated Kuppi students
     */
    PagedResponse<KuppiStudentResponse> getTopRatedKuppiStudents(Pageable pageable);
}

