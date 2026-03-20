package lk.iit.nextora.module.club.service;

import lk.iit.nextora.module.club.dto.response.dashboard.*;

/**
 * Service interface for role-based club dashboard data aggregation.
 * Each method returns a pre-aggregated dashboard response tailored to a specific user role,
 * enabling the frontend to render a complete dashboard with a single API call.
 */
public interface ClubDashboardService {

    /**
     * Dashboard for normal students (ROLE_STUDENT).
     * Shows browseable clubs, open registrations, memberships, public announcements.
     */
    StudentClubDashboardResponse getStudentDashboard();

    /**
     * Dashboard for a club member within a specific club.
     * Shows club details, position, announcements, elections, activity log.
     */
    ClubMemberDashboardResponse getClubMemberDashboard(Long clubId);

    /**
     * Dashboard for Non-Academic Staff (ROLE_NON_ACADEMIC_STAFF).
     * Platform-wide club management overview.
     */
    StaffClubDashboardResponse getStaffDashboard();

    /**
     * Dashboard for Admin / Super Admin (ROLE_ADMIN, ROLE_SUPER_ADMIN).
     * Platform-wide stats with election analytics and growth metrics.
     */
    AdminClubDashboardResponse getAdminDashboard();

    /**
     * Dashboard for Academic Staff (ROLE_ACADEMIC_STAFF).
     * Read-only clubs overview plus clubs they advise.
     */
    AcademicStaffClubDashboardResponse getAcademicStaffDashboard();
}

