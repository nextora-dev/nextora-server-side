package lk.iit.nextora.common.constants;

public class AppConstants {
    private AppConstants() {
        throw new IllegalStateException("Constants class - cannot be instantiated");
    }

    // Pagination defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
}
