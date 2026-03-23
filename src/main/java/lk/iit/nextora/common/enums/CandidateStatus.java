package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of a candidate in an election
 */
@Getter
@RequiredArgsConstructor
public enum CandidateStatus {

    PENDING("Pending", "Nomination is pending review"),
    APPROVED("Approved", "Nomination has been approved"),
    REJECTED("Rejected", "Nomination has been rejected"),
    WITHDRAWN("Withdrawn", "Candidate has withdrawn from election"),
    DISQUALIFIED("Disqualified", "Candidate has been disqualified");

    private final String displayName;
    private final String description;

    /**
     * Check if candidate can receive votes
     */
    public boolean canReceiveVotes() {
        return this == APPROVED;
    }

    /**
     * Check if candidate status can be modified
     */
    public boolean canModify() {
        return this == PENDING;
    }
}
