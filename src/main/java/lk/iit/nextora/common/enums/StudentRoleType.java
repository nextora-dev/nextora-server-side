package lk.iit.nextora.common.enums;

import lombok.Getter;

import java.util.Set;

/**
 * Sub-roles for students within the ROLE_STUDENT main role.
 * A student can have multiple sub-roles which are additive (cumulative).
 *
 * Student Role Structure (Cumulative):
 * - Base Role: NORMAL → Default role for all students
 * - Additional Capabilities (Added Gradually):
 *   - CLUB_MEMBER → When student joins a club
 *   - BATCH_REP → When appointed as Batch Representative
 *   - KUPPI_STUDENT → When approved to participate/host Kuppi sessions
 *
 * Roles are NEVER removed automatically - they are additive.
 * Admin approval is required for: BATCH_REP, KUPPI_STUDENT
 */
@Getter
public enum StudentRoleType {
    // ==================== NORMAL STUDENT (Base Role) ====================
    // Basic student with standard access - every student has this
    NORMAL("Normal Student", "Regular student with basic access",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.KUPPI_LEAVE,
                    Permission.KUPPI_FEEDBACK,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH
            )),

    // ==================== CLUB MEMBER ====================
    // Student who is a member of a club/society - can participate in club activities
    // Granted when: Student joins a club (self-request or club admin approval)
    CLUB_MEMBER("Club Member", "Student who is a member of a club/society",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.KUPPI_LEAVE,
                    Permission.KUPPI_FEEDBACK,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH,

                    // Club election management permissions
                    Permission.CANDIDATE_NOMINATE,
                    Permission.CANDIDATE_VIEW,
                    Permission.VOTE_CAST,
                    Permission.VOTE_VIEW_RESULTS,
                    Permission.CLUB_MEMBERSHIP_MANAGE,
                    Permission.CLUB_MEMBERSHIP_VIEW,
                    Permission.CLUB_UPDATE,

                    Permission.ELECTION_READ
            )),

    // ==================== BATCH REPRESENTATIVE ====================
    // Student representative for their batch - can coordinate, announce, and represent
    // Granted when: Admin or authorized staff assigns student as Batch Rep
    BATCH_REP("Batch Representative", "Student representative for their batch",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.KUPPI_LEAVE,
                    Permission.KUPPI_FEEDBACK,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH,
                    // Batch-specific permissions
                    Permission.BATCH_READ,
                    Permission.BATCH_UPDATE,
                    Permission.BATCH_ANNOUNCE,
                    Permission.BATCH_COORDINATE,
                    Permission.BATCH_REPRESENT
            )),

    // ==================== KUPPI STUDENT ====================
    // Student who can request/host Kuppi sessions for peer learning
    // Granted when: Student requests Kuppi session and admin approves
    KUPPI_STUDENT("Kuppi Student", "Student who can request and conduct kuppi sessions",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_CREATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_UPDATE,
                    Permission.KUPPI_DELETE,
                    Permission.KUPPI_HOST,
                    Permission.KUPPI_JOIN,
                    Permission.KUPPI_LEAVE,
                    Permission.KUPPI_CANCEL,
                    Permission.KUPPI_RESCHEDULE,
                    Permission.KUPPI_FEEDBACK,
                    Permission.KUPPI_VIEW_PARTICIPANTS,
                    Permission.KUPPI_VIEW_ANALYTICS,
                    Permission.KUPPI_NOTE_CREATE,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_UPDATE,
                    Permission.KUPPI_NOTE_DELETE,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH
            )),

    /**
     * @deprecated Use KUPPI_STUDENT instead. Kept for backward compatibility with existing data.
     */
    @Deprecated
    SENIOR_KUPPI("Senior Kuppi Mentor", "Senior student who can conduct kuppi sessions (deprecated, use KUPPI_STUDENT)",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_CREATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_UPDATE,
                    Permission.KUPPI_DELETE,
                    Permission.KUPPI_HOST,
                    Permission.KUPPI_JOIN,
                    Permission.KUPPI_LEAVE,
                    Permission.KUPPI_CANCEL,
                    Permission.KUPPI_RESCHEDULE,
                    Permission.KUPPI_FEEDBACK,
                    Permission.KUPPI_VIEW_PARTICIPANTS,
                    Permission.KUPPI_VIEW_ANALYTICS,
                    Permission.KUPPI_NOTE_CREATE,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_UPDATE,
                    Permission.KUPPI_NOTE_DELETE,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH
            ));

    private final String displayName;
    private final String description;
    private final Set<Permission> additionalPermissions;

    StudentRoleType(String displayName, String description, Set<Permission> additionalPermissions) {
        this.displayName = displayName;
        this.description = description;
        this.additionalPermissions = additionalPermissions;
    }

    /**
     * Get default student role type
     */
    public static StudentRoleType getDefault() {
        return NORMAL;
    }

    /**
     * Check if this student sub-role has a specific additional permission
     */
    public boolean hasPermission(Permission permission) {
        return additionalPermissions.contains(permission);
    }

    /**
     * Check if this role type requires admin approval
     * BATCH_REP and KUPPI_STUDENT require admin approval
     */
    public boolean requiresAdminApproval() {
        return this == BATCH_REP || this == KUPPI_STUDENT || this == SENIOR_KUPPI;
    }

    /**
     * Check if this role type can be self-assigned through activities
     * CLUB_MEMBER can be gained through club join request + approval
     */
    public boolean canBeSelfRequested() {
        return this == CLUB_MEMBER;
    }

    /**
     * Check if this is a base role that should never be removed
     */
    public boolean isBaseRole() {
        return this == NORMAL;
    }

    /**
     * Check if this role is deprecated and should be migrated
     */
    public boolean isDeprecated() {
        return this == SENIOR_KUPPI;
    }

    /**
     * Get the replacement role for deprecated roles
     */
    public StudentRoleType getReplacementRole() {
        if (this == SENIOR_KUPPI) {
            return KUPPI_STUDENT;
        }
        return this;
    }
}
