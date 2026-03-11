package lk.iit.nextora.module.intranet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.intranet.dto.MitigationFormResponse;
import lk.iit.nextora.module.intranet.service.IntranetContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for mitigating circumstances forms (UoW and RGU programmes).
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.MITIGATION_FORMS)
@RequiredArgsConstructor
@Tag(name = "Mitigation Forms", description = "Mitigating circumstances forms for UoW and RGU programmes")
@SecurityRequirement(name = "bearerAuth")
public class MitigationFormsController {

    private final IntranetContentService contentService;

    @GetMapping
    @Operation(summary = "Get all mitigation forms",
            description = "Returns a summary list of all available mitigating circumstances forms")
    public ApiResponse<List<MitigationFormResponse>> getAll() {
        log.debug("Fetching all mitigation forms");
        List<MitigationFormResponse> forms = contentService.getAllMitigationForms();
        return ApiResponse.success("Mitigation forms retrieved successfully", forms);
    }

    @GetMapping(ApiConstants.BY_SLUG)
    @Operation(summary = "Get mitigation form by slug",
            description = "Returns full form details including eligible circumstances, required documents, and outcomes")
    public ApiResponse<MitigationFormResponse> getBySlug(
            @Parameter(description = "Form slug identifier", example = "uow-mitigating-circumstances-form")
            @PathVariable String slug) {
        log.debug("Fetching mitigation form: {}", slug);
        MitigationFormResponse form = contentService.getMitigationFormBySlug(slug);
        return ApiResponse.success("Mitigation form retrieved successfully", form);
    }
}
