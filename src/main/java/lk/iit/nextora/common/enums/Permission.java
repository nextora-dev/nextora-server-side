package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    // ==================== USER MANAGEMENT ====================
    USER_CREATE("USER:CREATE", "Create new users", "USER", "CREATE", PermissionCategory.USER_MANAGEMENT),
    USER_READ("USER:READ", "View user profiles", "USER", "READ", PermissionCategory.USER_MANAGEMENT),
    USER_UPDATE("USER:UPDATE", "Modify user information", "USER", "UPDATE", PermissionCategory.USER_MANAGEMENT),
    USER_DELETE("USER:DELETE", "Delete user accounts", "USER", "DELETE", PermissionCategory.USER_MANAGEMENT),
    USER_ACTIVATE("USER:ACTIVATE", "Activate/deactivate users", "USER", "ACTIVATE", PermissionCategory.USER_MANAGEMENT),
    USER_RESET_PASSWORD("USER:RESET_PASSWORD", "Reset user passwords", "USER", "RESET_PASSWORD", PermissionCategory.USER_MANAGEMENT),
    USER_ADMIN_CREATE("USER:ADMIN_CREATE", "Create admin users", "USER", "ADMIN_CREATE", PermissionCategory.USER_MANAGEMENT),
    USER_ADMIN_READ("USER:ADMIN_READ", "Admin view all user profiles", "USER", "ADMIN_READ", PermissionCategory.USER_MANAGEMENT),
    USER_ADMIN_UPDATE("USER:ADMIN_UPDATE", "Admin update any user", "USER", "ADMIN_UPDATE", PermissionCategory.USER_MANAGEMENT),
    USER_ADMIN_DELETE("USER:ADMIN_DELETE", "Admin delete any user", "USER", "ADMIN_DELETE", PermissionCategory.USER_MANAGEMENT),
    USER_RESTORE("USER:RESTORE", "Restore deleted users", "USER", "RESTORE", PermissionCategory.USER_MANAGEMENT),
    USER_UNLOCK("USER:UNLOCK", "Unlock suspended user accounts", "USER", "UNLOCK", PermissionCategory.USER_MANAGEMENT),

    // ==================== KUPPI SESSIONS ====================
    KUPPI_CREATE("KUPPI:CREATE", "Create kuppi sessions", "KUPPI", "CREATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_READ("KUPPI:READ", "View kuppi sessions", "KUPPI", "READ", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_UPDATE("KUPPI:UPDATE", "Update kuppi sessions", "KUPPI", "UPDATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_DELETE("KUPPI:DELETE", "Delete kuppi sessions", "KUPPI", "DELETE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_HOST("KUPPI:HOST", "Host kuppi sessions", "KUPPI", "HOST", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_JOIN("KUPPI:JOIN", "Join kuppi sessions", "KUPPI", "JOIN", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPROVE("KUPPI:APPROVE", "Approve kuppi sessions", "KUPPI", "APPROVE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_CANCEL("KUPPI:CANCEL", "Cancel kuppi sessions", "KUPPI", "CANCEL", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_RESCHEDULE("KUPPI:RESCHEDULE", "Reschedule kuppi sessions", "KUPPI", "RESCHEDULE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_LEAVE("KUPPI:LEAVE", "Leave kuppi sessions", "KUPPI", "LEAVE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_FEEDBACK("KUPPI:FEEDBACK", "Submit feedback for kuppi sessions", "KUPPI", "FEEDBACK", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_VIEW_PARTICIPANTS("KUPPI:VIEW_PARTICIPANTS", "View kuppi session participants", "KUPPI", "VIEW_PARTICIPANTS", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_VIEW_ANALYTICS("KUPPI:VIEW_ANALYTICS", "View kuppi analytics", "KUPPI", "VIEW_ANALYTICS", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_VIEW_STATS("KUPPI:VIEW_STATS", "View kuppi platform statistics", "KUPPI", "VIEW_STATS", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_ADMIN_UPDATE("KUPPI:ADMIN_UPDATE", "Admin update any kuppi session", "KUPPI", "ADMIN_UPDATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_ADMIN_DELETE("KUPPI:ADMIN_DELETE", "Admin delete any kuppi session", "KUPPI", "ADMIN_DELETE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_PERMANENT_DELETE("KUPPI:PERMANENT_DELETE", "Permanently delete kuppi sessions", "KUPPI", "PERMANENT_DELETE", PermissionCategory.KUPPI_MANAGEMENT),

    // ==================== KUPPI NOTES ====================
    KUPPI_NOTE_CREATE("KUPPI_NOTE:CREATE", "Create kuppi notes", "KUPPI_NOTE", "CREATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_READ("KUPPI_NOTE:READ", "View kuppi notes", "KUPPI_NOTE", "READ", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_UPDATE("KUPPI_NOTE:UPDATE", "Update kuppi notes", "KUPPI_NOTE", "UPDATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_DELETE("KUPPI_NOTE:DELETE", "Delete kuppi notes", "KUPPI_NOTE", "DELETE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_DOWNLOAD("KUPPI_NOTE:DOWNLOAD", "Download kuppi notes", "KUPPI_NOTE", "DOWNLOAD", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_SEARCH("KUPPI_NOTE:SEARCH", "Search kuppi notes", "KUPPI_NOTE", "SEARCH", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_ADMIN_UPDATE("KUPPI_NOTE:ADMIN_UPDATE", "Admin update any kuppi note", "KUPPI_NOTE", "ADMIN_UPDATE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_ADMIN_DELETE("KUPPI_NOTE:ADMIN_DELETE", "Admin delete any kuppi note", "KUPPI_NOTE", "ADMIN_DELETE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_NOTE_PERMANENT_DELETE("KUPPI_NOTE:PERMANENT_DELETE", "Permanently delete kuppi notes", "KUPPI_NOTE", "PERMANENT_DELETE", PermissionCategory.KUPPI_MANAGEMENT),

    // ==================== CLUB MANAGEMENT ====================
    CLUB_CREATE("CLUB:CREATE", "Create clubs", "CLUB", "CREATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_READ("CLUB:READ", "View clubs", "CLUB", "READ", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_UPDATE("CLUB:UPDATE", "Update club information", "CLUB", "UPDATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_DELETE("CLUB:DELETE", "Delete clubs", "CLUB", "DELETE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_MANAGE_MEMBERS("CLUB:MANAGE_MEMBERS", "Manage club members", "CLUB", "MANAGE_MEMBERS", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_POST("CLUB:POST", "Post club announcements", "CLUB", "POST", PermissionCategory.CLUB_MANAGEMENT),

    // ==================== BATCH MANAGEMENT ====================
    BATCH_READ("BATCH:READ", "View batch information", "BATCH", "READ", PermissionCategory.BATCH_MANAGEMENT),
    BATCH_UPDATE("BATCH:UPDATE", "Update batch information", "BATCH", "UPDATE", PermissionCategory.BATCH_MANAGEMENT),
    BATCH_ANNOUNCE("BATCH:ANNOUNCE", "Make batch announcements", "BATCH", "ANNOUNCE", PermissionCategory.BATCH_MANAGEMENT),
    BATCH_COORDINATE("BATCH:COORDINATE", "Coordinate batch activities", "BATCH", "COORDINATE", PermissionCategory.BATCH_MANAGEMENT),
    BATCH_REPRESENT("BATCH:REPRESENT", "Represent batch to faculty", "BATCH", "REPRESENT", PermissionCategory.BATCH_MANAGEMENT),

    // ==================== EVENTS ====================
    EVENT_CREATE("EVENT:CREATE", "Create events", "EVENT", "CREATE", PermissionCategory.EVENT_MANAGEMENT),
    EVENT_READ("EVENT:READ", "View events", "EVENT", "READ", PermissionCategory.EVENT_MANAGEMENT),
    EVENT_UPDATE("EVENT:UPDATE", "Update events", "EVENT", "UPDATE", PermissionCategory.EVENT_MANAGEMENT),
    EVENT_DELETE("EVENT:DELETE", "Delete events", "EVENT", "DELETE", PermissionCategory.EVENT_MANAGEMENT),
    EVENT_REGISTER("EVENT:REGISTER", "Register for events", "EVENT", "REGISTER", PermissionCategory.EVENT_MANAGEMENT),

    // ==================== COMMUNICATION ====================
    COMMUNICATION_SEND("COMM:SEND", "Send messages", "COMM", "SEND", PermissionCategory.COMMUNICATION),
    COMMUNICATION_READ("COMM:READ", "Read messages", "COMM", "READ", PermissionCategory.COMMUNICATION),
    COMMUNICATION_BROADCAST("COMM:BROADCAST", "Broadcast messages", "COMM", "BROADCAST", PermissionCategory.COMMUNICATION),

    // ==================== LOST AND FOUND ====================
    LOST_FOUND_CREATE("LOST_FOUND:CREATE", "Create lost/found items", "LOST_FOUND", "CREATE", PermissionCategory.LOST_AND_FOUND),
    LOST_FOUND_READ("LOST_FOUND:READ", "View lost/found items", "LOST_FOUND", "READ", PermissionCategory.LOST_AND_FOUND),
    LOST_FOUND_UPDATE("LOST_FOUND:UPDATE", "Update lost/found items", "LOST_FOUND", "UPDATE", PermissionCategory.LOST_AND_FOUND),
    LOST_FOUND_DELETE("LOST_FOUND:DELETE", "Delete lost/found items", "LOST_FOUND", "DELETE", PermissionCategory.LOST_AND_FOUND);

    private final String permission;
    private final String description;
    private final String resource;
    private final String action;
    private final PermissionCategory category;

    /**
     * Permission Categories for grouping
     */
    @Getter
    public enum PermissionCategory {
        USER_MANAGEMENT("User Management"),
        KUPPI_MANAGEMENT("Kuppi Management"),
        CLUB_MANAGEMENT("Club Management"),
        BATCH_MANAGEMENT("Batch Management"),
        EVENT_MANAGEMENT("Event Management"),
        COMMUNICATION("Communication"),
        LOST_AND_FOUND("Lost and Found");

        private final String displayName;

        PermissionCategory(String displayName) {
            this.displayName = displayName;
        }

    }
}
