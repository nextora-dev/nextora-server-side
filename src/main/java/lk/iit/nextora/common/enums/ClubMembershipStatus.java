package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing membership status in a club
 */
@Getter
@RequiredArgsConstructor
public enum ClubMembershipStatus {

    PENDING("Pending", "Membership application is pending"),
    ACTIVE("Active", "Member is active in the club"),
    INACTIVE("Inactive", "Member is inactive"),
    SUSPENDED("Suspended", "Membership is suspended"),
    EXPIRED("Expired", "Membership has expired"),
    REJECTED("Rejected", "Membership application was rejected"),
    REVOKED("Revoked", "Membership has been revoked");

    private final String displayName;
    private final String description;

    /**
     * Check if member can vote
     */
    public boolean canVote() {
        return this == ACTIVE;
    }

    /**
     * Check if member can nominate
     */
    public boolean canNominate() {
        return this == ACTIVE;
    }
}
