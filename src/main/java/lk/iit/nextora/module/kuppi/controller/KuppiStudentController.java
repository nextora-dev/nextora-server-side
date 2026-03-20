package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentDetailResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiStudentResponse;
import lk.iit.nextora.module.kuppi.service.KuppiStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Kuppi Student operations.
 *
 * Provides endpoints to:
 * - View all Kuppi students (students who can host Kuppi sessions)
 * - Get detailed information about a specific Kuppi student
 * - Search Kuppi students by name, subject, or faculty
 * - Get top-rated Kuppi students
 *
 * All endpoints require KUPPI:READ permission (available to all authenticated students).
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.KUPPI_STUDENTS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Students", description = "Endpoints for viewing Kuppi Students (session hosts)")
public class KuppiStudentController {

    private final KuppiStudentService kuppiStudentService;

    // ==================== View All Kuppi Students ====================

    @GetMapping
    @Operation(
            summary = "Get all Kuppi students",
            description = "Retrieve a paginated list of all active Kuppi students who can host sessions. " +
                          "Results are sorted by rating by default."
    )
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiStudentResponse>> getAllKuppiStudents(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "kuppiRating")
            @RequestParam(defaultValue = "kuppiRating") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("REST request to get all Kuppi students: page={}, size={}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<KuppiStudentResponse> response = kuppiStudentService.getAllKuppiStudents(pageable);

        return ApiResponse.success("Kuppi students retrieved successfully", response);
    }

    // ==================== Get Kuppi Student by ID ====================

    @GetMapping(ApiConstants.KUPPI_STUDENT_BY_ID)
    @Operation(
            summary = "Get Kuppi student by ID",
            description = "Retrieve detailed information about a specific Kuppi student, " +
                          "including their profile, statistics, recent sessions, and upcoming sessions."
    )
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<KuppiStudentDetailResponse> getKuppiStudentById(
            @Parameter(description = "ID of the Kuppi student", required = true, example = "1")
            @PathVariable Long studentId) {

        log.info("REST request to get Kuppi student details: studentId={}", studentId);

        KuppiStudentDetailResponse response = kuppiStudentService.getKuppiStudentById(studentId);

        return ApiResponse.success("Kuppi student details retrieved successfully", response);
    }

    // ==================== Search Kuppi Students ====================

    @GetMapping(ApiConstants.KUPPI_STUDENTS_SEARCH_NAME)
    @Operation(
            summary = "Search Kuppi students by name",
            description = "Search for Kuppi students by their first name or last name."
    )
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiStudentResponse>> searchByName(
            @Parameter(description = "Name to search for", required = true, example = "John")
            @RequestParam String name,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to search Kuppi students by name: name={}", name);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiStudentResponse> response = kuppiStudentService.searchKuppiStudentsByName(name, pageable);

        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_STUDENTS_SEARCH_SUBJECT)
    @Operation(
            summary = "Search Kuppi students by subject",
            description = "Search for Kuppi students who teach a specific subject."
    )
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiStudentResponse>> searchBySubject(
            @Parameter(description = "Subject to search for", required = true, example = "Mathematics")
            @RequestParam String subject,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to search Kuppi students by subject: subject={}", subject);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiStudentResponse> response = kuppiStudentService.searchKuppiStudentsBySubject(subject, pageable);

        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_STUDENTS_BY_FACULTY)
    @Operation(
            summary = "Get Kuppi students by faculty",
            description = "Retrieve Kuppi students belonging to a specific faculty."
    )
    @PreAuthorize("hasAuthority('KUPPI:READ')")
    public ApiResponse<PagedResponse<KuppiStudentResponse>> getByFaculty(
            @Parameter(description = "Faculty name", required = true, example = "COMPUTING")
            @PathVariable String faculty,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to get Kuppi students by faculty: faculty={}", faculty);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<KuppiStudentResponse> response = kuppiStudentService.getKuppiStudentsByFaculty(faculty, pageable);

        return ApiResponse.success("Kuppi students retrieved successfully", response);
    }
}

