package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.FoundationProgramResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for foundation programme information categories.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.FOUNDATION_PROGRAM)
@RequiredArgsConstructor
@Tag(name = "Foundation Program", description = "Foundation programme information — calendar, timetable, assessments, LMS, mitigation")
@SecurityRequirement(name = "bearerAuth")
public class FoundationProgramController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all foundation program categories",
            description = "Returns a summary list of all foundation programme information categories")
    public ApiResponse<List<FoundationProgramResponse>> getAll() {
        log.debug("Fetching all foundation program categories");
        List<FoundationProgramResponse> categories = contentService.getAllFoundationProgramCategories();
        return ApiResponse.success("Foundation program categories retrieved successfully", categories);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get foundation program category by slug",
            description = "Returns full details for a specific foundation programme category (e.g., academic-calendar, time-table)")
    public ApiResponse<FoundationProgramResponse> getBySlug(
            @Parameter(description = "Category slug identifier", example = "academic-calendar")
            @PathVariable String slug) {
        log.debug("Fetching foundation program category: {}", slug);
        FoundationProgramResponse category = contentService.getFoundationProgramBySlug(slug);
        return ApiResponse.success("Foundation program category retrieved successfully", category);
    }
}


