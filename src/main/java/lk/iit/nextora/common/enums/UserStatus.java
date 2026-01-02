package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    DELETED("Deleted"),
    PENDING_VERIFICATION("Pending_Verification");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
