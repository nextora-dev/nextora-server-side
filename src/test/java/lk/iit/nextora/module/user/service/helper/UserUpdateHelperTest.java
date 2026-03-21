package lk.iit.nextora.module.user.service.helper;

import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Admin;
import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserUpdateHelper Unit Tests")
class UserUpdateHelperTest {

    private UserUpdateHelper helper;

    @BeforeEach
    void setUp() {
        helper = new UserUpdateHelper();
    }

    // ============================================================
    // COMMON FIELDS
    // ============================================================

    @Nested
    @DisplayName("updateCommonFields")
    class UpdateCommonFieldsTests {

        @Test
        @DisplayName("Should update firstName when provided")
        void updateCommonFields_firstName_updates() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().firstName("  Jane  ").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should update lastName when provided")
        void updateCommonFields_lastName_updates() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().lastName("Smith").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should update phone when provided (even empty string)")
        void updateCommonFields_phone_updates() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().phone("+94771234567").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getPhoneNumber()).isEqualTo("+94771234567");
        }

        @Test
        @DisplayName("Should not change fields when request values are null")
        void updateCommonFields_nullValues_noChange() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            helper.updateCommonFields(user, request);

            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should not update firstName when blank")
        void updateCommonFields_blankFirstName_noChange() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().firstName("   ").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getFirstName()).isEqualTo("John");
        }
    }

    // ============================================================
    // STUDENT FIELDS
    // ============================================================

    @Nested
    @DisplayName("updateStudentFields")
    class UpdateStudentFieldsTests {

        @Test
        @DisplayName("Should update student common fields")
        void updateStudentFields_commonFields_updates() {
            Student student = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .address("123 Main St")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .guardianName("Parent Name")
                    .guardianPhone("+94777654321")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getAddress()).isEqualTo("123 Main St");
            assertThat(student.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 15));
            assertThat(student.getGuardianName()).isEqualTo("Parent Name");
            assertThat(student.getGuardianPhone()).isEqualTo("+94777654321");
        }

        @Test
        @DisplayName("Should update club member fields for CLUB_MEMBER role")
        void updateStudentFields_clubMember_updatesClubFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.CLUB_MEMBER));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .clubName("IEEE")
                    .clubJoinDate(LocalDate.of(2024, 3, 1))
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getClubName()).isEqualTo("IEEE");
            assertThat(student.getClubJoinDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        }

        @Test
        @DisplayName("Should update batch rep fields for BATCH_REP role")
        void updateStudentFields_batchRep_updatesBatchFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.BATCH_REP));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .batchRepYear("2024")
                    .batchRepSemester("Semester 1")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getBatchRepYear()).isEqualTo("2024");
            assertThat(student.getBatchRepSemester()).isEqualTo("Semester 1");
        }

        @Test
        @DisplayName("Should update kuppi fields for student with kuppi capability")
        void updateStudentFields_kuppiStudent_updatesKuppiFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.KUPPI_STUDENT));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .kuppiSubjects(Set.of("Math", "Physics"))
                    .kuppiExperienceLevel("Advanced")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getKuppiSubjects()).containsExactlyInAnyOrder("Math", "Physics");
            assertThat(student.getKuppiExperienceLevel()).isEqualTo("Advanced");
        }

        @Test
        @DisplayName("Should not update club fields for non-CLUB_MEMBER student")
        void updateStudentFields_normalStudent_ignoresClubFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL));
            student.setClubName(null);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .clubName("IEEE")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getClubName()).isNull();
        }
    }

    // ============================================================
    // ACADEMIC STAFF FIELDS
    // ============================================================

    @Nested
    @DisplayName("updateAcademicStaffFields")
    class UpdateAcademicStaffFieldsTests {

        @Test
        @DisplayName("Should update all academic staff fields")
        void updateAcademicStaffFields_allFields_updates() {
            AcademicStaff staff = new AcademicStaff();
            staff.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .officeLocation("Room 301")
                    .responsibilities("Teaching")
                    .specialization("AI/ML")
                    .bio("PhD in CS")
                    .availableForMeetings(true)
                    .designation("Senior Lecturer")
                    .build();

            helper.updateAcademicStaffFields(staff, request);

            assertThat(staff.getOfficeLocation()).isEqualTo("Room 301");
            assertThat(staff.getResponsibilities()).isEqualTo("Teaching");
            assertThat(staff.getSpecialization()).isEqualTo("AI/ML");
            assertThat(staff.getBio()).isEqualTo("PhD in CS");
            assertThat(staff.getAvailableForMeetings()).isTrue();
            assertThat(staff.getDesignation()).isEqualTo("Senior Lecturer");
        }

        @Test
        @DisplayName("Should not change null fields")
        void updateAcademicStaffFields_nullValues_noChange() {
            AcademicStaff staff = new AcademicStaff();
            staff.setOfficeLocation("Original");
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            helper.updateAcademicStaffFields(staff, request);

            assertThat(staff.getOfficeLocation()).isEqualTo("Original");
        }
    }

    // ============================================================
    // NON-ACADEMIC STAFF FIELDS
    // ============================================================

    @Nested
    @DisplayName("updateNonAcademicStaffFields")
    class UpdateNonAcademicStaffFieldsTests {

        @Test
        @DisplayName("Should update workLocation and shift")
        void updateNonAcademicStaffFields_allFields_updates() {
            NonAcademicStaff staff = new NonAcademicStaff();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .workLocation("Building A")
                    .shift("Morning")
                    .build();

            helper.updateNonAcademicStaffFields(staff, request);

            assertThat(staff.getWorkLocation()).isEqualTo("Building A");
            assertThat(staff.getShift()).isEqualTo("Morning");
        }
    }

    // ============================================================
    // ROLE-SPECIFIC ROUTING
    // ============================================================

    @Nested
    @DisplayName("updateRoleSpecificFields - routing")
    class RoleSpecificRoutingTests {

        @Test
        @DisplayName("Should route Student to updateStudentFields")
        void updateRoleSpecificFields_student_routesToStudentFields() {
            Student student = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .address("New Address")
                    .build();

            helper.updateRoleSpecificFields(student, request);

            assertThat(student.getAddress()).isEqualTo("New Address");
        }

        @Test
        @DisplayName("Should route AcademicStaff to updateAcademicStaffFields")
        void updateRoleSpecificFields_academicStaff_routesToStaffFields() {
            AcademicStaff staff = new AcademicStaff();
            staff.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .officeLocation("Room 500")
                    .build();

            helper.updateRoleSpecificFields(staff, request);

            assertThat(staff.getOfficeLocation()).isEqualTo("Room 500");
        }

        @Test
        @DisplayName("Should handle Admin user without errors (no role-specific fields)")
        void updateRoleSpecificFields_admin_noError() {
            Admin admin = new Admin();
            admin.setRole(UserRole.ROLE_ADMIN);
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            // Should not throw
            helper.updateRoleSpecificFields(admin, request);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Student createStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setEmail("student@iit.ac.lk");
        student.setPassword("encoded");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setRole(UserRole.ROLE_STUDENT);
        student.setStatus(UserStatus.ACTIVE);
        student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL));
        return student;
    }
}