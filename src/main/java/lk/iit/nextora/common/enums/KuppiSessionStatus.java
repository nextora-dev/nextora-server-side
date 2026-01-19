package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing the status/lifecycle of a Kuppi session
 */
@Getter
public enum KuppiSessionStatus {
    DRAFT("Draft"),
    SCHEDULED("Scheduled"),
    LIVE("Live"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    KuppiSessionStatus(String displayName) {
        this.displayName = displayName;
    }

}
