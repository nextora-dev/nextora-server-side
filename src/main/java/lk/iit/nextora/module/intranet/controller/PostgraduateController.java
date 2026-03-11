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
 * REST controller for postgraduate degree programmes.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.POSTGRADUATE)
@RequiredArgsConstructor
@Tag(name = "Postgraduate Programs", description = "Postgraduate degree programme information")
@SecurityRequirement(name = "bearerAuth")
public class PostgraduateController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all postgraduate programs",
            description = "Returns a summary list of all active postgraduate degree programmes")
    public ApiResponse<List<ProgramResponse>> getAll() {
        log.debug("Fetching all postgraduate programs");
        List<ProgramResponse> programs = contentService.getAllPostgraduatePrograms();
        return ApiResponse.success("Postgraduate programs retrieved successfully", programs);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get postgraduate program by slug",
            description = "Returns full programme details including modules, entry requirements, and career prospects")
    public ApiResponse<ProgramResponse> getBySlug(
            @Parameter(description = "Program slug identifier", example = "msc-ase")
            @PathVariable String slug) {
        log.debug("Fetching postgraduate program: {}", slug);
        ProgramResponse program = contentService.getPostgraduateProgramBySlug(slug);
        return ApiResponse.success("Postgraduate program retrieved successfully", program);
    }
}
