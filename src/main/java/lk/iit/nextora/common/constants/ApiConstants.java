package lk.iit.nextora.common.constants;

/**
 * Centralized API path constants used across controllers and tests.
 */

public final class ApiConstants {
    private ApiConstants() {
        throw new IllegalStateException("Constants class - cannot be instantiated");
    }

    // Base paths
    public static final String API_BASE = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_V1 = API_BASE + API_VERSION;
    public static final String ADMIN = API_V1 + "/admin" ;
    public static final String SUPER_ADMIN = API_V1 + "/super-admin" ;
    public static final String AUTH = API_V1 + "/auth";
    public static final String KUPPI = API_V1 + "/kuppi";
    public static final String USER = API_V1 + "/user";
    public static final String CLUBS = API_V1 + "/club";
    public static final String ELECTIONS = API_V1 + "/club/election";
    public static final String PUSH = API_V1 + "/push";
    public static final String NOTIFICATIONS = API_V1 + "/notifications";

    // Authentication endpoints

    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REFRESH = "/refresh-token";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_VERIFY_EMAIL = "/verify-email";
    public static final String AUTH_RESEND_VERIFICATION = "/resend-verification";

    // Forgot Password endpoints
    public static final String AUTH_FORGOT_PASSWORD = "/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";

    // Admin User Management endpoints
    public static final String USER_ADMIN = ADMIN + "/user";
    public static final String USER_SUPER_ADMIN = SUPER_ADMIN + "/user";

    public static final String ADMIN_CREATE_USER = "";
    public static final String ADMIN_CHANGE_PASSWORD_FIRST_LOGIN = "/change-password-first-login";
    public static final String ADMIN_USER_SEARCH = "/search";
    public static final String ADMIN_USER_FILTER = "/filter";
    public static final String ADMIN_USER_STATS = "/stats";

    // User endpoints

    public static final String USER_ME = "/me";
    public static final String USER_ACTIVE = "/active";
    public static final String CHANGE_PASSWORD = "/password";
    public static final String USER_CHANGE_PASSWORD = USER_ME + CHANGE_PASSWORD;
    public static final String USER_BY_ID = "/{id}";
    public static final String USER_SEARCH = "/search";
    public static final String USER_ME_SEARCH = USER_ME + USER_SEARCH;


    public static final String USER_ACTIVATE = USER_BY_ID + "/activate";
    public static final String USER_DEACTIVATE = USER_BY_ID + "/deactivate";
    public static final String USER_SUSPEND = USER_BY_ID + "/suspend";

    public static final String USER_UNLOCK = USER_BY_ID + "/unlock";

    // User endpoints -admin
    public static final String ADMIN_USER = "/admin";
    public static final String USER_RESTORE = USER_BY_ID + "/restore";
    public static final String USER_RESET_PASSWORD = USER_BY_ID + "/reset-password";
    public static final String USER_PERMANENT_DELETE = USER_BY_ID + "/permanent";

    // Admin User Management by Super Admin
    public static final String ADMIN_USER_MANAGEMENT = ADMIN + "/admin-users";
    public static final String ADMIN_USER_BY_ID = "/{adminId}";
    public static final String ADMIN_USER_SOFT_DELETE = ADMIN_USER_BY_ID + "/soft-delete";
    public static final String ADMIN_USER_PERMANENT_DELETE = ADMIN_USER_BY_ID + "/permanent";
    public static final String ADMIN_USER_RESTORE = ADMIN_USER_BY_ID + "/restore";

    // Normal User Management by Super Admin (restore and permanent delete)
    public static final String NORMAL_USER = "/normal";
    public static final String NORMAL_USER_BY_ID = NORMAL_USER + "/{userId}";
    public static final String NORMAL_USER_RESTORE = NORMAL_USER_BY_ID + "/restore";
    public static final String NORMAL_USER_PERMANENT_DELETE = NORMAL_USER_BY_ID + "/permanent";

    // Kuppi endpoints
    public static final String KUPPI_SESSIONS = KUPPI + "/sessions";
    public static final String KUPPI_NOTES = KUPPI + "/notes";


    public static final String KUPPI_COMMENTS = "/comments";
    public static final String KUPPI_PARTICIPANTS = "/participants";
    public static final String KUPPI_MY = "/my";
    public static final String KUPPI_SEARCH = "/search";
    public static final String KUPPI_UPCOMING = "/upcoming";
    public static final String KUPPI_PENDING = "/pending" ;
    public static final String KUPPI_ANALYTICS = KUPPI_MY + "/analytics";
    public static final String KUPPI_STATS = "/stats";
    public static final String KUPPI_JOIN = "/join";
    public static final String KUPPI_LEAVE = "/leave";
    public static final String KUPPI_FEEDBACK = "/feedback";
    public static final String KUPPI_APPROVE = "/approve";
    public static final String KUPPI_REJECT = "/reject";



    public static final String KUPPI_SESSION_BY_ID = "/{sessionId}";
    public static final String KUPPI_SEARCH_SUBJECT = "/search/subject";
    public static final String KUPPI_SEARCH_HOST = "/search/host";
    public static final String KUPPI_SEARCH_DATE = "/search/date";
    public static final String KUPPI_CANCEL = KUPPI_SESSION_BY_ID +"/cancel";
    public static final String KUPPI_RESCHEDULE = KUPPI_SESSION_BY_ID + "/reschedule";

    // Kuppi Admin endpoints
    public static final String KUPPI_ADMIN = ADMIN + "/kuppi";

    public static final String KUPPI_ADMIN_SESSIONS = KUPPI_ADMIN + "/sessions";
    public static final String KUPPI_ADMIN_SESSIONS_BY_ID = "/sessions/{sessionId}";
    public static final String KUPPI_ADMIN_SESSIONS_PERMANENT = "/sessions/{sessionId}/permanent";
    public static final String KUPPI_ADMIN_NOTES = KUPPI_ADMIN + "/notes";
    public static final String KUPPI_ADMIN_NOTES_BY_ID = "/notes/{noteId}";
    public static final String KUPPI_ADMIN_NOTES_PERMANENT = "/notes/{noteId}/permanent";

    // Kuppi Notes endpoints
    public static final String KUPPI_NOTE_BY_ID = "/{noteId}";
    public static final String KUPPI_NOTE_SESSION = "/session/{sessionId}";
    public static final String KUPPI_NOTE_UPLOAD = "/upload";
    public static final String KUPPI_NOTE_DOWNLOAD_FILE = "/{noteId}/download/file";
    public static final String KUPPI_NOTE_UPDATE_UPLOAD = "/{noteId}/upload";

    public static final String KUPPI_DOWNLOAD = KUPPI_NOTE_BY_ID + "/download";

    // Kuppi Application endpoints (Students applying to become Kuppi Students)
    public static final String KUPPI_APPLICATIONS = KUPPI + "/applications";
    public static final String KUPPI_APPLICATION_BY_ID = "/{applicationId}";
    public static final String KUPPI_APPLICATION_MY = "/my";
    public static final String KUPPI_APPLICATION_ACTIVE = "/active";
    public static final String KUPPI_APPLICATION_CAN_APPLY = "/can-apply";
    public static final String KUPPI_APPLICATION_IS_KUPPI_STUDENT = "/is-kuppi-student";

    // Kuppi Application Admin endpoints
    public static final String KUPPI_ADMIN_APPLICATION_BASE = "/applications";
    public static final String KUPPI_ADMIN_APPLICATION_BY_ID = KUPPI_ADMIN_APPLICATION_BASE + "/{applicationId}";
    public static final String KUPPI_ADMIN_APPLICATION_STATUS = KUPPI_ADMIN_APPLICATION_BASE + "/status/{status}";
    public static final String KUPPI_ADMIN_APPLICATION_PENDING = KUPPI_ADMIN_APPLICATION_BASE + "/pending";
    public static final String KUPPI_ADMIN_APPLICATION_ACTIVE = KUPPI_ADMIN_APPLICATION_BASE + "/active";
    public static final String KUPPI_ADMIN_APPLICATION_SEARCH = KUPPI_ADMIN_APPLICATION_BASE + "/search";
    public static final String KUPPI_ADMIN_APPLICATION_APPROVE = KUPPI_ADMIN_APPLICATION_BASE + "/{applicationId}/approve";
    public static final String KUPPI_ADMIN_APPLICATION_REJECT = KUPPI_ADMIN_APPLICATION_BASE + "/{applicationId}/reject";
    public static final String KUPPI_ADMIN_APPLICATION_UNDER_REVIEW = KUPPI_ADMIN_APPLICATION_BASE + "/{applicationId}/under-review";
    public static final String KUPPI_ADMIN_APPLICATION_STATS = KUPPI_ADMIN_APPLICATION_BASE + "/stats";
    public static final String KUPPI_ADMIN_APPLICATION_REVOKE = KUPPI_ADMIN_APPLICATION_BASE + "/revoke/{studentId}";
    public static final String KUPPI_ADMIN_APPLICATION_PERMANENT = KUPPI_ADMIN_APPLICATION_BASE + "/{applicationId}/permanent";

    // Kuppi Students endpoints (View Kuppi Students who can host sessions)
    public static final String KUPPI_STUDENTS = KUPPI + "/students";
    public static final String KUPPI_STUDENT_BY_ID = "/{studentId}";
    public static final String KUPPI_STUDENTS_SEARCH_NAME = "/search/name";
    public static final String KUPPI_STUDENTS_SEARCH_SUBJECT = "/search/subject";
    public static final String KUPPI_STUDENTS_BY_FACULTY = "/faculty/{faculty}";
    public static final String KUPPI_STUDENTS_TOP_RATED = "/top-rated";

    // ==================== Club & Voting Endpoints ====================

    // Clubs (voting module - for elections)

    public static final String CLUB_BY_ID = "/{clubId}";
    public static final String CLUB_BY_CODE = "/code/{clubCode}";
    public static final String CLUB_SEARCH = "/search";
    public static final String CLUB_FACULTY = "/faculty/{faculty}";
    public static final String CLUB_OPEN_REGISTRATION = "/open-registration";
    public static final String CLUB_JOIN =  "/join";
    public static final String CLUB_LEAVE = "/{clubId}/leave";

    public static final String CLUB_MEMBERS = "/{clubId}/members";
    public static final String CLUB_MEMBERS_ACTIVE = "/{clubId}/members/active";
    public static final String CLUB_MEMBERS_PENDING = "/{clubId}/members/pending";
    public static final String MY_MEMBERSHIPS = "/my-memberships";
    public static final String MEMBERSHIP_BY_ID = "/memberships/{membershipId}";
    public static final String MEMBERSHIP_APPROVE = "/memberships/{membershipId}/approve";
    public static final String MEMBERSHIP_REJECT = "/memberships/{membershipId}/reject";
    public static final String MEMBERSHIP_SUSPEND = "/memberships/{membershipId}/suspend";

    // Club membership extended endpoints
    public static final String MEMBERSHIP_CHANGE_POSITION = "/memberships/{membershipId}/position";
    public static final String MEMBERSHIP_BULK_APPROVE = "/{clubId}/memberships/bulk-approve";
    public static final String CLUB_TOGGLE_REGISTRATION = "/{clubId}/toggle-registration";
    public static final String CLUB_STATISTICS = "/{clubId}/statistics";

    // Club-scoped election endpoints
    public static final String CLUB_ELECTIONS = "/{clubId}/elections";
    public static final String CLUB_ELECTIONS_ACTIVE = "/{clubId}/elections/active";
    public static final String CLUB_ELECTIONS_UPCOMING = "/{clubId}/elections/upcoming";
    public static final String CLUB_ELECTIONS_COMPLETED = "/{clubId}/elections/completed";

    // ==================== Club Dashboard Endpoints ====================
    public static final String CLUB_DASHBOARD = API_V1 + "/club/dashboard";
    public static final String CLUB_DASHBOARD_STUDENT = "/student";
    public static final String CLUB_DASHBOARD_CLUB_MEMBER = "/club-member";
    public static final String CLUB_DASHBOARD_STAFF = "/staff";
    public static final String CLUB_DASHBOARD_ADMIN = "/admin";
    public static final String CLUB_DASHBOARD_ACADEMIC = "/academic";


    // ==================== Club Announcements Endpoints ====================
    public static final String CLUB_ANNOUNCEMENTS = API_V1 + "/club/announcements";
    public static final String CLUB_ANNOUNCEMENT_BY_ID = "/{announcementId}";
    public static final String CLUB_ANNOUNCEMENTS_BY_CLUB = "/club/{clubId}";
    public static final String CLUB_ANNOUNCEMENTS_PUBLIC = "/club/{clubId}/public";
    public static final String CLUB_ANNOUNCEMENTS_PINNED = "/club/{clubId}/pinned";
    public static final String CLUB_ANNOUNCEMENTS_SEARCH = "/search";
    public static final String CLUB_ANNOUNCEMENT_PIN = "/{announcementId}/pin";
    public static final String CLUB_ANNOUNCEMENT_UNPIN = "/{announcementId}/unpin";

    // ==================== Club Admin Endpoints ====================
    public static final String CLUB_ADMIN = ADMIN + "/club";
    public static final String CLUB_ADMIN_ALL = "/all";
    public static final String CLUB_ADMIN_BY_ID = "/{clubId}";
    public static final String CLUB_ADMIN_STATS = "/stats";
    public static final String CLUB_ADMIN_ACTIVITY_LOG = "/{clubId}/activity-log";
    public static final String CLUB_ADMIN_MEMBERS_BY_POSITION = "/{clubId}/members/position/{position}";
    public static final String CLUB_ADMIN_FORCE_SUSPEND = "/memberships/{membershipId}/force-suspend";
    public static final String CLUB_ADMIN_FORCE_ACTIVATE = "/memberships/{membershipId}/force-activate";
    public static final String CLUB_ADMIN_PLATFORM_STATS = "/platform-stats";
    public static final String CLUB_ADMIN_PERMANENT_DELETE = "/{clubId}/permanent";
    public static final String CLUB_ADMIN_ANNOUNCEMENT_PERMANENT_DELETE = "/announcements/{announcementId}/permanent";

    // Election endpoints

    public static final String ELECTION_BY_ID = "/{electionId}";
    public static final String ELECTION_DETAILS = ELECTION_BY_ID + "/details";
    public static final String ELECTION_BY_CLUB = "/club/{clubId}";
    public static final String ELECTION_BY_STATUS = "/status/{status}";
    public static final String ELECTION_UPCOMING = "/upcoming";
    public static final String ELECTION_VOTABLE = "/votable";
    public static final String ELECTION_SEARCH = "/search";
    public static final String ELECTION_OPEN_NOMINATIONS = ELECTION_BY_ID + "/open-nominations";
    public static final String ELECTION_CLOSE_NOMINATIONS = ELECTION_BY_ID + "/close-nominations";
    public static final String ELECTION_OPEN_VOTING = ELECTION_BY_ID + "/open-voting";
    public static final String ELECTION_CLOSE_VOTING = ELECTION_BY_ID +"/close-voting";
    public static final String ELECTION_PUBLISH_RESULTS = ELECTION_BY_ID + "/publish-results";
    public static final String ELECTION_CANCEL = ELECTION_BY_ID + "/cancel";

    // Candidate endpoints
    public static final String CANDIDATE = "/candidate";
    public static final String CANDIDATE_BY_ID = CANDIDATE + "/{candidateId}";
    public static final String CANDIDATE_NOMINATE = CANDIDATE + "/nominate";
    public static final String CANDIDATE_UPDATE = CANDIDATE_NOMINATE + "/{candidateId}";
    public static final String CANDIDATE_DELETE =  CANDIDATE_NOMINATE + "/{candidateId}";
    public static final String CANDIDATE_REVIEW =  CANDIDATE + "/review";
    public static final String CANDIDATE_WITHDRAW =  CANDIDATE_BY_ID + "/withdraw";
    public static final String ELECTION_CANDIDATES = ELECTION_BY_ID + CANDIDATE;
    public static final String CANDIDATES_APPROVED = ELECTION_BY_ID + CANDIDATE + "/approved";
    public static final String CANDIDATES_PENDING = ELECTION_BY_ID + CANDIDATE + "/pending";
    public static final String MY_CANDIDACIES =  CANDIDATE + "/my";

    // Voting endpoints
    public static final String VOTE = "/vote";
    public static final String HAS_VOTED = ELECTION_BY_ID + "/has-voted";
    public static final String VERIFY_VOTE = ELECTION_BY_ID + "/verify-vote";
    public static final String ELECTION_RESULTS = ELECTION_BY_ID + "/results";
    public static final String ELECTION_LIVE_COUNT = ELECTION_BY_ID + "/live-count";

    // ==================== Voting Admin Endpoints ====================
    public static final String ELECTION_ADMIN = ADMIN + "/election";

    public static final String ELECTION_ADMIN_ELECTION_PERMANENT = "/{electionId}/permanent";
    public static final String ELECTION_ADMIN_FORCE_OPEN_NOMINATIONS = "/{electionId}/force-open-nominations";
    public static final String ELECTION_ADMIN_FORCE_CLOSE_NOMINATIONS = "/{electionId}/force-close-nominations";
    public static final String ELECTION_ADMIN_FORCE_OPEN_VOTING = "/{electionId}/force-open-voting";
    public static final String ELECTION_ADMIN_FORCE_CLOSE_VOTING = "/{electionId}/force-close-voting";
    public static final String ELECTION_ADMIN_FORCE_PUBLISH_RESULTS = "/{electionId}/force-publish-results";
    public static final String ELECTION_ADMIN_FORCE_CANCEL = "/{electionId}/force-cancel";
    public static final String ELECTION_ADMIN_CANDIDATES_BY_ELECTION = "/{electionId}/candidates";
    public static final String ELECTION_ADMIN_CANDIDATE_FORCE_APPROVE_PATH = "/{electionId}/candidates/{candidateId}/force-approve";
    public static final String ELECTION_ADMIN_CANDIDATE_FORCE_REJECT_PATH = "/{electionId}/candidates/{candidateId}/force-reject";
    public static final String ELECTION_ADMIN_CANDIDATE_DISQUALIFY_PATH = "/{electionId}/candidates/{candidateId}/disqualify";
    public static final String ELECTION_ADMIN_CANDIDATES_UPDATE = "/candidates/{candidateId}";
    public static final String ELECTION_ADMIN_LIVE_VOTES = "/{electionId}/live-votes";
    public static final String ELECTION_ADMIN_STATISTICS_PATH = "/statistics";
    public static final String ELECTION_ADMIN_STATISTICS_CLUBS = "/statistics/clubs/{clubId}";
    public static final String ELECTION_ADMIN_STATISTICS_ELECTIONS = "/statistics/elections/{electionId}";
    public static final String ELECTION_ADMIN_STATISTICS_SUMMARY = "/statistics/summary";
    public static final String ELECTION_ADMIN_PROCESS_STATUS_PATH = "/process-status-updates";
    public static final String ELECTION_ADMIN_RESET_VOTES_PATH = "/{electionId}/reset-votes";



















//    public static final String VOTING_ADMIN_CLUBS = ELECTION_ADMIN + "/clubs";
//    public static final String VOTING_ADMIN_CLUB_BY_ID = VOTING_ADMIN_CLUBS + "/{clubId}";
//    public static final String VOTING_ADMIN_MEMBERSHIPS = ELECTION_ADMIN + "/memberships";
//    public static final String VOTING_ADMIN_MEMBERSHIP_FORCE_APPROVE = "/{membershipId}/force-approve";
//    public static final String VOTING_ADMIN_MEMBERSHIP_FORCE_REJECT = "/{membershipId}/force-reject";
//    public static final String VOTING_ADMIN_MEMBERSHIP_SUSPEND = "/{membershipId}/suspend";
//    public static final String VOTING_ADMIN_CLUB_MEMBERSHIPS = VOTING_ADMIN_CLUBS + "/{clubId}/memberships";
//
//    public static final String VOTING_ADMIN_ELECTIONS = ELECTION_ADMIN + "/elections";
//    public static final String VOTING_ADMIN_ELECTION_BY_ID = VOTING_ADMIN_ELECTIONS + "/{electionId}";
//
//    public static final String VOTING_ADMIN_CANDIDATES = VOTING_ADMIN_ELECTIONS + "/{electionId}/candidates";
//    public static final String VOTING_ADMIN_CANDIDATE_FORCE_APPROVE = "/candidates/{candidateId}/force-approve";
//    public static final String VOTING_ADMIN_CANDIDATE_FORCE_REJECT = "/candidates/{candidateId}/force-reject";
//    public static final String VOTING_ADMIN_CANDIDATE_DISQUALIFY = "/candidates/{candidateId}/disqualify";
//
//
//    public static final String VOTING_ADMIN_STATISTICS = ELECTION_ADMIN + "/statistics";
//
//    public static final String VOTING_ADMIN_CLUB_STATISTICS = VOTING_ADMIN_CLUBS + "/{clubId}/statistics";
//
//    public static final String VOTING_SUPER_ADMIN_PROCESS_STATUS = ELECTION_ADMIN + "/elections/process-status-updates";
//
//    public static final String VOTING_SUPER_ADMIN_AUDIT_LOG = ELECTION_ADMIN + "/audit-log";
//
//    public static final String VOTING_SUPER_ADMIN_BULK_APPROVE = ELECTION_ADMIN + "/memberships/bulk-approve";
//    public static final String VOTING_SUPER_ADMIN_BULK_CANCEL = ELECTION_ADMIN + "/elections/bulk-cancel";
//
//    public static final String VOTING_SUPER_ADMIN_CLUB_EXPORT = ELECTION_ADMIN + "/clubs/{clubId}/export";
//    public static final String VOTING_SUPER_ADMIN_ELECTION_EXPORT = ELECTION_ADMIN + "/elections/{electionId}/export";
//
//    public static final String VOTING_SUPER_ADMIN_PERMANENT_DELETE_CLUB = ELECTION_ADMIN + "/clubs/{clubId}/permanent";
//    public static final String VOTING_SUPER_ADMIN_RESET_VOTES = ELECTION_ADMIN + "/elections/{electionId}/reset-votes";
//    public static final String VOTING_SUPER_ADMIN_INVALIDATE_VOTES = ELECTION_ADMIN + "/voters/{voterId}/invalidate-votes";
//    public static final String VOTING_SUPER_ADMIN_CONFIG = ELECTION_ADMIN + "/config";

}


