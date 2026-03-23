package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.InfoResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for general info categories — course details, houses, students' union, clubs.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.INFO)
@RequiredArgsConstructor
@Tag(name = "Info", description = "General institutional information — courses, houses, students' union, clubs & societies")
@SecurityRequirement(name = "bearerAuth")
public class InfoController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all info categories",
            description = "Returns a summary list of all general information categories")
    public ApiResponse<List<InfoResponse>> getAll() {
        log.debug("Fetching all info categories");
        List<InfoResponse> infoCategories = contentService.getAllInfoCategories();
        return ApiResponse.success("Info categories retrieved successfully", infoCategories);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get info category by slug",
            description = "Returns full details for a specific info category including nested data")
    public ApiResponse<InfoResponse> getBySlug(
            @Parameter(description = "Info category slug identifier", example = "course-details")
            @PathVariable String slug) {
        log.debug("Fetching info category: {}", slug);
        InfoResponse info = contentService.getInfoBySlug(slug);
        return ApiResponse.success("Info category retrieved successfully", info);
    }
}
