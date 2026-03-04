package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.AcademicCalendarResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for academic calendars of partner universities.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.ACADEMIC_CALENDARS)
@RequiredArgsConstructor
@Tag(name = "Academic Calendars", description = "Academic calendar information for partner universities")
@SecurityRequirement(name = "bearerAuth")
public class AcademicCalendarsController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all academic calendars",
            description = "Returns a summary list of academic calendars for all partner universities")
    public ApiResponse<List<AcademicCalendarResponse>> getAll() {
        log.debug("Fetching all academic calendars");
        List<AcademicCalendarResponse> calendars = contentService.getAllAcademicCalendars();
        return ApiResponse.success("Academic calendars retrieved successfully", calendars);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get academic calendar by university slug",
            description = "Returns full calendar details including events for a specific university")
    public ApiResponse<AcademicCalendarResponse> getBySlug(
            @Parameter(description = "University slug identifier", example = "uow")
            @PathVariable String slug) {
        log.debug("Fetching academic calendar: {}", slug);
        AcademicCalendarResponse calendar = contentService.getAcademicCalendarBySlug(slug);
        return ApiResponse.success("Academic calendar retrieved successfully", calendar);
    }
}
