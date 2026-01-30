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

    // Authentication endpoints
    public static final String AUTH = API_V1 + "/auth";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_REFRESH = "/refresh-token";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_VERIFY_EMAIL = "/verify-email";
    public static final String AUTH_RESEND_VERIFICATION = "/resend-verification";

    // Admin User Management endpoints
    public static final String ADMIN_USERS = API_V1 + "/admin/users";
    public static final String ADMIN_CREATE_USER = "";
    public static final String ADMIN_CHANGE_PASSWORD_FIRST_LOGIN = "/change-password-first-login";

    // User endpoints
    public static final String USERS = API_V1 + "/users";
    public static final String USER_ME = "/me";
    public static final String USER_ACTIVE = "/active";
    public static final String CHANGE_PASSWORD = "/password";
    public static final String USER_CHANGE_PASSWORD = USER_ME + CHANGE_PASSWORD;
    public static final String USER_BY_ID = "/{id}";
    public static final String USER_SEARCH = "/search";
    public static final String USER_ME_SEARCH = USER_ME + USER_SEARCH;
    public static final String USER_RESTORE = USER_BY_ID + "/restore";
    public static final String USER_ADMIN = "/admin";
    public static final String USER_ACTIVATE = USER_BY_ID + "/activate";
    public static final String USER_DEACTIVATE = USER_BY_ID + "/deactivate";
    public static final String USER_RESET_PASSWORD = USER_BY_ID + "/reset-password";
    public static final String USER_UNLOCK = USER_BY_ID + "/unlock";

    // Student Role Management endpoints (Admin-controlled role upgrades)
    public static final String STUDENT_ROLES = API_V1 + "/students";
    public static final String STUDENT_ROLES_BY_ID = "/{studentId}/roles";
    public static final String STUDENT_ADD_ROLE = "/{studentId}/roles/add";
    public static final String STUDENT_REMOVE_ROLE = "/{studentId}/roles/remove";
    public static final String STUDENT_ADD_CLUB_MEMBER = "/{studentId}/roles/club-member";
    public static final String STUDENT_ADD_BATCH_REP = "/{studentId}/roles/batch-rep";
    public static final String STUDENT_ADD_KUPPI_STUDENT = "/{studentId}/roles/kuppi-student";
    public static final String STUDENTS_BY_ROLE = "/by-role/{roleType}";
    public static final String STUDENT_ROLE_HISTORY = "/{studentId}/roles/history";

    // Kuppi endpoints
    public static final String KUPPI = API_V1 + "/kuppi";
    public static final String KUPPI_SESSIONS = KUPPI + "/sessions";
    public static final String KUPPI_NOTES = KUPPI + "/notes";
    public static final String KUPPI_COMMENTS = "/comments";
    public static final String KUPPI_PARTICIPANTS = "/participants";
    public static final String KUPPI_MY = "/my";
    public static final String KUPPI_SEARCH = "/search";
    public static final String KUPPI_UPCOMING = "/upcoming";
    public static final String KUPPI_PENDING = "/pending" ;
    public static final String KUPPI_ANALYTICS = "/analytics";
    public static final String KUPPI_STATS = "/stats";
    public static final String KUPPI_JOIN = "/join";
    public static final String KUPPI_LEAVE = "/leave";
    public static final String KUPPI_FEEDBACK = "/feedback";
    public static final String KUPPI_APPROVE = "/approve";
    public static final String KUPPI_REJECT = "/reject";
    public static final String KUPPI_CANCEL = "/cancel";
    public static final String KUPPI_RESCHEDULE = "/reschedule";
    public static final String KUPPI_DOWNLOAD = "/download";
    public static final String KUPPI_SESSION_BY_ID = "/{sessionId}";
    public static final String KUPPI_SEARCH_SUBJECT = "/search/subject";
    public static final String KUPPI_SEARCH_HOST = "/search/host";
    public static final String KUPPI_SEARCH_DATE = "/search/date";

    // Kuppi Admin endpoints
    public static final String KUPPI_ADMIN = API_V1 + "/admin/kuppi";
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

    // ==================== Club & Voting Endpoints ====================

    // Club Module (standalone club management)
    public static final String CLUB_MODULE = API_V1 + "/club-management";

    // Clubs (voting module - for elections)
    public static final String CLUBS = API_V1 + "/clubs";
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

    // Election endpoints
    public static final String ELECTIONS = API_V1 + "/elections";
    public static final String ELECTION_BY_ID = "/{electionId}";
    public static final String ELECTION_DETAILS = "/{electionId}/details";
    public static final String ELECTION_BY_CLUB = "/club/{clubId}";
    public static final String ELECTION_BY_STATUS = "/status/{status}";
    public static final String ELECTION_UPCOMING = "/upcoming";
    public static final String ELECTION_VOTABLE = "/votable";
    public static final String ELECTION_SEARCH = "/search";
    public static final String ELECTION_OPEN_NOMINATIONS = "/{electionId}/open-nominations";
    public static final String ELECTION_CLOSE_NOMINATIONS = "/{electionId}/close-nominations";
    public static final String ELECTION_OPEN_VOTING = "/{electionId}/open-voting";
    public static final String ELECTION_CLOSE_VOTING = "/{electionId}/close-voting";
    public static final String ELECTION_PUBLISH_RESULTS = "/{electionId}/publish-results";
    public static final String ELECTION_CANCEL = "/{electionId}/cancel";

    // Candidate endpoints
    public static final String CANDIDATES = "/candidates";
    public static final String CANDIDATE_BY_ID = "/candidates/{candidateId}";
    public static final String CANDIDATE_NOMINATE = "/candidates/nominate";
    public static final String CANDIDATE_UPDATE = "/candidates/nominate/{candidateId}";
    public static final String CANDIDATE_DELETE = "/candidates/nominate/{candidateId}";
    public static final String CANDIDATE_REVIEW = "/candidates/review";
    public static final String CANDIDATE_WITHDRAW = "/candidates/{candidateId}/withdraw";
    public static final String ELECTION_CANDIDATES = "/{electionId}/candidates";
    public static final String CANDIDATES_APPROVED = "/{electionId}/candidates/approved";
    public static final String CANDIDATES_PENDING = "/{electionId}/candidates/pending";
    public static final String MY_CANDIDACIES = "/candidates/my";

    // Voting endpoints
    public static final String VOTE = "/vote";
    public static final String HAS_VOTED = "/{electionId}/has-voted";
    public static final String VERIFY_VOTE = "/{electionId}/verify-vote";
    public static final String ELECTION_RESULTS = "/{electionId}/results";
    public static final String ELECTION_LIVE_COUNT = "/{electionId}/live-count";

    // ==================== Voting Admin Endpoints ====================
    public static final String VOTING_ADMIN = API_V1 + "/admin/voting";
    public static final String VOTING_ADMIN_CLUBS = VOTING_ADMIN + "/clubs";
    public static final String VOTING_ADMIN_CLUB_BY_ID = VOTING_ADMIN_CLUBS + "/{clubId}";
    public static final String VOTING_ADMIN_MEMBERSHIPS = VOTING_ADMIN + "/memberships";
    public static final String VOTING_ADMIN_MEMBERSHIP_FORCE_APPROVE = "/{membershipId}/force-approve";
    public static final String VOTING_ADMIN_MEMBERSHIP_FORCE_REJECT = "/{membershipId}/force-reject";
    public static final String VOTING_ADMIN_MEMBERSHIP_SUSPEND = "/{membershipId}/suspend";
    public static final String VOTING_ADMIN_CLUB_MEMBERSHIPS = VOTING_ADMIN_CLUBS + "/{clubId}/memberships";

    public static final String VOTING_ADMIN_ELECTIONS = VOTING_ADMIN + "/elections";
    public static final String VOTING_ADMIN_ELECTION_BY_ID = VOTING_ADMIN_ELECTIONS + "/{electionId}";
    public static final String VOTING_ADMIN_FORCE_OPEN_NOMINATIONS = "/{electionId}/force-open-nominations";
    public static final String VOTING_ADMIN_FORCE_CLOSE_NOMINATIONS = "/{electionId}/force-close-nominations";
    public static final String VOTING_ADMIN_FORCE_OPEN_VOTING = "/{electionId}/force-open-voting";
    public static final String VOTING_ADMIN_FORCE_CLOSE_VOTING = "/{electionId}/force-close-voting";
    public static final String VOTING_ADMIN_FORCE_PUBLISH_RESULTS = "/{electionId}/force-publish-results";
    public static final String VOTING_ADMIN_FORCE_CANCEL = "/{electionId}/force-cancel";

    public static final String VOTING_ADMIN_CANDIDATES = VOTING_ADMIN_ELECTIONS + "/{electionId}/candidates";
    public static final String VOTING_ADMIN_CANDIDATE_FORCE_APPROVE = "/candidates/{candidateId}/force-approve";
    public static final String VOTING_ADMIN_CANDIDATE_FORCE_REJECT = "/candidates/{candidateId}/force-reject";
    public static final String VOTING_ADMIN_CANDIDATE_DISQUALIFY = "/candidates/{candidateId}/disqualify";

    public static final String VOTING_ADMIN_LIVE_VOTES = "/{electionId}/live-votes";
    public static final String VOTING_ADMIN_STATISTICS = VOTING_ADMIN + "/statistics";
    public static final String VOTING_ADMIN_STATISTICS_PATH = "/statistics";
    public static final String VOTING_ADMIN_CLUB_STATISTICS = VOTING_ADMIN_CLUBS + "/{clubId}/statistics";
    public static final String VOTING_ADMIN_STATISTICS_CLUBS = "/statistics/clubs/{clubId}";
    public static final String VOTING_ADMIN_STATISTICS_ELECTIONS = "/statistics/elections/{electionId}";
    public static final String VOTING_ADMIN_STATISTICS_SUMMARY = "/statistics/summary";
    public static final String VOTING_ADMIN_ELECTION_PERMANENT = "/{electionId}/permanent";
    public static final String VOTING_ADMIN_CANDIDATES_BY_ELECTION = "/{electionId}/candidates";
    public static final String VOTING_ADMIN_CANDIDATE_FORCE_APPROVE_PATH = "/{electionId}/candidates/{candidateId}/force-approve";
    public static final String VOTING_ADMIN_CANDIDATE_FORCE_REJECT_PATH = "/{electionId}/candidates/{candidateId}/force-reject";
    public static final String VOTING_ADMIN_CANDIDATE_DISQUALIFY_PATH = "/{electionId}/candidates/{candidateId}/disqualify";
    public static final String VOTING_ADMIN_CANDIDATES_UPDATE = "/candidates/{candidateId}";
    public static final String VOTING_ADMIN_RESET_VOTES_PATH = "/{electionId}/reset-votes";

    public static final String VOTING_SUPER_ADMIN_PROCESS_STATUS = VOTING_ADMIN + "/elections/process-status-updates";
    public static final String VOTING_SUPER_ADMIN_PROCESS_STATUS_PATH = "/process-status-updates";
    public static final String VOTING_SUPER_ADMIN_AUDIT_LOG = VOTING_ADMIN + "/audit-log";

    public static final String VOTING_SUPER_ADMIN_BULK_APPROVE = VOTING_ADMIN + "/memberships/bulk-approve";
    public static final String VOTING_SUPER_ADMIN_BULK_CANCEL = VOTING_ADMIN + "/elections/bulk-cancel";

    public static final String VOTING_SUPER_ADMIN_CLUB_EXPORT = VOTING_ADMIN + "/clubs/{clubId}/export";
    public static final String VOTING_SUPER_ADMIN_ELECTION_EXPORT = VOTING_ADMIN + "/elections/{electionId}/export";

    public static final String VOTING_SUPER_ADMIN_PERMANENT_DELETE_CLUB = VOTING_ADMIN + "/clubs/{clubId}/permanent";
    public static final String VOTING_SUPER_ADMIN_RESET_VOTES = VOTING_ADMIN + "/elections/{electionId}/reset-votes";
    public static final String VOTING_SUPER_ADMIN_INVALIDATE_VOTES = VOTING_ADMIN + "/voters/{voterId}/invalidate-votes";
    public static final String VOTING_SUPER_ADMIN_CONFIG = VOTING_ADMIN + "/config";

}


