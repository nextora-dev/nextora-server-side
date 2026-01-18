package lk.iit.nextora.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum UserRole {
    // ==================== SUPER_ADMIN - Complete system access ====================
    ROLE_SUPER_ADMIN("Super_Admin", "Unrestricted access to all system functions", true,
            getAllPermissions()),

    // ==================== ADMIN - Administrative access ====================
    ROLE_ADMIN("Admin", "Admin access to all system functions", true,
            getAdminPermissions()),

    // ==================== STUDENT - Student access (base permissions, sub-roles add more) ====================
    ROLE_STUDENT("Student", "Student access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_JOIN,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_READ,
                    Permission.EVENT_REGISTER,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.LOST_FOUND_CREATE,
                    Permission.LOST_FOUND_READ
            )),

    // ==================== LECTURER - Lecturer access ====================
    ROLE_LECTURER("Lecturer", "Lecturer access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_APPROVE,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_READ
            )),

    // ==================== NON_ACADEMIC_STAFF ====================
    ROLE_NON_ACADEMIC_STAFF("Non_Academic_Staff", "Non-academic staff access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.EVENT_READ,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.LOST_FOUND_READ,
                    Permission.LOST_FOUND_UPDATE
            )),

    // ==================== ACADEMIC_STAFF ====================
    ROLE_ACADEMIC_STAFF("Academic_Staff", "Academic staff access to system functions", true,
            Set.of(
                    Permission.USER_READ,
                    Permission.USER_UPDATE,
                    Permission.KUPPI_READ,
                    Permission.KUPPI_APPROVE,
                    Permission.CLUB_READ,
                    Permission.BATCH_READ,
                    Permission.BATCH_UPDATE,
                    Permission.EVENT_CREATE,
                    Permission.EVENT_READ,
                    Permission.EVENT_UPDATE,
                    Permission.COMMUNICATION_READ,
                    Permission.COMMUNICATION_SEND,
                    Permission.COMMUNICATION_BROADCAST,
                    Permission.LOST_FOUND_READ
            ));

    private final String displayName;
    private final String description;
    private final boolean systemRole;
    private final Set<Permission> permissions;

    UserRole(String displayName, String description, boolean systemRole, Set<Permission> permissions) {
        this.displayName = displayName;
        this.description = description;
        this.systemRole = systemRole;
        this.permissions = permissions;
    }

    // ==================== HELPER METHODS FOR PERMISSION SETS ====================

    private static Set<Permission> getAllPermissions() {
        return new HashSet<>(Arrays.asList(Permission.values()));
    }

    private static Set<Permission> getAdminPermissions() {
        return new HashSet<>(Arrays.asList(Permission.values()));
    }
}
