package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.StudentsRelationsUnitResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Students Relations Unit (SRU) — help desk, video resources, etc.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.STUDENTS_RELATIONS_UNIT)
@RequiredArgsConstructor
@Tag(name = "Students Relations Unit", description = "Students Relations Unit information, help desk, and video resources")
@SecurityRequirement(name = "bearerAuth")
public class StudentsRelationsUnitController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get Students Relations Unit info",
            description = "Returns root-level SRU information including contact details and available sub-categories")
    public ApiResponse<StudentsRelationsUnitResponse> getInfo() {
        log.debug("Fetching Students Relations Unit info");
        StudentsRelationsUnitResponse sruInfo = contentService.getStudentsRelationsUnitInfo();
        return ApiResponse.success("Students Relations Unit info retrieved successfully", sruInfo);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get SRU category by slug",
            description = "Returns details for a specific SRU sub-category, e.g. help-desk-video-series")
    public ApiResponse<StudentsRelationsUnitResponse> getBySlug(
            @Parameter(description = "SRU category slug identifier", example = "help-desk-video-series")
            @PathVariable String slug) {
        log.debug("Fetching SRU category: {}", slug);
        StudentsRelationsUnitResponse sruCategory = contentService.getStudentsRelationsUnitBySlug(slug);
        return ApiResponse.success("SRU category retrieved successfully", sruCategory);
    }
}
