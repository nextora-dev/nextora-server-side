package lk.iit.nextora.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum UserRole {
    // ==================== SUPER_ADMIN - Complete system access ====================
    ROLE_SUPER_ADMIN("Super_Admin", "Unrestricted access to all system functions", true,
            getAllPermissions()),

    // ==================== ADMIN - Administrative access ====================
    ROLE_ADMIN("Admin", "Admin access to all system functions", true,
            getAdminPermissions()),

    // ==================== STUDENT - Student access (base permissions, sub-roles add more) ====================
    ROLE_STUDENT("Student", "Student access to system functions", true,
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
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_READ,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ,
                    Permission.LOST_FOUND_UPDATE,
                    Permission.LOST_FOUND_CLAIM,
                    // Voting permissions for students
                    Permission.ELECTION_READ,
                    Permission.CANDIDATE_VIEW,
                    Permission.CANDIDATE_NOMINATE,
                    Permission.VOTE_CAST,
                    Permission.VOTE_VIEW_RESULTS,
                    Permission.CLUB_MEMBERSHIP_VIEW,
                    Permission.USER_RESET_PASSWORD,
                    Permission.MEETING_CREATE,
                    Permission.MEETING_READ,
                    Permission.MEETING_CANCEL,
                    // Boarding House - students can browse listings
                    Permission.BOARDING_HOUSE_READ
            )),

    // ==================== NON_ACADEMIC_STAFF ====================
    ROLE_NON_ACADEMIC_STAFF("Non_Academic_Staff", "Non-academic staff access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.CLUB_READ,
                    Permission.CLUB_CREATE,
                    Permission.CLUB_UPDATE,
                    Permission.CLUB_DELETE,
                    Permission.CLUB_MEMBERSHIP_MANAGE,

                    Permission.ELECTION_CREATE,
                    Permission.ELECTION_UPDATE,
                    Permission.ELECTION_DELETE,
                    Permission.ELECTION_READ,
                    Permission.ELECTION_MANAGE,
                    Permission.VOTE_VIEW_STATISTICS,
                    Permission.ELECTION_PUBLISH_RESULTS,
                    Permission.CANDIDATE_APPROVE,
                    Permission.CANDIDATE_VIEW,

                    // Voting statistics permissions
                    Permission.VOTE_VIEW_RESULTS,
                    Permission.USER_RESET_PASSWORD,
                    Permission.USER_UPDATE,

                    // Lost & Found admin permissions
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ,
                    Permission.LOST_FOUND_UPDATE,
                    Permission.LOST_FOUND_DELETE,
                    Permission.LOST_FOUND_CLAIM,
                    Permission.LOST_FOUND_ADMIN_VIEW,
                    Permission.LOST_FOUND_ADMIN_UPDATE,
                    // Boarding House - non-academic staff can manage listings
                    Permission.BOARDING_HOUSE_CREATE,
                    Permission.BOARDING_HOUSE_READ,
                    Permission.BOARDING_HOUSE_UPDATE,
                    Permission.BOARDING_HOUSE_DELETE,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.EVENT_DELETE,
                    Permission.EVENT_VIEW_ANALYTICS
            )),

    // ==================== ACADEMIC_STAFF ====================
    ROLE_ACADEMIC_STAFF("Academic_Staff", "Academic staff access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_APPROVE,
                    Permission.KUPPI_NOTE_READ,
                    Permission.KUPPI_NOTE_DOWNLOAD,
                    Permission.KUPPI_NOTE_SEARCH,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.EVENT_DELETE,
                    Permission.EVENT_VIEW_ANALYTICS,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_READ,
                    Permission.USER_RESET_PASSWORD,
                    Permission.MEETING_MANAGE,
                    Permission.MEETING_VIEW_CALENDAR,
                    Permission.MEETING_ADD_NOTES,
                    Permission.MEETING_AVAILABILITY,
                    Permission.MEETING_READ
            ));

    private final String displayName;
    private final String description;
    private final boolean systemRole;
    private final Set<Permission> permissions;

    UserRole(String displayName, String description, boolean systemRole, Set<Permission> permissions) {
        this.displayName = displayName;
        this.description = description;
        this.systemRole = systemRole;
        this.permissions = permissions;
    }

    // ==================== HELPER METHODS FOR PERMISSION SETS ====================

    private static Set<Permission> getAllPermissions() {
        return new HashSet<>(Arrays.asList(Permission.values()));
    }

    private static Set<Permission> getAdminPermissions() {
        Set<Permission> adminPermissions = new HashSet<>(Arrays.asList(Permission.values()));
        // Remove super admin exclusive permissions
        adminPermissions.remove(Permission.ELECTION_SUPER_ADMIN);
        adminPermissions.remove(Permission.ELECTION_PERMANENT_DELETE);
        adminPermissions.remove(Permission.USER_ADMIN_DELETE);
        adminPermissions.remove(Permission.USER_PERMANENT_DELETE);
        adminPermissions.remove(Permission.KUPPI_PERMANENT_DELETE);
        adminPermissions.remove(Permission.KUPPI_NOTE_PERMANENT_DELETE);
        // Boarding House permanent delete is Super Admin only
        adminPermissions.remove(Permission.BOARDING_HOUSE_PERMANENT_DELETE);
        return adminPermissions;
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
