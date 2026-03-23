package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.ProgramResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for undergraduate degree programmes.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.UNDERGRADUATE)
@RequiredArgsConstructor
@Tag(name = "Undergraduate Programs", description = "Undergraduate degree programme information")
@SecurityRequirement(name = "bearerAuth")
public class UndergraduateController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all undergraduate programs",
            description = "Returns a summary list of all active undergraduate degree programmes")
    public ApiResponse<List<ProgramResponse>> getAll() {
        log.debug("Fetching all undergraduate programs");
        List<ProgramResponse> programs = contentService.getAllUndergraduatePrograms();
        return ApiResponse.success("Undergraduate programs retrieved successfully", programs);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get undergraduate program by slug",
            description = "Returns full programme details including modules, entry requirements, and career prospects")
    public ApiResponse<ProgramResponse> getBySlug(
            @Parameter(description = "Program slug identifier", example = "bsc-ai-ds")
            @PathVariable String slug) {
        log.debug("Fetching undergraduate program: {}", slug);
        ProgramResponse program = contentService.getUndergraduateProgramBySlug(slug);
        return ApiResponse.success("Undergraduate program retrieved successfully", program);
    }
}
