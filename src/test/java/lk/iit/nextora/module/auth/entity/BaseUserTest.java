package lk.iit.nextora.module.auth.entity;

import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseUser & Student Entity Unit Tests")
class BaseUserTest {

    private Student student;
    private Admin admin;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setEmail("student@iit.ac.lk");
        student.setPassword("encoded");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setRole(UserRole.ROLE_STUDENT);
        student.setStatus(UserStatus.ACTIVE);
        student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL));

        admin = new Admin();
        admin.setId(2L);
        admin.setEmail("admin@iit.ac.lk");
        admin.setPassword("encoded");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
    }

    // ============================================================
    // UserDetails CONTRACT TESTS
    // ============================================================

    @Nested
    @DisplayName("UserDetails Contract")
    class UserDetailsContractTests {

        @Test
        @DisplayName("getUsername should return email")
        void getUsername_returnsEmail() {
            assertThat(student.getUsername()).isEqualTo("student@iit.ac.lk");
        }

        @Test
        @DisplayName("isEnabled should return true for ACTIVE status")
        void isEnabled_activeStatus_returnsTrue() {
            assertThat(student.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEnabled should return true for PASSWORD_CHANGE_REQUIRED status")
        void isEnabled_passwordChangeRequired_returnsTrue() {
            student.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
            assertThat(student.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEnabled should return false for SUSPENDED status")
        void isEnabled_suspendedStatus_returnsFalse() {
            student.setStatus(UserStatus.SUSPENDED);
            assertThat(student.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("isEnabled should return false for DELETED status")
        void isEnabled_deletedStatus_returnsFalse() {
            student.setStatus(UserStatus.DELETED);
            assertThat(student.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("isEnabled should return false for DEACTIVATED status")
        void isEnabled_deactivatedStatus_returnsFalse() {
            student.setStatus(UserStatus.DEACTIVATED);
            assertThat(student.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("isAccountNonLocked should return false for SUSPENDED")
        void isAccountNonLocked_suspended_returnsFalse() {
            student.setStatus(UserStatus.SUSPENDED);
            assertThat(student.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("isAccountNonLocked should return true for ACTIVE")
        void isAccountNonLocked_active_returnsTrue() {
            assertThat(student.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("isAccountNonExpired should always return true")
        void isAccountNonExpired_always_returnsTrue() {
            assertThat(student.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("isCredentialsNonExpired should always return true")
        void isCredentialsNonExpired_always_returnsTrue() {
            assertThat(student.isCredentialsNonExpired()).isTrue();
        }
    }

    // ============================================================
    // AUTHORITIES / PERMISSION TESTS
    // ============================================================

    @Nested
    @DisplayName("Authorities & Permissions")
    class AuthoritiesTests {

        @Test
        @DisplayName("Should include role as authority")
        void getAuthorities_includesRole() {
            Collection<? extends GrantedAuthority> authorities = student.getAuthorities();
            Set<String> authorityStrings = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            assertThat(authorityStrings).contains("ROLE_STUDENT");
        }

        @Test
        @DisplayName("Student should have role permissions as authorities")
        void getAuthorities_studentRole_includesPermissions() {
            Collection<? extends GrantedAuthority> authorities = student.getAuthorities();
            Set<String> authorityStrings = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            assertThat(authorityStrings).contains("USER:READ", "USER:UPDATE");
        }

        @Test
        @DisplayName("Admin should have more permissions than student")
        void getAuthorities_admin_hasMoreThanStudent() {
            assertThat(admin.getAuthorities().size()).isGreaterThan(student.getAuthorities().size());
        }

        @Test
        @DisplayName("hasPermission by Permission enum should work correctly")
        void hasPermission_existingPermission_returnsTrue() {
            assertThat(student.hasPermission(Permission.USER_READ)).isTrue();
        }

        @Test
        @DisplayName("hasPermission by string should work correctly")
        void hasPermission_byString_returnsTrue() {
            assertThat(student.hasPermission("USER:READ")).isTrue();
        }

        @Test
        @DisplayName("hasPermission should return false for missing permission")
        void hasPermission_missingPermission_returnsFalse() {
            assertThat(student.hasPermission(Permission.USER_CREATE)).isFalse();
        }
    }

    // ============================================================
    // STUDENT SUB-ROLE TESTS
    // ============================================================

    @Nested
    @DisplayName("Student Sub-Roles")
    class StudentSubRoleTests {

        @Test
        @DisplayName("Student with CLUB_MEMBER role should get additional permissions")
        void getAuthorities_clubMemberRole_includesClubPermissions() {
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.CLUB_MEMBER));
            Collection<? extends GrantedAuthority> authorities = student.getAuthorities();
            // Club member gets additional permissions beyond NORMAL
            assertThat(authorities.size()).isGreaterThan(
                    new Student() {{
                        setRole(UserRole.ROLE_STUDENT);
                        setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL));
                    }}.getAuthorities().size()
            );
        }

        @Test
        @DisplayName("hasRoleType should return true for assigned role")
        void hasRoleType_assignedRole_returnsTrue() {
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.BATCH_REP));
            assertThat(student.hasRoleType(StudentRoleType.BATCH_REP)).isTrue();
        }

        @Test
        @DisplayName("hasRoleType should return false for unassigned role")
        void hasRoleType_unassignedRole_returnsFalse() {
            assertThat(student.hasRoleType(StudentRoleType.BATCH_REP)).isFalse();
        }

        @Test
        @DisplayName("addRoleType should add new role type")
        void addRoleType_newRole_addsSuccessfully() {
            student.addRoleType(StudentRoleType.CLUB_MEMBER);
            assertThat(student.getStudentRoleTypes()).contains(StudentRoleType.CLUB_MEMBER, StudentRoleType.NORMAL);
        }

        @Test
        @DisplayName("removeRoleType should not allow removing NORMAL role")
        void removeRoleType_normalRole_doesNotRemove() {
            student.removeRoleType(StudentRoleType.NORMAL);
            assertThat(student.getStudentRoleTypes()).contains(StudentRoleType.NORMAL);
        }

        @Test
        @DisplayName("removeRoleType should remove non-NORMAL role")
        void removeRoleType_nonNormalRole_removes() {
            student.addRoleType(StudentRoleType.BATCH_REP);
            student.removeRoleType(StudentRoleType.BATCH_REP);
            assertThat(student.getStudentRoleTypes()).doesNotContain(StudentRoleType.BATCH_REP);
        }

        @Test
        @DisplayName("getPrimaryRoleType should return highest priority role")
        void getPrimaryRoleType_multipleRoles_returnsHighestPriority() {
            student.setStudentRoleTypes(EnumSet.of(
                    StudentRoleType.NORMAL,
                    StudentRoleType.CLUB_MEMBER,
                    StudentRoleType.BATCH_REP
            ));
            assertThat(student.getPrimaryRoleType()).isEqualTo(StudentRoleType.BATCH_REP);
        }

        @Test
        @DisplayName("getPrimaryRoleType should return NORMAL when only NORMAL")
        void getPrimaryRoleType_onlyNormal_returnsNormal() {
            assertThat(student.getPrimaryRoleType()).isEqualTo(StudentRoleType.NORMAL);
        }
    }

    // ============================================================
    // HELPER METHOD TESTS
    // ============================================================

    @Nested
    @DisplayName("Helper Methods")
    class HelperMethodTests {

        @Test
        @DisplayName("getFullName should concatenate first and last name")
        void getFullName_returnsFullName() {
            assertThat(student.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("isActive should return true for ACTIVE status")
        void isActive_activeStatus_returnsTrue() {
            assertThat(student.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive should return false for non-ACTIVE status")
        void isActive_nonActiveStatus_returnsFalse() {
            student.setStatus(UserStatus.SUSPENDED);
            assertThat(student.isActive()).isFalse();
        }

        @Test
        @DisplayName("getUserType should return STUDENT for Student entity")
        void getUserType_student_returnsStudent() {
            assertThat(student.getUserType()).isEqualTo("STUDENT");
        }
    }
}
