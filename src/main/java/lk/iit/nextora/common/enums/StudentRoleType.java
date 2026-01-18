package lk.iit.nextora.common.enums;

import lombok.Getter;

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
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_READ,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ
            )),

    // ==================== CLUB MEMBER ====================
    // Student who is a member of a club/society - can post announcements, manage club activities
    CLUB_MEMBER("Club Member", "Student who is a member of a club/society",
            Set.of(
                    Permission.USER_READ,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.CLUB_READ,
                    Permission.CLUB_UPDATE,
                    Permission.CLUB_POST,
                    Permission.CLUB_MANAGE_MEMBERS,
                    Permission.BATCH_READ,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ
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
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_READ,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ
            )),

    // ==================== BATCH REPRESENTATIVE ====================
    // Student representative for their batch - can coordinate, announce, and represent
    BATCH_REP("Batch Representative", "Student representative for their batch",
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.BATCH_UPDATE,
                    Permission.BATCH_ANNOUNCE,
                    Permission.BATCH_COORDINATE,
                    Permission.BATCH_REPRESENT,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ,
                    Permission.LOST_FOUND_UPDATE
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
