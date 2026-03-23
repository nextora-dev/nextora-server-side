package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.StudentComplaintCategoryResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for student complaint categories.
 * Provides endpoints to list all categories and retrieve details by slug.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.STUDENT_COMPLAINTS)
@RequiredArgsConstructor
@Tag(name = "Student Complaints", description = "Student complaint categories and submission information")
@SecurityRequirement(name = "bearerAuth")
public class StudentComplaintsController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all complaint categories",
            description = "Returns a summary list of all active student complaint categories")
    public ApiResponse<List<StudentComplaintCategoryResponse>> getAllCategories() {
        log.debug("Fetching all student complaint categories");
        List<StudentComplaintCategoryResponse> categories = contentService.getAllStudentComplaintCategories();
        return ApiResponse.success("Student complaint categories retrieved successfully", categories);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get complaint category by slug",
            description = "Returns full details and instructions for a specific complaint category")
    public ApiResponse<StudentComplaintCategoryResponse> getBySlug(
            @Parameter(description = "Category slug identifier", example = "academic-course-delivery")
            @PathVariable String slug) {
        log.debug("Fetching student complaint category: {}", slug);
        StudentComplaintCategoryResponse category = contentService.getStudentComplaintBySlug(slug);
        return ApiResponse.success("Student complaint category retrieved successfully", category);
    }
}
