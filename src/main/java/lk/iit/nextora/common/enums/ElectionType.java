package lk.iit.nextora.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing types of elections in a club
 */
@Getter
@RequiredArgsConstructor
public enum ElectionType {

    PRESIDENT("President Election", "Election for club president position"),
    VICE_PRESIDENT("Vice President Election", "Election for vice president position"),
    SECRETARY("Secretary Election", "Election for secretary position"),
    TREASURER("Treasurer Election", "Election for treasurer position"),
    GENERAL("General Election", "General purpose election"),
    POLL("Poll", "Simple poll/survey for club decisions"),
    REFERENDUM("Referendum", "Club-wide referendum on specific matters");

    private final String displayName;
    private final String description;

    /**
     * Check if this election type requires manifesto
     */
    public boolean requiresManifesto() {
        return this == PRESIDENT || this == VICE_PRESIDENT ||
               this == SECRETARY || this == TREASURER;
    }

    /**
     * Check if this is a leadership position
     */
    public boolean isLeadershipPosition() {
        return this == PRESIDENT || this == VICE_PRESIDENT ||
               this == SECRETARY || this == TREASURER;
    }
}
