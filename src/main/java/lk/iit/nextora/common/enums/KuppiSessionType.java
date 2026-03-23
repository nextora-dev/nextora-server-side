package lk.iit.nextora.common.enums;

import lombok.Getter;

/**
 * Enum representing the type/format of Kuppi session
 */
@Getter
public enum KuppiSessionType {
    LIVE("Live Session"),
    RECORDED("Recorded Session");

    private final String displayName;

    KuppiSessionType(String displayName) {
        this.displayName = displayName;
    }

}

