package lk.iit.nextora.module.intranet.service;

import lk.iit.nextora.module.intranet.dto.*;

import java.util.List;

/**
 * Unified service interface for all intranet content endpoints.
 * Provides read-only access to institutional content stored in the database.
 * <p>
 * Each content area follows a consistent pattern:
 * <ul>
 *   <li><b>getAll*()</b> — returns a summary list (lightweight projection)</li>
 *   <li><b>get*BySlug(slug)</b> — returns full detail for a single resource</li>
 * </ul>
 *
 * @author nextora-team
 * @since 1.0
 */
public interface IntranetContentService {

    // ── Schedules ───────────────────────────────────────────────────

    List<ScheduleCategoryResponse> getAllScheduleCategories();

    ScheduleCategoryResponse getScheduleCategoryBySlug(String slug);

    // ── Student Complaints ──────────────────────────────────────────────

    List<StudentComplaintCategoryResponse> getAllStudentComplaintCategories();

    StudentComplaintCategoryResponse getStudentComplaintBySlug(String slug);

    // ── Academic Calendars ──────────────────────────────────────────────

    List<AcademicCalendarResponse> getAllAcademicCalendars();

    AcademicCalendarResponse getAcademicCalendarBySlug(String slug);

    // ── Undergraduate Programs ──────────────────────────────────────────

    List<ProgramResponse> getAllUndergraduatePrograms();

    ProgramResponse getUndergraduateProgramBySlug(String slug);

    // ── Postgraduate Programs ───────────────────────────────────────────

    List<ProgramResponse> getAllPostgraduatePrograms();

    ProgramResponse getPostgraduateProgramBySlug(String slug);

    // ── Foundation Program ──────────────────────────────────────────────

    List<FoundationProgramResponse> getAllFoundationProgramCategories();

    FoundationProgramResponse getFoundationProgramBySlug(String slug);

    // ── Students Relations Unit ─────────────────────────────────────────

    StudentsRelationsUnitResponse getStudentsRelationsUnitInfo();

    StudentsRelationsUnitResponse getStudentsRelationsUnitBySlug(String slug);

    // ── Student Policies ────────────────────────────────────────────────

    List<StudentPolicyResponse> getAllStudentPolicies();

    StudentPolicyResponse getStudentPolicyBySlug(String slug);

    // ── Mitigation Forms ────────────────────────────────────────────────

    List<MitigationFormResponse> getAllMitigationForms();

    MitigationFormResponse getMitigationFormBySlug(String slug);

    // ── Staff ───────────────────────────────────────────────────────────

    List<StaffResponse> getAllStaffCategories();

    StaffResponse getStaffBySlug(String slug);

    // ── Info ────────────────────────────────────────────────────────────

    List<InfoResponse> getAllInfoCategories();

    InfoResponse getInfoBySlug(String slug);
}
