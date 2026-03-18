package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of an event
 */
@Getter
@RequiredArgsConstructor
public enum EventStatus {

    DRAFT("Draft", "Event is created but not visible to users"),
    PUBLISHED("Published", "Event is visible to users"),
    CANCELLED("Cancelled", "Event has been cancelled"),
    COMPLETED("Completed", "Event has ended");


    private final String displayName;
    private final String description;

    /**
     * Check if event can be edited
     */
    public boolean canEdit() {
        return this == DRAFT;
    }

    /**
     * Check if event status can be changed to Published
     */
    public boolean canPublish() {
        return this == DRAFT;
    }

    public boolean canCancel() {
        return this == DRAFT || this == PUBLISHED;
    }

    public boolean isVisibleToUsers() {
        return this == PUBLISHED || this == COMPLETED;
    }
}
