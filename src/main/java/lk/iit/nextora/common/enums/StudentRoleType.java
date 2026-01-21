package lk.iit.nextora.common.enums;

import lombok.Getter;
import org.springframework.security.access.method.P;

import java.util.Set;

/**
 * Sub-roles for students within the ROLE_STUDENT main role.
 * A student can have one of these sub-roles which determines
 * their additional permissions and capabilities.
 */
@Getter
public enum StudentRoleType {
    // ==================== NORMAL STUDENT ====================
    // Basic student with standard access
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
    // Student who is a member of a club/society - can post announcements, manage club activities
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
                    Permission.ELECTION_CREATE,
                    Permission.ELECTION_READ,
                    Permission.ELECTION_UPDATE,
                    Permission.ELECTION_DELETE,
                    Permission.ELECTION_MANAGE,
                    Permission.ELECTION_PUBLISH_RESULTS,
                    Permission.CANDIDATE_NOMINATE,
                    Permission.CANDIDATE_APPROVE,
                    Permission.CANDIDATE_VIEW,
                    Permission.VOTE_CAST,
                    Permission.VOTE_VIEW_RESULTS,
                    Permission.VOTE_VIEW_STATISTICS,
                    Permission.CLUB_MEMBERSHIP_MANAGE,
                    Permission.CLUB_MEMBERSHIP_VIEW,
                    Permission.CLUB_UPDATE
            )),

    // ==================== SENIOR KUPPI ====================
    // Senior student who can conduct kuppi (tutoring) sessions for juniors
    SENIOR_KUPPI("Senior Kuppi Mentor", "Senior student who can conduct kuppi sessions",
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

    // ==================== BATCH REPRESENTATIVE ====================
    // Student representative for their batch - can coordinate, announce, and represent
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
}
