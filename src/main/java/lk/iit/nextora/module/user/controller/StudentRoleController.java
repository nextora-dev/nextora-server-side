package lk.iit.nextora.module.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.module.user.dto.request.AddStudentRoleRequest;
import lk.iit.nextora.module.user.dto.request.RemoveStudentRoleRequest;
import lk.iit.nextora.module.user.dto.response.StudentRoleResponse;
import lk.iit.nextora.module.user.service.StudentRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for student role management.
 *
 * Implements the Admin-Controlled Student Role Upgrade Flow:
 * - Admin creates NORMAL student account
 * - Student roles are progressively upgraded based on activities
 * - Roles are additive (cumulative), not replacements
 *
 * Role Upgrade Rules:
 * - Roles are never removed automatically
 * - New roles are added on top of existing roles
 * - Admin approval required for: BATCH_REP, KUPPI_STUDENT
 * - Students cannot assign roles to themselves
 */
@RestController
@RequestMapping(ApiConstants.STUDENT_ROLES)
@RequiredArgsConstructor
@Tag(name = "Student Role Management", description = "Endpoints for managing student role upgrades")
@SecurityRequirement(name = "bearerAuth")
public class StudentRoleController {

    private final StudentRoleService studentRoleService;

    // ==================== Get Student Roles ====================

    @GetMapping(ApiConstants.STUDENT_ROLES_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('STUDENT_ROLE:VIEW', 'USER:ADMIN_READ')")
    @Operation(
            summary = "Get student roles",
            description = "Retrieve all roles assigned to a student. Requires STUDENT_ROLE:VIEW or USER:ADMIN_READ permission."
    )
    public ApiResponse<StudentRoleResponse> getStudentRoles(
            @Parameter(description = "Student database ID") @PathVariable Long studentId) {
        StudentRoleResponse response = studentRoleService.getStudentRoles(studentId);
        return ApiResponse.success("Student roles retrieved successfully", response);
    }

    @GetMapping("/by-student-id/{studentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('STUDENT_ROLE:VIEW', 'USER:ADMIN_READ')")
    @Operation(
            summary = "Get student roles by university ID",
            description = "Retrieve all roles assigned to a student using their university ID (e.g., IIT12345)."
    )
    public ApiResponse<StudentRoleResponse> getStudentRolesByStudentId(
            @Parameter(description = "Student university ID (e.g., IIT12345)") @PathVariable String studentId) {
        StudentRoleResponse response = studentRoleService.getStudentRolesByStudentId(studentId);
        return ApiResponse.success("Student roles retrieved successfully", response);
    }

    // ==================== Add Roles ====================

    @PostMapping(ApiConstants.STUDENT_ADD_ROLE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Add role to student",
            description = "Add a new role to a student. Admin/Super Admin only. " +
                    "Roles are cumulative - existing roles are preserved."
    )
    public ApiResponse<StudentRoleResponse> addRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Valid @RequestBody AddStudentRoleRequest request) {
        StudentRoleResponse response = studentRoleService.addRole(studentId, request);
        return ApiResponse.success("Role added successfully", response);
    }

    @PostMapping(ApiConstants.STUDENT_ADD_CLUB_MEMBER)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    @Operation(
            summary = "Add CLUB_MEMBER role",
            description = "Add CLUB_MEMBER role to a student. Called when club membership is approved. " +
                    "Can be triggered by Admin/Super Admin or Club Admin with CLUB_MEMBERSHIP:MANAGE permission."
    )
    public ApiResponse<StudentRoleResponse> addClubMemberRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Valid @RequestBody AddStudentRoleRequest request) {
        StudentRoleResponse response = studentRoleService.addClubMemberRole(studentId, request);
        return ApiResponse.success("Club member role added successfully", response);
    }

    @PostMapping(ApiConstants.STUDENT_ADD_BATCH_REP)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Assign BATCH_REP role",
            description = "Assign Batch Representative role to a student. Admin/Super Admin only. " +
                    "Student must be appointed by authorized staff."
    )
    public ApiResponse<StudentRoleResponse> addBatchRepRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Valid @RequestBody AddStudentRoleRequest request) {
        StudentRoleResponse response = studentRoleService.addBatchRepRole(studentId, request);
        return ApiResponse.success("Batch representative role assigned successfully", response);
    }

    @PostMapping(ApiConstants.STUDENT_ADD_KUPPI_STUDENT)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('STUDENT_ROLE:KUPPI_APPROVE')")
    @Operation(
            summary = "Approve KUPPI_STUDENT role",
            description = "Approve Kuppi Student role for a student. Called after admin approves a kuppi session request. " +
                    "Allows student to create and host Kuppi sessions."
    )
    public ApiResponse<StudentRoleResponse> addKuppiStudentRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Valid @RequestBody AddStudentRoleRequest request) {
        StudentRoleResponse response = studentRoleService.addKuppiStudentRole(studentId, request);
        return ApiResponse.success("Kuppi student role approved successfully", response);
    }

    // ==================== Remove Roles ====================

    @DeleteMapping(ApiConstants.STUDENT_REMOVE_ROLE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "Remove role from student",
            description = "Remove a role from a student. Admin/Super Admin only. " +
                    "Note: The NORMAL base role cannot be removed."
    )
    public ApiResponse<StudentRoleResponse> removeRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Valid @RequestBody RemoveStudentRoleRequest request) {
        StudentRoleResponse response = studentRoleService.removeRole(studentId, request);
        return ApiResponse.success("Role removed successfully", response);
    }

    // ==================== Query Students by Role ====================

    @GetMapping(ApiConstants.STUDENTS_BY_ROLE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('STUDENT_ROLE:VIEW', 'USER:ADMIN_READ')")
    @Operation(
            summary = "Get students by role type",
            description = "Retrieve all students who have a specific role type."
    )
    public ApiResponse<List<StudentRoleResponse>> getStudentsByRole(
            @Parameter(description = "Role type to filter by") @PathVariable StudentRoleType roleType) {
        List<StudentRoleResponse> students = studentRoleService.getStudentsByRole(roleType);
        return ApiResponse.success("Students retrieved successfully", students);
    }

    // ==================== Check Role ====================

    @GetMapping("/{studentId}/roles/has/{roleType}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('STUDENT_ROLE:VIEW', 'USER:ADMIN_READ', 'USER:READ')")
    @Operation(
            summary = "Check if student has role",
            description = "Check if a student has a specific role type."
    )
    public ApiResponse<Boolean> hasRole(
            @Parameter(description = "Student database ID") @PathVariable Long studentId,
            @Parameter(description = "Role type to check") @PathVariable StudentRoleType roleType) {
        boolean hasRole = studentRoleService.hasRole(studentId, roleType);
        return ApiResponse.success("Role check completed", hasRole);
    }

    // ==================== Migration Endpoint ====================

    @PostMapping("/migrate-deprecated-roles")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Migrate deprecated roles",
            description = "Migrate deprecated SENIOR_KUPPI roles to KUPPI_STUDENT. Super Admin only. " +
                    "This is a one-time migration operation for data consistency."
    )
    public ApiResponse<Integer> migrateDeprecatedRoles() {
        int migratedCount = studentRoleService.migrateDeprecatedRoles();
        return ApiResponse.success("Migration completed successfully. " + migratedCount + " students migrated.", migratedCount);
    }
}
