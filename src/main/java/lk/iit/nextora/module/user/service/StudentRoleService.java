package lk.iit.nextora.module.user.service;

import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.module.user.dto.request.AddStudentRoleRequest;
import lk.iit.nextora.module.user.dto.request.RemoveStudentRoleRequest;
import lk.iit.nextora.module.user.dto.response.StudentRoleResponse;

import java.util.List;

/**
 * Service interface for student role management.
 *
 * Student Role Structure (Cumulative):
 * - Base Role: NORMAL → Default role for all students
 * - Additional Capabilities (Added Gradually):
 *   - CLUB_MEMBER → When student joins a club
 *   - BATCH_REP → When appointed as Batch Representative
 *   - KUPPI_STUDENT → When approved to participate/host Kuppi sessions
 *
 * Role Upgrade Rules:
 * - Roles are never removed automatically
 * - New roles are added on top of existing roles
 * - Admin approval is required for: BATCH_REP, KUPPI_STUDENT
 * - Student cannot assign roles to themselves
 */
public interface StudentRoleService {

    /**
     * Get all roles for a student by student ID (database ID)
     */
    StudentRoleResponse getStudentRoles(Long studentId);

    /**
     * Get all roles for a student by student ID (university ID like "IIT12345")
     */
    StudentRoleResponse getStudentRolesByStudentId(String studentId);

    /**
     * Add a role to a student (Admin/Super Admin only)
     *
     * @param studentId Database ID of the student
     * @param request Role addition request with role type and optional role-specific data
     * @return Updated student role information
     */
    StudentRoleResponse addRole(Long studentId, AddStudentRoleRequest request);

    /**
     * Remove a role from a student (Admin/Super Admin only)
     * Note: NORMAL role cannot be removed
     *
     * @param studentId Database ID of the student
     * @param request Role removal request with role type and reason
     * @return Updated student role information
     */
    StudentRoleResponse removeRole(Long studentId, RemoveStudentRoleRequest request);

    /**
     * Add CLUB_MEMBER role to a student
     * Called when a student's club membership is approved
     *
     * @param studentId Database ID of the student
     * @param request Role addition request with club-specific data
     * @return Updated student role information
     */
    StudentRoleResponse addClubMemberRole(Long studentId, AddStudentRoleRequest request);

    /**
     * Add BATCH_REP role to a student (Admin/Super Admin only)
     *
     * @param studentId Database ID of the student
     * @param request Role addition request with batch rep-specific data
     * @return Updated student role information
     */
    StudentRoleResponse addBatchRepRole(Long studentId, AddStudentRoleRequest request);

    /**
     * Add KUPPI_STUDENT role to a student (Admin/Super Admin only)
     * Called after admin approves a kuppi session request
     *
     * @param studentId Database ID of the student
     * @param request Role addition request with kuppi-specific data
     * @return Updated student role information
     */
    StudentRoleResponse addKuppiStudentRole(Long studentId, AddStudentRoleRequest request);

    /**
     * Get all students with a specific role type
     *
     * @param roleType The role type to filter by
     * @return List of students with the specified role
     */
    List<StudentRoleResponse> getStudentsByRole(StudentRoleType roleType);

    /**
     * Check if a student has a specific role
     *
     * @param studentId Database ID of the student
     * @param roleType The role type to check
     * @return true if student has the role, false otherwise
     */
    boolean hasRole(Long studentId, StudentRoleType roleType);

    /**
     * Migrate deprecated SENIOR_KUPPI roles to KUPPI_STUDENT
     * This is a batch operation for data migration
     *
     * @return Number of students migrated
     */
    int migrateDeprecatedRoles();
}
