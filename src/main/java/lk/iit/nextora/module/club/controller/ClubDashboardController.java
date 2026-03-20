package lk.iit.nextora.module.club.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.module.club.dto.response.dashboard.*;
import lk.iit.nextora.module.club.service.ClubDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for role-based Club Dashboard endpoints.
 * Each endpoint returns a pre-aggregated dashboard tailored to the caller's role,
 * enabling the frontend to render a complete dashboard with a single API call.
 *
 * Endpoints:
 * - GET /student       → Student (browse clubs, memberships)
 * - GET /club-member   → Club Member (club detail, elections, announcements)
 * - GET /staff         → Non-Academic Staff (platform-wide management)
 * - GET /admin         → Admin / Super Admin (full analytics)
 * - GET /academic      → Academic Staff (read-only overview)
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.CLUB_DASHBOARD)
@RequiredArgsConstructor
@Tag(name = "Club Dashboard", description = "Role-based club dashboard endpoints for frontend integration")
public class ClubDashboardController {

    private final ClubDashboardService dashboardService;

    // ==================== Student Dashboard ====================

    @GetMapping(ApiConstants.CLUB_DASHBOARD_STUDENT)
    @Operation(summary = "Student dashboard",
               description = "Get club overview for students: browseable clubs, memberships, public announcements")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<StudentClubDashboardResponse> getStudentDashboard() {
        log.info("Student requesting club dashboard");
        StudentClubDashboardResponse response = dashboardService.getStudentDashboard();
        return ApiResponse.success("Student dashboard loaded successfully", response);
    }

    // ==================== Club Member Dashboard ====================

    @GetMapping(ApiConstants.CLUB_DASHBOARD_CLUB_MEMBER)
    @Operation(summary = "Club member dashboard",
               description = "Get club-specific dashboard: announcements, elections, members, activity log")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<ClubMemberDashboardResponse> getClubMemberDashboard(
            @RequestParam Long clubId) {
        log.info("Club member requesting dashboard for club: {}", clubId);
        ClubMemberDashboardResponse response = dashboardService.getClubMemberDashboard(clubId);
        return ApiResponse.success("Club member dashboard loaded successfully", response);
    }

    // ==================== Non-Academic Staff Dashboard ====================

    @GetMapping(ApiConstants.CLUB_DASHBOARD_STAFF)
    @Operation(summary = "Staff dashboard",
               description = "Platform-wide club management overview for non-academic staff")
    @PreAuthorize("hasAuthority('CLUB:VIEW_STATS')")
    public ApiResponse<StaffClubDashboardResponse> getStaffDashboard() {
        log.info("Staff requesting club dashboard");
        StaffClubDashboardResponse response = dashboardService.getStaffDashboard();
        return ApiResponse.success("Staff dashboard loaded successfully", response);
    }

    // ==================== Admin Dashboard ====================

    @GetMapping(ApiConstants.CLUB_DASHBOARD_ADMIN)
    @Operation(summary = "Admin dashboard",
               description = "Full platform analytics: clubs, elections, members, activity")
    @PreAuthorize("hasAuthority('CLUB:VIEW_STATS')")
    public ApiResponse<AdminClubDashboardResponse> getAdminDashboard() {
        log.info("Admin requesting club dashboard");
        AdminClubDashboardResponse response = dashboardService.getAdminDashboard();
        return ApiResponse.success("Admin dashboard loaded successfully", response);
    }

    // ==================== Academic Staff Dashboard ====================

    @GetMapping(ApiConstants.CLUB_DASHBOARD_ACADEMIC)
    @Operation(summary = "Academic staff dashboard",
               description = "Read-only clubs overview plus clubs they advise")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<AcademicStaffClubDashboardResponse> getAcademicStaffDashboard() {
        log.info("Academic staff requesting club dashboard");
        AcademicStaffClubDashboardResponse response = dashboardService.getAcademicStaffDashboard();
        return ApiResponse.success("Academic staff dashboard loaded successfully", response);
    }
}

