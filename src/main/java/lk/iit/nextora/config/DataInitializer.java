package lk.iit.nextora.config;

import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        createSuperAdmin();
        createAdmin();
        createLecturer();
        createAcademicStaff();
        createNonAcademicStaff();
        createStudents();

        log.info("Data initialization completed successfully!");
    }

    private void createSuperAdmin() {
        if (superAdminRepository.count() == 0) {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setFirstName("Robert");
            superAdmin.setLastName("Johnson");
            superAdmin.setEmail("robert.superadmin@example.com");
            superAdmin.setPhoneNumber("0775556677");
            superAdmin.setPassword(passwordEncoder.encode("Password123"));
            superAdmin.setRole(UserRole.ROLE_SUPER_ADMIN);
            superAdmin.setSuperAdminId("SA001");
            superAdmin.setAssignedDate(LocalDate.of(2024, 1, 1));
            superAdmin.setAccessLevel("FULL_ACCESS");
            superAdminRepository.save(superAdmin);
            log.info("Created SuperAdmin: {}", superAdmin.getEmail());
        }
    }

    private void createAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setFirstName("Emily");
            admin.setLastName("Davis");
            admin.setEmail("emily.admin@example.com");
            admin.setPhoneNumber("0774445566");
            admin.setPassword(passwordEncoder.encode("Password123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setAdminId("ADM001");
            admin.setDepartment("Administration");
            admin.setPermissions(Set.of("USER_MANAGEMENT", "CONTENT_MANAGEMENT", "REPORTS"));
            admin.setAssignedDate(LocalDate.of(2024, 1, 10));
            adminRepository.save(admin);
            log.info("Created Admin: {}", admin.getEmail());
        }
    }

    private void createLecturer() {
        if (lecturerRepository.count() == 0) {
            Lecturer lecturer = new Lecturer();
            lecturer.setFirstName("Dr. James");
            lecturer.setLastName("Smith");
            lecturer.setEmail("james.lecturer@example.com");
            lecturer.setPhoneNumber("0771112233");
            lecturer.setPassword(passwordEncoder.encode("Password123"));
            lecturer.setRole(UserRole.ROLE_LECTURER);
            lecturer.setEmployeeId("LEC001");
            lecturer.setDepartment("Computer Science");
            lecturer.setFaculty("Computing");
            lecturer.setDesignation("Senior Lecturer");
            lecturer.setSpecialization("Machine Learning");
            lecturer.setQualifications(Set.of("PhD", "MSc", "BSc"));
            lecturer.setOfficeLocation("Block A, Room 101");
            lecturer.setBio("Experienced lecturer in AI and ML");
            lecturerRepository.save(lecturer);
            log.info("Created Lecturer: {}", lecturer.getEmail());
        }
    }

    private void createAcademicStaff() {
        if (academicStaffRepository.count() == 0) {
            AcademicStaff academicStaff = new AcademicStaff();
            academicStaff.setFirstName("Sarah");
            academicStaff.setLastName("Williams");
            academicStaff.setEmail("sarah.academic@example.com");
            academicStaff.setPhoneNumber("0772223344");
            academicStaff.setPassword(passwordEncoder.encode("Password123"));
            academicStaff.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            academicStaff.setEmployeeId("ACS001");
            academicStaff.setDepartment("Research and Development");
            academicStaff.setPosition("Research Coordinator");
            academicStaff.setOfficeLocation("Block B, Room 205");
            academicStaff.setJoinDate(LocalDate.of(2023, 1, 15));
            academicStaff.setResponsibilities("Coordinating research activities and publications");
            academicStaffRepository.save(academicStaff);
            log.info("Created Academic Staff: {}", academicStaff.getEmail());
        }
    }

    private void createNonAcademicStaff() {
        if (nonAcademicStaffRepository.count() == 0) {
            NonAcademicStaff nonAcademicStaff = new NonAcademicStaff();
            nonAcademicStaff.setFirstName("Michael");
            nonAcademicStaff.setLastName("Brown");
            nonAcademicStaff.setEmail("michael.nonacademic@example.com");
            nonAcademicStaff.setPhoneNumber("0773334455");
            nonAcademicStaff.setPassword(passwordEncoder.encode("Password123"));
            nonAcademicStaff.setRole(UserRole.ROLE_NON_ACADEMIC_STAFF);
            nonAcademicStaff.setEmployeeId("NAS001");
            nonAcademicStaff.setDepartment("IT Support");
            nonAcademicStaff.setPosition("IT Administrator");
            nonAcademicStaff.setWorkLocation("Block C, Room 102");
            nonAcademicStaff.setJoinDate(LocalDate.of(2022, 6, 20));
            nonAcademicStaffRepository.save(nonAcademicStaff);
            log.info("Created Non-Academic Staff: {}", nonAcademicStaff.getEmail());
        }
    }

    private void createStudents() {
        if (studentRepository.count() == 0) {
            // 1. Normal Student
            Student normalStudent = new Student();
            normalStudent.setFirstName("John");
            normalStudent.setLastName("Doe");
            normalStudent.setEmail("normal.student@iit.ac.lk");
            normalStudent.setPhoneNumber("+94771234567");
            normalStudent.setPassword(passwordEncoder.encode("Test@123"));
            normalStudent.setRole(UserRole.ROLE_STUDENT);
            normalStudent.setStudentId("IIT2024001");
            normalStudent.setBatch("2024");
            normalStudent.setProgram("BSc Computer Science");
            normalStudent.setFaculty("Computing");
            normalStudent.setDateOfBirth(LocalDate.of(2002, 5, 15));
            normalStudent.setAddress("123 Main Street, Colombo");
            normalStudent.setGuardianName("Robert Doe");
            normalStudent.setGuardianPhone("+94777654321");
            normalStudent.setStudentRoleType(StudentRoleType.NORMAL);
            studentRepository.save(normalStudent);
            log.info("Created Normal Student: {}", normalStudent.getEmail());

            // 2. Club Member Student
            Student clubMember = new Student();
            clubMember.setFirstName("Jane");
            clubMember.setLastName("Smith");
            clubMember.setEmail("club.member@iit.ac.lk");
            clubMember.setPhoneNumber("+94771234568");
            clubMember.setPassword(passwordEncoder.encode("Test@123"));
            clubMember.setRole(UserRole.ROLE_STUDENT);
            clubMember.setStudentId("IIT2024002");
            clubMember.setBatch("2024");
            clubMember.setProgram("BSc Computer Science");
            clubMember.setFaculty("Computing");
            clubMember.setDateOfBirth(LocalDate.of(2002, 8, 20));
            clubMember.setAddress("456 Park Road, Kandy");
            clubMember.setGuardianName("Mary Smith");
            clubMember.setGuardianPhone("+94777654322");
            clubMember.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            clubMember.setClubName("IEEE Student Branch");
            clubMember.setClubPosition("Secretary");
            clubMember.setClubJoinDate(LocalDate.of(2024, 3, 15));
            clubMember.setClubMembershipId("IEEE-2024-001");
            studentRepository.save(clubMember);
            log.info("Created Club Member Student: {}", clubMember.getEmail());

            // 3. Senior Kuppi Student
            Student seniorKuppi = new Student();
            seniorKuppi.setFirstName("Mike");
            seniorKuppi.setLastName("Johnson");
            seniorKuppi.setEmail("senior.kuppi@iit.ac.lk");
            seniorKuppi.setPhoneNumber("+94771234569");
            seniorKuppi.setPassword(passwordEncoder.encode("Test@123"));
            seniorKuppi.setRole(UserRole.ROLE_STUDENT);
            seniorKuppi.setStudentId("IIT2022001");
            seniorKuppi.setBatch("2022");
            seniorKuppi.setProgram("BSc Computer Science");
            seniorKuppi.setFaculty("Computing");
            seniorKuppi.setDateOfBirth(LocalDate.of(2000, 3, 10));
            seniorKuppi.setAddress("789 Lake View, Galle");
            seniorKuppi.setGuardianName("David Johnson");
            seniorKuppi.setGuardianPhone("+94777654323");
            seniorKuppi.setStudentRoleType(StudentRoleType.SENIOR_KUPPI);
            seniorKuppi.setKuppiSubjects(Set.of("Data Structures", "Algorithms", "Database Systems", "OOP"));
            seniorKuppi.setKuppiExperienceLevel("Advanced");
            seniorKuppi.setKuppiAvailability("Weekends 10AM-4PM, Weekdays after 6PM");
            seniorKuppi.setKuppiSessionsCompleted(0);
            seniorKuppi.setKuppiRating(0.0);
            studentRepository.save(seniorKuppi);
            log.info("Created Senior Kuppi Student: {}", seniorKuppi.getEmail());

            // 4. Batch Representative Student
            Student batchRep = new Student();
            batchRep.setFirstName("Sarah");
            batchRep.setLastName("Williams");
            batchRep.setEmail("batch.rep@iit.ac.lk");
            batchRep.setPhoneNumber("+94771234570");
            batchRep.setPassword(passwordEncoder.encode("Test@123"));
            batchRep.setRole(UserRole.ROLE_STUDENT);
            batchRep.setStudentId("IIT2024003");
            batchRep.setBatch("2024");
            batchRep.setProgram("BSc Computer Science");
            batchRep.setFaculty("Computing");
            batchRep.setDateOfBirth(LocalDate.of(2002, 11, 25));
            batchRep.setAddress("321 Hill Street, Negombo");
            batchRep.setGuardianName("James Williams");
            batchRep.setGuardianPhone("+94777654324");
            batchRep.setStudentRoleType(StudentRoleType.BATCH_REP);
            batchRep.setBatchRepYear("2024");
            batchRep.setBatchRepSemester("Semester 1");
            batchRep.setBatchRepElectedDate(LocalDate.of(2024, 1, 10));
            batchRep.setBatchRepResponsibilities("Coordinate with faculty, organize batch events, represent student concerns to administration");
            studentRepository.save(batchRep);
            log.info("Created Batch Rep Student: {}", batchRep.getEmail());
        }
    }
}
