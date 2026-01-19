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

    // Kuppi Admin endpoints
    public static final String KUPPI_ADMIN = API_V1 + "/admin/kuppi";
    public static final String KUPPI_ADMIN_SESSIONS = KUPPI_ADMIN + "/sessions";
    public static final String KUPPI_ADMIN_NOTES = KUPPI_ADMIN + "/notes";
}
