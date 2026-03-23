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
    public static final String ADMIN = API_V1 + "/admin";

    // Authentication endpoints
    public static final String AUTH = API_V1 + "/auth";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_REFRESH = "/refresh-token";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_FORGOT_PASSWORD = "/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/reset-password";

    // User endpoints
    public static final String USERS = API_V1 + "/users";
    public static final String USER = API_V1 + "/user";
    public static final String USER_ME = "/me";
    public static final String USER_ACTIVE = "/active";
    public static final String CHANGE_PASSWORD = "/password";
    public static final String USER_CHANGE_PASSWORD = USER_ME + CHANGE_PASSWORD;
    public static final String USER_BY_ID = "/{id}";
    public static final String USER_SEARCH = "/search";
    public static final String USER_ME_SEARCH = USER_ME + USER_SEARCH;
    public static final String USER_RESTORE = USER_BY_ID + "/restore";

    // Admin User Management
    public static final String USER_ADMIN = API_V1 + "/admin/users";
    public static final String ADMIN_USER_STATS = "/stats";
    public static final String ADMIN_USER_SEARCH = "/search";
    public static final String ADMIN_USER_FILTER = "/filter";
    public static final String USER_ACTIVATE = "/{id}/activate";
    public static final String USER_DEACTIVATE = "/{id}/deactivate";
    public static final String USER_SUSPEND = "/{id}/suspend";
    public static final String USER_UNLOCK = "/{id}/unlock";
    public static final String USER_RESET_PASSWORD = "/{id}/reset-password";

    // Super Admin User Management
    public static final String USER_SUPER_ADMIN = API_V1 + "/super-admin/users";
    public static final String ADMIN_USER = "/admin";
    public static final String ADMIN_USER_BY_ID = "/admin/{id}";
    public static final String ADMIN_USER_PERMANENT_DELETE = "/admin/{id}/permanent";
    public static final String ADMIN_USER_RESTORE = "/admin/{id}/restore";
    public static final String NORMAL_USER_RESTORE = "/normal/{id}/restore";
    public static final String NORMAL_USER_PERMANENT_DELETE = "/normal/{id}/permanent";

    // Kuppi endpoints
    public static final String KUPPI = API_V1 + "/kuppi";
    public static final String KUPPI_SESSIONS = KUPPI + "/sessions";
    public static final String KUPPI_NOTES = KUPPI + "/notes";
    public static final String KUPPI_COMMENTS = "/comments";
    public static final String KUPPI_PARTICIPANTS = "/participants";
    public static final String KUPPI_MY = "/my";
    public static final String KUPPI_SEARCH = "/search";
    public static final String KUPPI_UPCOMING = "/upcoming";
    public static final String KUPPI_PENDING = "/pending";
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

    // Kuppi Session detail endpoints
    public static final String KUPPI_SESSION_BY_ID = "/{sessionId}";
    public static final String KUPPI_SEARCH_SUBJECT = "/search-subject";
    public static final String KUPPI_SEARCH_HOST = "/search-host";
    public static final String KUPPI_SEARCH_DATE = "/search-date";

    // Kuppi Student endpoints
    public static final String KUPPI_STUDENTS = KUPPI + "/students";
    public static final String KUPPI_STUDENT_BY_ID = "/{studentId}";
    public static final String KUPPI_STUDENTS_SEARCH_NAME = "/search/name";
    public static final String KUPPI_STUDENTS_SEARCH_SUBJECT = "/search/subject";
    public static final String KUPPI_STUDENTS_BY_FACULTY = "/faculty/{faculty}";

    // Kuppi Note endpoints
    public static final String KUPPI_NOTE_SESSION = "/session/{sessionId}";
    public static final String KUPPI_NOTE_BY_ID = "/{noteId}";
    public static final String KUPPI_NOTE_UPLOAD = "/upload";
    public static final String KUPPI_NOTE_DOWNLOAD_FILE = "/{noteId}/file";
    public static final String KUPPI_NOTE_UPDATE_UPLOAD = "/{noteId}";

    // Kuppi Admin endpoints
    public static final String KUPPI_ADMIN = API_V1 + "/admin/kuppi";
    public static final String KUPPI_ADMIN_SESSIONS = KUPPI_ADMIN + "/sessions";
    public static final String KUPPI_ADMIN_NOTES = KUPPI_ADMIN + "/notes";

    // Kuppi Admin Application endpoints
    public static final String KUPPI_ADMIN_APPLICATION_BASE = "/applications";
    public static final String KUPPI_ADMIN_APPLICATION_STATUS = "/applications/status/{status}";
    public static final String KUPPI_ADMIN_APPLICATION_PENDING = "/applications/pending";
    public static final String KUPPI_ADMIN_APPLICATION_ACTIVE = "/applications/active";
    public static final String KUPPI_ADMIN_APPLICATION_BY_ID = "/applications/{id}";
    public static final String KUPPI_ADMIN_APPLICATION_SEARCH = "/applications/search";
    public static final String KUPPI_ADMIN_APPLICATION_STATS = "/applications/stats";
    public static final String KUPPI_ADMIN_APPLICATION_APPROVE = "/applications/{id}/approve";
    public static final String KUPPI_ADMIN_APPLICATION_REJECT = "/applications/{id}/reject";
    public static final String KUPPI_ADMIN_APPLICATION_UNDER_REVIEW = "/applications/{id}/under-review";
    public static final String KUPPI_ADMIN_APPLICATION_PERMANENT = "/applications/{id}/permanent";
    public static final String KUPPI_ADMIN_APPLICATION_REVOKE = "/applications/{id}/revoke";
    public static final String KUPPI_ADMIN_SESSIONS_PERMANENT = "/sessions/{id}/permanent";
    public static final String KUPPI_ADMIN_NOTES_PERMANENT = "/notes/{id}/permanent";

    // ==================== Event Endpoints ====================

    // Event Module
    public static final String EVENTS = API_V1 + "/events";
    public static final String EVENT_BY_ID = "/{eventId}";
    public static final String EVENT_SEARCH = "/search";
    public static final String EVENT_UPCOMING = "/upcoming";
    public static final String EVENT_ONGOING = "/ongoing";
    public static final String EVENT_PAST = "/past";
    public static final String EVENT_MY = "/my";
    public static final String EVENT_PUBLISH = "/publish";
    public static final String EVENT_CANCEL = "/cancel";
    public static final String EVENT_RESCHEDULE = "/reschedule";
    public static final String EVENT_ANALYTICS = "/analytics";
    public static final String EVENT_STATS = "/stats";

    // Event Admin endpoints
    public static final String EVENT_ADMIN = API_V1 + "/admin/events";
    public static final String EVENT_ADMIN_BY_ID = EVENT_ADMIN + "/{eventId}";

    // ==================== Lost & Found Endpoints ====================

    // Base path for the Lost & Found module — same level as KUPPI = API_V1 + "/kuppi"
    public static final String LOST_AND_FOUND = API_V1 + "/lost-and-found";

    // Controller base paths
    public static final String LOST_AND_FOUND_ITEMS = LOST_AND_FOUND + "/items";
    public static final String LOST_AND_FOUND_CLAIMS = LOST_AND_FOUND + "/claims";

    // Sub-path constants — mirrors the KUPPI_MY / KUPPI_SEARCH / KUPPI_APPROVE pattern
    public static final String LOST_AND_FOUND_MY = "/my";
    public static final String LOST_AND_FOUND_SEARCH = "/search";
    public static final String LOST_AND_FOUND_STATUS = "/status/{status}";
    public static final String LOST_AND_FOUND_APPROVE = "/approve";
    public static final String LOST_AND_FOUND_REJECT = "/reject";

    // Admin endpoint — mirrors KUPPI_ADMIN = ADMIN + "/kuppi"
    public static final String LOST_AND_FOUND_ADMIN = API_V1 + "/admin/lost-and-found";


    // ==================== Club & Voting Endpoints ====================

    // Club Module (standalone club management)
    public static final String CLUB_MODULE = API_V1 + "/club-management";

    // Club Admin endpoints
    public static final String CLUB_ADMIN = API_V1 + "/admin/clubs";
    public static final String CLUB_ADMIN_STATS = "/stats";
    public static final String CLUB_ADMIN_ACTIVITY_LOG = "/activity-log";
    public static final String MEMBERSHIP_CHANGE_POSITION = "/memberships/{membershipId}/change-position";
    public static final String MEMBERSHIP_BULK_APPROVE = "/memberships/bulk-approve";
    public static final String CLUB_TOGGLE_REGISTRATION = "/{clubId}/toggle-registration";
    public static final String CLUB_STATISTICS = "/{clubId}/statistics";
    public static final String CLUB_ADMIN_PERMANENT_DELETE = "/{clubId}/permanent";
    public static final String CLUB_ADMIN_ANNOUNCEMENT_PERMANENT_DELETE = "/announcements/{announcementId}/permanent";

    // Club Announcement endpoints
    public static final String CLUB_ANNOUNCEMENTS = API_V1 + "/club-announcements";
    public static final String CLUB_ANNOUNCEMENT_BY_ID = "/{announcementId}";
    public static final String CLUB_ANNOUNCEMENTS_BY_CLUB = "/club/{clubId}";
    public static final String CLUB_ANNOUNCEMENTS_PUBLIC = "/public";
    public static final String CLUB_ANNOUNCEMENTS_PINNED = "/pinned";
    public static final String CLUB_ANNOUNCEMENTS_SEARCH = "/search";
    public static final String CLUB_ANNOUNCEMENT_PIN = "/{announcementId}/pin";
    public static final String CLUB_ANNOUNCEMENT_UNPIN = "/{announcementId}/unpin";

    // Clubs (voting module - for elections)
    public static final String CLUBS = API_V1 + "/clubs";
    public static final String CLUB_BY_ID = "/{clubId}";
    public static final String CLUB_BY_CODE = "/code/{clubCode}";
    public static final String CLUB_SEARCH = "/search";
    public static final String CLUB_FACULTY = "/faculty/{faculty}";
    public static final String CLUB_OPEN_REGISTRATION = "/open-registration";
    public static final String CLUB_JOIN = "/join";
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
    public static final String CANDIDATE_UPDATE = "/candidates/{candidateId}";
    public static final String CANDIDATE_DELETE = "/candidates/{candidateId}";
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
    public static final String VOTING_ADMIN_CLUB_STATISTICS = VOTING_ADMIN_CLUBS + "/{clubId}/statistics";

    public static final String VOTING_SUPER_ADMIN_PROCESS_STATUS = VOTING_ADMIN + "/elections/process-status-updates";
    public static final String VOTING_SUPER_ADMIN_AUDIT_LOG = VOTING_ADMIN + "/audit-log";

    public static final String VOTING_SUPER_ADMIN_BULK_APPROVE = VOTING_ADMIN + "/memberships/bulk-approve";
    public static final String VOTING_SUPER_ADMIN_BULK_CANCEL = VOTING_ADMIN + "/elections/bulk-cancel";

    public static final String VOTING_SUPER_ADMIN_CLUB_EXPORT = VOTING_ADMIN + "/clubs/{clubId}/export";
    public static final String VOTING_SUPER_ADMIN_ELECTION_EXPORT = VOTING_ADMIN + "/elections/{electionId}/export";

    public static final String VOTING_SUPER_ADMIN_PERMANENT_DELETE_CLUB = VOTING_ADMIN + "/clubs/{clubId}/permanent";
    public static final String VOTING_SUPER_ADMIN_RESET_VOTES = VOTING_ADMIN + "/elections/{electionId}/reset-votes";
    public static final String VOTING_SUPER_ADMIN_INVALIDATE_VOTES = VOTING_ADMIN
            + "/voters/{voterId}/invalidate-votes";
    public static final String VOTING_SUPER_ADMIN_CONFIG = VOTING_ADMIN + "/config";

    // ==================== Election Admin Endpoints ====================
    public static final String ELECTION_ADMIN = API_V1 + "/admin/elections";
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
    public static final String ELECTION_ADMIN_CANDIDATES_UPDATE = "/{electionId}/candidates/{candidateId}";
    public static final String ELECTION_ADMIN_LIVE_VOTES = "/{electionId}/live-votes";
    public static final String ELECTION_ADMIN_STATISTICS_PATH = "/statistics";
    public static final String ELECTION_ADMIN_STATISTICS_CLUBS = "/statistics/clubs";
    public static final String ELECTION_ADMIN_STATISTICS_ELECTIONS = "/statistics/elections";
    public static final String ELECTION_ADMIN_STATISTICS_SUMMARY = "/statistics/summary";
    public static final String ELECTION_ADMIN_PROCESS_STATUS_PATH = "/process-status-updates";
    public static final String ELECTION_ADMIN_RESET_VOTES_PATH = "/{electionId}/reset-votes";

    // ==================== Meeting Endpoints (Student-Lecturer) ====================

    // Base meeting paths
    public static final String MEETINGS = API_V1 + "/meetings";
    public static final String MEETING_ADMIN = "/api/v1/admin/meetings";

    // Common endpoints
    public static final String MEETING_BY_ID = "/{meetingId}";
    public static final String MEETING_START = "/{meetingId}/start";
    public static final String MEETING_COMPLETE = "/{meetingId}/complete";

    // Student endpoints (requesting meetings)
    public static final String MEETING_MY_REQUESTS = "/my/requests";
    public static final String MEETING_MY_UPCOMING = "/my/upcoming";
    public static final String MEETING_MY_SEARCH = "/my/search";
    public static final String MEETING_CANCEL_BY_STUDENT = "/{meetingId}/cancel";

    // Lecturer endpoints (managing meeting requests)
    public static final String MEETING_LECTURER = "/lecturer";
    public static final String MEETING_PENDING = "/lecturer/pending";
    public static final String MEETING_PENDING_COUNT = "/lecturer/pending/count";
    public static final String MEETING_LECTURER_ALL = "/lecturer/all";
    public static final String MEETING_LECTURER_UPCOMING = "/lecturer/upcoming";
    public static final String MEETING_LECTURER_CALENDAR = "/lecturer/calendar";
    public static final String MEETING_LECTURER_SEARCH = "/lecturer/search";
    public static final String MEETING_LECTURER_STATISTICS = "/lecturer/statistics";
    public static final String MEETING_ACCEPT = "/{meetingId}/accept";
    public static final String MEETING_REJECT = "/{meetingId}/reject";
    public static final String MEETING_RESCHEDULE = "/{meetingId}/reschedule";
    public static final String MEETING_CANCEL_BY_LECTURER = "/{meetingId}/lecturer-cancel";
    public static final String MEETING_ADD_NOTES = "/{meetingId}/notes";

    // Admin meeting endpoints
    public static final String MEETING_ADMIN_BY_ID = "/{meetingId}";
    public static final String MEETING_ADMIN_CANCEL = "/{meetingId}/force-cancel";
    public static final String MEETING_ADMIN_PERMANENT = "/{meetingId}/permanent";
    public static final String MEETING_ADMIN_STATISTICS = "/statistics";
    public static final String MEETING_ADMIN_LECTURER_STATISTICS = "/statistics/lecturer/{lecturerId}";
    // java
    public static final String BOARDINGHOUSE_PUBLIC = API_V1 + "/boardinghouse";
    public static final String BOARDINGHOUSE_ADMIN = API_V1 + "/admin/boardinghouse";

    public static final String BOARDINGHOUSE_HOUSES = "/houses";
    public static final String BOARDINGHOUSE_HOUSES_BY_ID = "/houses/{houseId}";
    public static final String BOARDINGHOUSE_HOUSES_PERMANENT = "/houses/{houseId}/permanent";


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
    // ==================== Kuppi Application Endpoints ====================
    public static final String KUPPI_APPLICATIONS = API_V1 + "/kuppi/applications";
    public static final String KUPPI_APPLICATION_MY = "/my";
    public static final String KUPPI_APPLICATION_ACTIVE = "/active";
    public static final String KUPPI_APPLICATION_BY_ID = "/{applicationId}";
    public static final String KUPPI_APPLICATION_CAN_APPLY = "/can-apply";
    public static final String KUPPI_APPLICATION_IS_KUPPI_STUDENT = "/is-kuppi-student";

    // ==================== Club Dashboard Endpoints ====================
    public static final String CLUB_DASHBOARD = API_V1 + "/club-dashboard";
    public static final String CLUB_DASHBOARD_STUDENT = "/student";
    public static final String CLUB_DASHBOARD_CLUB_MEMBER = "/club-member";
    public static final String CLUB_DASHBOARD_STAFF = "/staff";
    public static final String CLUB_DASHBOARD_ADMIN = "/admin";
    public static final String CLUB_DASHBOARD_ACADEMIC = "/academic";

    // Club Election endpoints
    public static final String CLUB_ELECTIONS = "/{clubId}/elections";
    public static final String CLUB_ELECTIONS_ACTIVE = "/{clubId}/elections/active";
    public static final String CLUB_ELECTIONS_UPCOMING = "/{clubId}/elections/upcoming";

    // ==================== Intranet Endpoints ====================
    public static final String BY_SLUG = "/{slug}";
    public static final String FOUNDATION_PROGRAM = API_V1 + "/foundation-program";
    public static final String STAFF = API_V1 + "/staff";
    public static final String MITIGATION_FORMS = API_V1 + "/mitigation-forms";
    public static final String STUDENTS_RELATIONS_UNIT = API_V1 + "/students-relations-unit";
    public static final String INFO = API_V1 + "/info";
    public static final String STUDENT_COMPLAINTS = API_V1 + "/student-complaints";
    public static final String ACADEMIC_CALENDARS = API_V1 + "/academic-calendars";
    public static final String POSTGRADUATE = API_V1 + "/postgraduate";
    public static final String SCHEDULES = API_V1 + "/schedules";
    public static final String STUDENT_POLICIES = API_V1 + "/student-policies";
    public static final String UNDERGRADUATE = API_V1 + "/undergraduate";

    // ==================== Push Notification Endpoints ====================
    public static final String PUSH = API_V1 + "/push";
    public static final String NOTIFICATIONS = API_V1 + "/notifications";

}


