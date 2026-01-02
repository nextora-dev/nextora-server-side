package lk.iit.nextora.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_STUDENT("Student"),
    ROLE_LECTURER("Lecturer"),
    ROLE_ACADEMIC_STAFF("Academic_Staff"),
    ROLE_NON_ACADEMIC_STAFF("Non_Academic_Staff"),
    ROLE_ADMIN("Admin"),
    ROLE_SUPER_ADMIN("Super_Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }
}
