package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Active"),
    DEACTIVATE("Deactivate"),
    SUSPENDED("Suspended"),
    DELETED("Deleted"),
    PASSWORD_CHANGE_REQUIRED("Password_Change_Required");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
