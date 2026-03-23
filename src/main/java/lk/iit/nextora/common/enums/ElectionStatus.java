package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the status of an election in the voting module
 */
@Getter
@RequiredArgsConstructor
public enum ElectionStatus {

    DRAFT("Draft", "Election is being set up"),
    NOMINATION_OPEN("Nomination Open", "Accepting candidate nominations"),
    NOMINATION_CLOSED("Nomination Closed", "Nominations are closed, awaiting voting"),
    VOTING_OPEN("Voting Open", "Voting is in progress"),
    VOTING_CLOSED("Voting Closed", "Voting has ended, results pending"),
    RESULTS_PUBLISHED("Results Published", "Election results are available"),
    CANCELLED("Cancelled", "Election has been cancelled"),
    ARCHIVED("Archived", "Election is archived");

    private final String displayName;
    private final String description;

    /**
     * Check if nominations can be submitted
     */
    public boolean canNominate() {
        return this == NOMINATION_OPEN;
    }

    /**
     * Check if voting is allowed
     */
    public boolean canVote() {
        return this == VOTING_OPEN;
    }

    /**
     * Check if results can be viewed
     */
    public boolean canViewResults() {
        return this == RESULTS_PUBLISHED || this == ARCHIVED;
    }

    /**
     * Check if election can be modified
     */
    public boolean canModify() {
        return this == DRAFT || this == NOMINATION_OPEN;
    }
}
