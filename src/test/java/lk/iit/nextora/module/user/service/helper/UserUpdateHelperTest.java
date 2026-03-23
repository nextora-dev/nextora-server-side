package lk.iit.nextora.module.user.service.helper;

import lk.iit.nextora.common.enums.ClubPositionsType;
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
        @DisplayName("Should update firstName when provided and trim whitespace")
        void updateCommonFields_firstName_trims() {
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
        @DisplayName("Should update phone when provided")
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

        @Test
        @DisplayName("Should not update lastName when blank")
        void updateCommonFields_blankLastName_noChange() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder().lastName("  ").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should update all common fields at once")
        void updateCommonFields_allFields_updatesAll() {
            Student user = createStudent();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phone("+94771111111")
                    .build();

            helper.updateCommonFields(user, request);

            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getPhoneNumber()).isEqualTo("+94771111111");
        }

        @Test
        @DisplayName("Should allow phone to be set to empty string (clearing phone)")
        void updateCommonFields_emptyPhone_setsEmpty() {
            Student user = createStudent();
            user.setPhoneNumber("+94771234567");
            UpdateProfileRequest request = UpdateProfileRequest.builder().phone("").build();

            helper.updateCommonFields(user, request);

            assertThat(user.getPhoneNumber()).isEmpty();
        }
    }

    // ============================================================
    // STUDENT FIELDS
    // ============================================================

    @Nested
    @DisplayName("updateStudentFields")
    class UpdateStudentFieldsTests {

        @Test
        @DisplayName("Should update student common fields (address, DOB, guardian)")
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
        @DisplayName("Should not update student fields when null")
        void updateStudentFields_nullValues_noChange() {
            Student student = createStudent();
            student.setAddress("Original Address");
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            helper.updateStudentFields(student, request);

            assertThat(student.getAddress()).isEqualTo("Original Address");
        }

        @Test
        @DisplayName("Should update club member fields for CLUB_MEMBER role")
        void updateStudentFields_clubMember_updatesClubFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.CLUB_MEMBER));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .clubName("IEEE")
                    .clubJoinDate(LocalDate.of(2024, 3, 1))
                    .clubMembershipId("IEEE-001")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getClubName()).isEqualTo("IEEE");
            assertThat(student.getClubJoinDate()).isEqualTo(LocalDate.of(2024, 3, 1));
            assertThat(student.getClubMembershipId()).isEqualTo("IEEE-001");
        }

        @Test
        @DisplayName("Should update batch rep fields for BATCH_REP role")
        void updateStudentFields_batchRep_updatesBatchFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.BATCH_REP));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .batchRepYear("2024")
                    .batchRepSemester("Semester 1")
                    .batchRepElectedDate(LocalDate.of(2024, 1, 10))
                    .batchRepResponsibilities("Coordinate batch activities")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getBatchRepYear()).isEqualTo("2024");
            assertThat(student.getBatchRepSemester()).isEqualTo("Semester 1");
            assertThat(student.getBatchRepElectedDate()).isEqualTo(LocalDate.of(2024, 1, 10));
            assertThat(student.getBatchRepResponsibilities()).isEqualTo("Coordinate batch activities");
        }

        @Test
        @DisplayName("Should update kuppi fields for KUPPI_STUDENT role")
        void updateStudentFields_kuppiStudent_updatesKuppiFields() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.KUPPI_STUDENT));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .kuppiSubjects(Set.of("Math", "Physics"))
                    .kuppiExperienceLevel("Advanced")
                    .kuppiAvailability("Weekends")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getKuppiSubjects()).containsExactlyInAnyOrder("Math", "Physics");
            assertThat(student.getKuppiExperienceLevel()).isEqualTo("Advanced");
            assertThat(student.getKuppiAvailability()).isEqualTo("Weekends");
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

        @Test
        @DisplayName("Should not update kuppi subjects when empty set")
        void updateStudentFields_emptyKuppiSubjects_noChange() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(StudentRoleType.NORMAL, StudentRoleType.KUPPI_STUDENT));
            student.setKuppiSubjects(Set.of("Existing"));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .kuppiSubjects(Set.of())
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getKuppiSubjects()).containsExactly("Existing");
        }

        @Test
        @DisplayName("Should handle student with multiple roles updating all role fields")
        void updateStudentFields_multipleRoles_updatesAll() {
            Student student = createStudent();
            student.setStudentRoleTypes(EnumSet.of(
                    StudentRoleType.NORMAL,
                    StudentRoleType.CLUB_MEMBER,
                    StudentRoleType.BATCH_REP,
                    StudentRoleType.KUPPI_STUDENT
            ));
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .address("New Address")
                    .clubName("Rotaract")
                    .batchRepYear("2025")
                    .kuppiSubjects(Set.of("Chemistry"))
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getAddress()).isEqualTo("New Address");
            assertThat(student.getClubName()).isEqualTo("Rotaract");
            assertThat(student.getBatchRepYear()).isEqualTo("2025");
            assertThat(student.getKuppiSubjects()).containsExactly("Chemistry");
        }

        @Test
        @DisplayName("Should handle null studentRoleTypes gracefully")
        void updateStudentFields_nullRoleTypes_noError() {
            Student student = createStudent();
            student.setStudentRoleTypes(null);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .address("New Address")
                    .build();

            helper.updateStudentFields(student, request);

            assertThat(student.getAddress()).isEqualTo("New Address");
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
            staff.setSpecialization("Original Spec");
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            helper.updateAcademicStaffFields(staff, request);

            assertThat(staff.getOfficeLocation()).isEqualTo("Original");
            assertThat(staff.getSpecialization()).isEqualTo("Original Spec");
        }

        @Test
        @DisplayName("Should set availableForMeetings to false")
        void updateAcademicStaffFields_availableFalse_setsFalse() {
            AcademicStaff staff = new AcademicStaff();
            staff.setAvailableForMeetings(true);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .availableForMeetings(false).build();

            helper.updateAcademicStaffFields(staff, request);

            assertThat(staff.getAvailableForMeetings()).isFalse();
        }

        @Test
        @DisplayName("Should trim whitespace from all string fields")
        void updateAcademicStaffFields_trimsWhitespace() {
            AcademicStaff staff = new AcademicStaff();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .officeLocation("  Room 301  ")
                    .specialization("  AI/ML  ")
                    .bio("  PhD  ")
                    .designation("  Prof  ")
                    .responsibilities("  Teaching  ")
                    .build();

            helper.updateAcademicStaffFields(staff, request);

            assertThat(staff.getOfficeLocation()).isEqualTo("Room 301");
            assertThat(staff.getSpecialization()).isEqualTo("AI/ML");
            assertThat(staff.getBio()).isEqualTo("PhD");
            assertThat(staff.getDesignation()).isEqualTo("Prof");
            assertThat(staff.getResponsibilities()).isEqualTo("Teaching");
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

        @Test
        @DisplayName("Should not change fields when null")
        void updateNonAcademicStaffFields_nullValues_noChange() {
            NonAcademicStaff staff = new NonAcademicStaff();
            staff.setWorkLocation("Original");
            staff.setShift("Night");
            UpdateProfileRequest request = UpdateProfileRequest.builder().build();

            helper.updateNonAcademicStaffFields(staff, request);

            assertThat(staff.getWorkLocation()).isEqualTo("Original");
            assertThat(staff.getShift()).isEqualTo("Night");
        }

        @Test
        @DisplayName("Should trim whitespace from fields")
        void updateNonAcademicStaffFields_trimsWhitespace() {
            NonAcademicStaff staff = new NonAcademicStaff();
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .workLocation("  Building B  ")
                    .shift("  Evening  ")
                    .build();

            helper.updateNonAcademicStaffFields(staff, request);

            assertThat(staff.getWorkLocation()).isEqualTo("Building B");
            assertThat(staff.getShift()).isEqualTo("Evening");
        }

        @Test
        @DisplayName("Should update only workLocation when shift is null")
        void updateNonAcademicStaffFields_onlyWorkLocation_updates() {
            NonAcademicStaff staff = new NonAcademicStaff();
            staff.setShift("Night");
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .workLocation("New Location")
                    .build();

            helper.updateNonAcademicStaffFields(staff, request);

            assertThat(staff.getWorkLocation()).isEqualTo("New Location");
            assertThat(staff.getShift()).isEqualTo("Night");
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
        @DisplayName("Should route NonAcademicStaff to updateNonAcademicStaffFields")
        void updateRoleSpecificFields_nonAcademicStaff_routesToNonAcademicFields() {
            NonAcademicStaff staff = new NonAcademicStaff();
            staff.setRole(UserRole.ROLE_NON_ACADEMIC_STAFF);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .workLocation("Lab B")
                    .shift("Afternoon")
                    .build();

            helper.updateRoleSpecificFields(staff, request);

            assertThat(staff.getWorkLocation()).isEqualTo("Lab B");
            assertThat(staff.getShift()).isEqualTo("Afternoon");
        }

        @Test
        @DisplayName("Should handle Admin user without errors (no role-specific fields)")
        void updateRoleSpecificFields_admin_noError() {
            Admin admin = new Admin();
            admin.setRole(UserRole.ROLE_ADMIN);
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .officeLocation("Should be ignored")
                    .build();

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
