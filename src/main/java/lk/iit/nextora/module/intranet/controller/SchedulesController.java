package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.ScheduleCategoryResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for schedule categories — orientation, temporary, assessments, annual events.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.SCHEDULES)
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Schedule categories including orientation, temporary changes, assessments, and annual events")
@SecurityRequirement(name = "bearerAuth")
public class SchedulesController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all schedule categories",
            description = "Returns a summary list of all active schedule categories")
    public ApiResponse<List<ScheduleCategoryResponse>> getAll() {
        log.debug("Fetching all schedule categories");
        List<ScheduleCategoryResponse> categories = contentService.getAllScheduleCategories();
        return ApiResponse.success("Schedule categories retrieved successfully", categories);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get schedule category by slug",
            description = "Returns full schedule category details including all events")
    public ApiResponse<ScheduleCategoryResponse> getBySlug(
            @Parameter(description = "Schedule category slug identifier", example = "orientation")
            @PathVariable String slug) {
        log.debug("Fetching schedule category: {}", slug);
        ScheduleCategoryResponse category = contentService.getScheduleCategoryBySlug(slug);
        return ApiResponse.success("Schedule category retrieved successfully", category);
    }
}

