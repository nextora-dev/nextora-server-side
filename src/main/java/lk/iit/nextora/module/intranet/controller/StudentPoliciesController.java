package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.StudentPolicyResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for student policies — code of conduct, IT policy, club policy, etc.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.STUDENT_POLICIES)
@RequiredArgsConstructor
@Tag(name = "Student Policies", description = "Institutional student policies and regulations")
@SecurityRequirement(name = "bearerAuth")
public class StudentPoliciesController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all student policies",
            description = "Returns a summary list of all active student policies")
    public ApiResponse<List<StudentPolicyResponse>> getAll() {
        log.debug("Fetching all student policies");
        List<StudentPolicyResponse> policies = contentService.getAllStudentPolicies();
        return ApiResponse.success("Student policies retrieved successfully", policies);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get student policy by slug",
            description = "Returns full policy content including key points, disciplinary process, and contact information")
    public ApiResponse<StudentPolicyResponse> getBySlug(
            @Parameter(description = "Policy slug identifier", example = "code-of-conduct")
            @PathVariable String slug) {
        log.debug("Fetching student policy: {}", slug);
        StudentPolicyResponse policy = contentService.getStudentPolicyBySlug(slug);
        return ApiResponse.success("Student policy retrieved successfully", policy);
    }
}
