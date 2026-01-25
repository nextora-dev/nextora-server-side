package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum ClubPositionsType {
    PRESIDENT("president"),
    VICE_PRESIDENT("vice_president"),
    SECRETARY("secretary"),
    TREASURER("treasurer"),
    Top_Board_MEMBER("top_board_member"),
    COMMITTEE_MEMBER("committee_member"),
    GENERAL_MEMBER("general_member");

    private final String displayName;

    ClubPositionsType(String displayName) {
        this.displayName = displayName;
    }
}
