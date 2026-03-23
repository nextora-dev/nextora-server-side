package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Status for Kuppi Student application requests.
 * Students apply to become Kuppi Students, and Admin/Academic Staff approve or reject.
 */
@Getter
@RequiredArgsConstructor
public enum KuppiApplicationStatus {
    PENDING("Pending", "Application is awaiting review"),
    UNDER_REVIEW("Under Review", "Application is being reviewed by staff"),
    APPROVED("Approved", "Application has been approved"),
    REJECTED("Rejected", "Application has been rejected"),
    CANCELLED("Cancelled", "Application was cancelled by the student"),
    EXPIRED("Expired", "Application expired without action");

    private final String displayName;
    private final String description;

    /**
     * Check if the application is in a final state (cannot be changed)
     */
    public boolean isFinalState() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }

    /**
     * Check if the application can be approved
     */
    public boolean canBeApproved() {
        return this == PENDING || this == UNDER_REVIEW;
    }

    /**
     * Check if the application can be rejected
     */
    public boolean canBeRejected() {
        return this == PENDING || this == UNDER_REVIEW;
    }

    /**
     * Check if the application can be cancelled by student
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == UNDER_REVIEW;
    }
}

