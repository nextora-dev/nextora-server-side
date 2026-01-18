package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Active"),
    Deactivate("Deactivate"),
    SUSPENDED("Suspended"),
    DELETED("Deleted"),
    PENDING_VERIFICATION("Pending_Verification");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
