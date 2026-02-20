package lk.iit.nextora.module.kuppi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.kuppi.service.KuppiApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.KUPPI_SUPER_ADMIN)
@RequiredArgsConstructor
public class KuppiSuperAdminController {

    private final KuppiApplicationService applicationService;

    // ==================== Application Management ====================

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_PERMANENT)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:PERMANENT_DELETE')")
    @Operation(
            summary = "Permanently delete application",
            description = "DANGER: Permanently delete an application from the database. " +
                    "This action is IRREVERSIBLE. Super Admin only."
    )
    public ApiResponse<Void> permanentlyDeleteApplication(@PathVariable Long applicationId) {
        applicationService.permanentlyDeleteApplication(applicationId);
        return ApiResponse.success("Application permanently deleted", null);
    }

    @DeleteMapping(ApiConstants.KUPPI_ADMIN_APPLICATION_REVOKE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('KUPPI_APPLICATION:REVOKE')")
    @Operation(
            summary = "Revoke Kuppi Student role",
            description = "Revoke the Kuppi Student role from a student. " +
                    "The student will no longer be able to create sessions or upload notes. " +
                    "Super Admin only."
    )
    public ApiResponse<Void> revokeKuppiStudentRole(
            @PathVariable Long studentId,
            @Parameter(description = "Reason for revocation") @RequestParam String reason) {

        applicationService.revokeKuppiStudentRole(studentId, reason);
        return ApiResponse.success("Kuppi Student role revoked successfully", null);
    }
}
