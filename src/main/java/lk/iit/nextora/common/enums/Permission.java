package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    // ==================== USER SELF MANAGEMENT (Any authenticated user for their own profile) ====================
    USER_READ("USER:READ", "View own user profile", "USER", "READ", PermissionCategory.USER_MANAGEMENT),
    USER_UPDATE("USER:UPDATE", "Update own user profile", "USER", "UPDATE", PermissionCategory.USER_MANAGEMENT),

    // ==================== NORMAL USER MANAGEMENT (Admin manages Student, Academic Staff, Non-Academic Staff) ====================
    NORMAL_USER_CREATE("NORMAL_USER:CREATE", "Create normal users (Student, Academic Staff, Non-Academic Staff)", "NORMAL_USER", "CREATE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_READ("NORMAL_USER:READ", "View normal user profiles", "NORMAL_USER", "READ", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_UPDATE("NORMAL_USER:UPDATE", "Update normal user information", "NORMAL_USER", "UPDATE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_DELETE("NORMAL_USER:DELETE", "Soft delete normal user accounts", "NORMAL_USER", "DELETE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_ACTIVATE("NORMAL_USER:ACTIVATE", "Activate normal users", "NORMAL_USER", "ACTIVATE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_DEACTIVATE("NORMAL_USER:DEACTIVATE", "Deactivate normal user accounts", "NORMAL_USER", "DEACTIVATE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_SUSPEND("NORMAL_USER:SUSPEND", "Suspend normal user accounts", "NORMAL_USER", "SUSPEND", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_UNLOCK("NORMAL_USER:UNLOCK", "Unlock suspended normal user accounts", "NORMAL_USER", "UNLOCK", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_RESET_PASSWORD("NORMAL_USER:RESET_PASSWORD", "Reset normal user passwords", "NORMAL_USER", "RESET_PASSWORD", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_RESTORE("NORMAL_USER:RESTORE", "Restore deleted normal users", "NORMAL_USER", "RESTORE", PermissionCategory.USER_MANAGEMENT),
    NORMAL_USER_PERMANENT_DELETE("NORMAL_USER:PERMANENT_DELETE", "Permanently delete normal users from database", "NORMAL_USER", "PERMANENT_DELETE", PermissionCategory.USER_MANAGEMENT),

    // ==================== ADMIN USER MANAGEMENT (Super Admin manages Admin users) ====================
    ADMIN_USER_CREATE("ADMIN_USER:CREATE", "Create admin users", "ADMIN_USER", "CREATE", PermissionCategory.USER_MANAGEMENT),
    ADMIN_USER_READ("ADMIN_USER:READ", "View admin user profiles", "ADMIN_USER", "READ", PermissionCategory.USER_MANAGEMENT),
    ADMIN_USER_UPDATE("ADMIN_USER:UPDATE", "Update admin user information", "ADMIN_USER", "UPDATE", PermissionCategory.USER_MANAGEMENT),
    ADMIN_USER_SOFT_DELETE("ADMIN_USER:SOFT_DELETE", "Soft delete admin users", "ADMIN_USER", "SOFT_DELETE", PermissionCategory.USER_MANAGEMENT),
    ADMIN_USER_PERMANENT_DELETE("ADMIN_USER:PERMANENT_DELETE", "Permanently delete admin users", "ADMIN_USER", "PERMANENT_DELETE", PermissionCategory.USER_MANAGEMENT),
    ADMIN_USER_RESTORE("ADMIN_USER:RESTORE", "Restore deleted admin users", "ADMIN_USER", "RESTORE", PermissionCategory.USER_MANAGEMENT),

    // ==================== STUDENT ROLE MANAGEMENT ====================
    STUDENT_ROLE_ADD("STUDENT_ROLE:ADD", "Add roles to students", "STUDENT_ROLE", "ADD", PermissionCategory.USER_MANAGEMENT),
    STUDENT_ROLE_REMOVE("STUDENT_ROLE:REMOVE", "Remove roles from students", "STUDENT_ROLE", "REMOVE", PermissionCategory.USER_MANAGEMENT),
    STUDENT_ROLE_VIEW("STUDENT_ROLE:VIEW", "View student roles", "STUDENT_ROLE", "VIEW", PermissionCategory.USER_MANAGEMENT),
    STUDENT_BATCH_REP_ASSIGN("STUDENT_ROLE:BATCH_REP_ASSIGN", "Assign batch representative role", "STUDENT_ROLE", "BATCH_REP_ASSIGN", PermissionCategory.USER_MANAGEMENT),
    STUDENT_KUPPI_APPROVE("STUDENT_ROLE:KUPPI_APPROVE", "Approve kuppi student role", "STUDENT_ROLE", "KUPPI_APPROVE", PermissionCategory.USER_MANAGEMENT),

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

    // ==================== KUPPI APPLICATION ====================
    KUPPI_APPLICATION_SUBMIT("KUPPI_APPLICATION:SUBMIT", "Submit kuppi student application", "KUPPI_APPLICATION", "SUBMIT", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_VIEW_OWN("KUPPI_APPLICATION:VIEW_OWN", "View own kuppi applications", "KUPPI_APPLICATION", "VIEW_OWN", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_CANCEL("KUPPI_APPLICATION:CANCEL", "Cancel own kuppi application", "KUPPI_APPLICATION", "CANCEL", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_VIEW_ALL("KUPPI_APPLICATION:VIEW_ALL", "View all kuppi applications", "KUPPI_APPLICATION", "VIEW_ALL", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_APPROVE("KUPPI_APPLICATION:APPROVE", "Approve kuppi applications", "KUPPI_APPLICATION", "APPROVE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_REJECT("KUPPI_APPLICATION:REJECT", "Reject kuppi applications", "KUPPI_APPLICATION", "REJECT", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_STATS("KUPPI_APPLICATION:STATS", "View kuppi application statistics", "KUPPI_APPLICATION", "STATS", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_REVOKE("KUPPI_APPLICATION:REVOKE", "Revoke kuppi student role", "KUPPI_APPLICATION", "REVOKE", PermissionCategory.KUPPI_MANAGEMENT),
    KUPPI_APPLICATION_PERMANENT_DELETE("KUPPI_APPLICATION:PERMANENT_DELETE", "Permanently delete kuppi applications", "KUPPI_APPLICATION", "PERMANENT_DELETE", PermissionCategory.KUPPI_MANAGEMENT),

    // ==================== CLUB MANAGEMENT ====================
    CLUB_CREATE("CLUB:CREATE", "Create clubs", "CLUB", "CREATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_READ("CLUB:READ", "View clubs", "CLUB", "READ", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_UPDATE("CLUB:UPDATE", "Update club information", "CLUB", "UPDATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_DELETE("CLUB:DELETE", "Delete clubs", "CLUB", "DELETE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_MANAGE_MEMBERS("CLUB:MANAGE_MEMBERS", "Manage club members", "CLUB", "MANAGE_MEMBERS", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_POST("CLUB:POST", "Post club announcements", "CLUB", "POST", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_VIEW_STATS("CLUB:VIEW_STATS", "View club statistics", "CLUB", "VIEW_STATS", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_VIEW_ACTIVITY_LOG("CLUB:VIEW_ACTIVITY_LOG", "View club activity logs", "CLUB", "VIEW_ACTIVITY_LOG", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_MEMBERSHIP_MANAGE("CLUB_MEMBERSHIP:MANAGE", "Manage club memberships", "CLUB_MEMBERSHIP", "MANAGE", PermissionCategory.VOTING_MANAGEMENT),
    CLUB_MEMBERSHIP_VIEW("CLUB_MEMBERSHIP:VIEW", "View club memberships", "CLUB_MEMBERSHIP", "VIEW", PermissionCategory.VOTING_MANAGEMENT),


    CLUB_PERMANENT_DELETE("CLUB:PERMANENT_DELETE", "Permanently delete clubs from database", "CLUB", "PERMANENT_DELETE", PermissionCategory.CLUB_MANAGEMENT),

    // ==================== CLUB ANNOUNCEMENTS ====================
    CLUB_ANNOUNCEMENT_CREATE("CLUB_ANNOUNCEMENT:CREATE", "Create club announcements", "CLUB_ANNOUNCEMENT", "CREATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_ANNOUNCEMENT_READ("CLUB_ANNOUNCEMENT:READ", "View club announcements", "CLUB_ANNOUNCEMENT", "READ", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_ANNOUNCEMENT_UPDATE("CLUB_ANNOUNCEMENT:UPDATE", "Update club announcements", "CLUB_ANNOUNCEMENT", "UPDATE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_ANNOUNCEMENT_DELETE("CLUB_ANNOUNCEMENT:DELETE", "Delete club announcements", "CLUB_ANNOUNCEMENT", "DELETE", PermissionCategory.CLUB_MANAGEMENT),
    CLUB_ANNOUNCEMENT_PERMANENT_DELETE("CLUB_ANNOUNCEMENT:PERMANENT_DELETE", "Permanently delete club announcements from database", "CLUB_ANNOUNCEMENT", "PERMANENT_DELETE", PermissionCategory.CLUB_MANAGEMENT),

    // ==================== VOTING/ELECTION MANAGEMENT ====================
    ELECTION_CREATE("ELECTION:CREATE", "Create elections", "ELECTION", "CREATE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_READ("ELECTION:READ", "View elections", "ELECTION", "READ", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_UPDATE("ELECTION:UPDATE", "Update elections", "ELECTION", "UPDATE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_DELETE("ELECTION:DELETE", "Delete elections", "ELECTION", "DELETE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_MANAGE("ELECTION:MANAGE", "Manage election lifecycle", "ELECTION", "MANAGE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_PUBLISH_RESULTS("ELECTION:PUBLISH_RESULTS", "Publish election results", "ELECTION", "PUBLISH_RESULTS", PermissionCategory.VOTING_MANAGEMENT),

    // Election Admin Permissions
    ELECTION_ADMIN_READ("ELECTION:ADMIN_READ", "Admin view all elections", "ELECTION", "ADMIN_READ", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_ADMIN_UPDATE("ELECTION:ADMIN_UPDATE", "Admin update any election", "ELECTION", "ADMIN_UPDATE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_ADMIN_DELETE("ELECTION:ADMIN_DELETE", "Admin delete any election", "ELECTION", "ADMIN_DELETE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_FORCE_MANAGE("ELECTION:FORCE_MANAGE", "Force manage election lifecycle", "ELECTION", "FORCE_MANAGE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_PERMANENT_DELETE("ELECTION:PERMANENT_DELETE", "Permanently delete elections", "ELECTION", "PERMANENT_DELETE", PermissionCategory.VOTING_MANAGEMENT),
    ELECTION_SUPER_ADMIN("ELECTION:SUPER_ADMIN", "Super admin election operations", "ELECTION", "SUPER_ADMIN", PermissionCategory.VOTING_MANAGEMENT),

    // Candidate Permissions
    CANDIDATE_NOMINATE("CANDIDATE:NOMINATE", "Nominate as candidate", "CANDIDATE", "NOMINATE", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_APPROVE("CANDIDATE:APPROVE", "Approve candidate nominations", "CANDIDATE", "APPROVE", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_VIEW("CANDIDATE:VIEW", "View candidates", "CANDIDATE", "VIEW", PermissionCategory.VOTING_MANAGEMENT),

    // Candidate Admin Permissions
    CANDIDATE_ADMIN_VIEW("CANDIDATE:ADMIN_VIEW", "Admin view all candidates", "CANDIDATE", "ADMIN_VIEW", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_ADMIN_UPDATE("CANDIDATE:ADMIN_UPDATE", "Admin update any candidate", "CANDIDATE", "ADMIN_UPDATE", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_ADMIN_DELETE("CANDIDATE:ADMIN_DELETE", "Admin delete any candidate", "CANDIDATE", "ADMIN_DELETE", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_FORCE_APPROVE("CANDIDATE:FORCE_APPROVE", "Force approve candidates", "CANDIDATE", "FORCE_APPROVE", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_FORCE_REJECT("CANDIDATE:FORCE_REJECT", "Force reject candidates", "CANDIDATE", "FORCE_REJECT", PermissionCategory.VOTING_MANAGEMENT),
    CANDIDATE_DISQUALIFY("CANDIDATE:DISQUALIFY", "Disqualify candidates", "CANDIDATE", "DISQUALIFY", PermissionCategory.VOTING_MANAGEMENT),

    // Vote Permissions
    VOTE_CAST("VOTE:CAST", "Cast vote in elections", "VOTE", "CAST", PermissionCategory.VOTING_MANAGEMENT),
    VOTE_VIEW_RESULTS("VOTE:VIEW_RESULTS", "View voting results", "VOTE", "VIEW_RESULTS", PermissionCategory.VOTING_MANAGEMENT),
    VOTE_VIEW_STATISTICS("VOTE:VIEW_STATISTICS", "View voting statistics", "VOTE", "VIEW_STATISTICS", PermissionCategory.VOTING_MANAGEMENT),

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
    LOST_FOUND_DELETE("LOST_FOUND:DELETE", "Delete lost/found items", "LOST_FOUND", "DELETE", PermissionCategory.LOST_AND_FOUND),

    // ==================== PUSH NOTIFICATIONS ====================
    PUSH_SEND("PUSH:SEND", "Send push notifications", "PUSH", "SEND", PermissionCategory.PUSH_NOTIFICATION),
    PUSH_SEND_BROADCAST("PUSH:SEND_BROADCAST", "Send broadcast push notifications to all users", "PUSH", "SEND_BROADCAST", PermissionCategory.PUSH_NOTIFICATION),
    PUSH_MANAGE("PUSH:MANAGE", "Manage push notification settings and tokens", "PUSH", "MANAGE", PermissionCategory.PUSH_NOTIFICATION);

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
        LOST_AND_FOUND("Lost and Found"),
        VOTING_MANAGEMENT("Voting Management"),
        PUSH_NOTIFICATION("Push Notification");

        private final String displayName;

        PermissionCategory(String displayName) {
            this.displayName = displayName;
        }

    }
}
