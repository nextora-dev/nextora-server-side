package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing gender preference for boarding house rooms
 */
@Getter
public enum GenderPreference {
    MALE("Male Only"),
    FEMALE("Female Only"),
    ANY("Any Gender");

    private final String displayName;

    GenderPreference(String displayName) {
        this.displayName = displayName;
    }
}
