package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.StaffResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for staff categories — SOC, mail groups, document archive, contacts, etc.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.STAFF)
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Staff directory, departments, mail groups, and document archive")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all staff categories",
            description = "Returns a summary list of all staff information categories")
    public ApiResponse<List<StaffResponse>> getAll() {
        log.debug("Fetching all staff categories");
        List<StaffResponse> staffCategories = contentService.getAllStaffCategories();
        return ApiResponse.success("Staff categories retrieved successfully", staffCategories);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get staff category by slug",
            description = "Returns full details for a specific staff category including members, contacts, or documents")
    public ApiResponse<StaffResponse> getBySlug(
            @Parameter(description = "Staff category slug identifier", example = "soc")
            @PathVariable String slug) {
        log.debug("Fetching staff category: {}", slug);
        StaffResponse staff = contentService.getStaffBySlug(slug);
        return ApiResponse.success("Staff category retrieved successfully", staff);
    }
}
