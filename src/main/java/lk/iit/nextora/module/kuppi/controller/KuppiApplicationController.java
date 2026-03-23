package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.kuppi.dto.request.KuppiApplicationRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiApplicationResponse;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller for Student Kuppi Application operations.
 * Students can apply to become Kuppi Students through this controller.
 */
@RestController
@RequestMapping(ApiConstants.KUPPI_APPLICATIONS)
@RequiredArgsConstructor
@Tag(name = "Kuppi Applications - Student", description = "Endpoints for students to apply for Kuppi Student role")
@SecurityRequirement(name = "bearerAuth")
public class KuppiApplicationController {

    private final KuppiApplicationService applicationService;

    // ==================== Student Endpoints ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:SUBMIT')")
    @Operation(
            summary = "Submit Kuppi Student application",
            description = "Submit an application to become a Kuppi Student with academic results upload. " +
                    "Only students without active applications can apply. " +
                    "Already approved Kuppi Students cannot apply again. " +
                    "Accepted file types: PDF, JPG, JPEG, PNG (max 5MB)."
    )
    public ApiResponse<KuppiApplicationResponse> submitApplication(
            @Valid @RequestPart("application") KuppiApplicationRequest request,
            @RequestPart(value = "academicResults")
            @Schema(type = "string", format = "binary", description = "Academic results document (PDF or image)")
            MultipartFile academicResults) {
        KuppiApplicationResponse response = applicationService.submitApplication(request, academicResults);
        return ApiResponse.success("Application submitted successfully", response);
    }

    @GetMapping(ApiConstants.KUPPI_APPLICATION_MY)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_OWN')")
    @Operation(
            summary = "Get my applications",
            description = "Get all Kuppi Student applications submitted by the current user"
    )
    public ApiResponse<List<KuppiApplicationResponse>> getMyApplications() {
        List<KuppiApplicationResponse> applications = applicationService.getMyApplications();
        return ApiResponse.success("Applications retrieved successfully", applications);
    }

    @GetMapping(ApiConstants.KUPPI_APPLICATION_ACTIVE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_OWN')")
    @Operation(
            summary = "Get my active application",
            description = "Get the current user's pending or under-review application (if exists)"
    )
    public ApiResponse<KuppiApplicationResponse> getMyActiveApplication() {
        KuppiApplicationResponse response = applicationService.getMyActiveApplication();
        return ApiResponse.success("Active application retrieved", response);
    }

    @DeleteMapping(ApiConstants.KUPPI_APPLICATION_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:CANCEL')")
    @Operation(
            summary = "Cancel my application",
            description = "Cancel own application (only if pending or under review)"
    )
    public ApiResponse<Void> cancelMyApplication(@PathVariable Long applicationId) {
        applicationService.cancelMyApplication(applicationId);
        return ApiResponse.success("Application cancelled successfully", null);
    }

    @GetMapping(ApiConstants.KUPPI_APPLICATION_CAN_APPLY)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_OWN')")
    @Operation(
            summary = "Check if can apply",
            description = "Check if the current user can submit a new application. " +
                    "Returns false if already a Kuppi Student or has an active application."
    )
    public ApiResponse<Map<String, Boolean>> canApply() {
        boolean canApply = applicationService.canApply();
        return ApiResponse.success("Eligibility check completed", Map.of("canApply", canApply));
    }

    @GetMapping(ApiConstants.KUPPI_APPLICATION_IS_KUPPI_STUDENT)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:VIEW_OWN')")
    @Operation(
            summary = "Check if Kuppi Student",
            description = "Check if the current user is already a Kuppi Student"
    )
    public ApiResponse<Map<String, Boolean>> isKuppiStudent() {
        boolean isKuppiStudent = applicationService.isKuppiStudent();
        return ApiResponse.success("Status check completed", Map.of("isKuppiStudent", isKuppiStudent));
    }
}

