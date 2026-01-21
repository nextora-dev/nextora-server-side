package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum FacultyType {
    COMPUTING("Computing"),
    BUSINESS("Business");

    private final String displayName;

    FacultyType(String displayName) {
        this.displayName = displayName;
    }

}
