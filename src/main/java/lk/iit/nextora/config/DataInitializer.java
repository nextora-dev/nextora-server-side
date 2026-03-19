package lk.iit.nextora.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.iit.nextora.common.enums.*;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.repository.*;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubMembership;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.election.entity.*;
import lk.iit.nextora.module.election.repository.*;
import lk.iit.nextora.module.event.entity.Event;
import lk.iit.nextora.module.event.entity.EventRegistration;
import lk.iit.nextora.module.event.repository.EventRegistrationRepository;
import lk.iit.nextora.module.event.repository.EventRepository;
import lk.iit.nextora.module.intranet.entity.*;
import lk.iit.nextora.module.intranet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final StudentRepository studentRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final ElectionRepository electionRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Event repositories ─────────────────────────────────────────
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    // ── Intranet repositories ───────────────────────────────────────
    private final StudentComplaintCategoryRepository complaintRepo;
    private final AcademicCalendarRepository calendarRepo;
    private final ProgramRepository programRepo;
    private final FoundationCategoryRepository foundationRepo;
    private final SruCategoryRepository sruRepo;
    private final StudentPolicyRepository policyRepo;
    private final MitigationFormRepository mitigationRepo;
    private final StaffCategoryRepository staffRepo;
    private final InfoCategoryRepository infoRepo;
    private final ScheduleCategoryRepository scheduleRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        createSuperAdmin();
        createAdmin();
        createAcademicStaff();
        createNonAcademicStaff();
        createStudents();
        createClubs();
        createClubMemberships();
        createElections();
        createEvents();

        // ── Intranet content seeding ────────────────────────────────
        seedIntranetContent();

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

    private void createAcademicStaff() {
        if (academicStaffRepository.count() == 0) {
            // Create first academic staff (with lecturer-like details)
            AcademicStaff lecturer = new AcademicStaff();
            lecturer.setFirstName("Dr. James");
            lecturer.setLastName("Smith");
            lecturer.setEmail("james.academic@example.com");
            lecturer.setPhoneNumber("0771112233");
            lecturer.setPassword(passwordEncoder.encode("Password123"));
            lecturer.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            lecturer.setEmployeeId("ACS001");
            lecturer.setDepartment("Computer Science");
            lecturer.setFaculty(FacultyType.COMPUTING);
            lecturer.setPosition("Senior Lecturer");
            lecturer.setDesignation("Senior Lecturer");
            lecturer.setSpecialization("Machine Learning");
            lecturer.setQualifications(Set.of("PhD", "MSc", "BSc"));
            lecturer.setOfficeLocation("Block A, Room 101");
            lecturer.setBio("Experienced lecturer in AI and ML");
            lecturer.setJoinDate(LocalDate.of(2020, 1, 15));
            academicStaffRepository.save(lecturer);
            log.info("Created Academic Staff (Lecturer): {}", lecturer.getEmail());

            // Create second academic staff (research coordinator)
            AcademicStaff academicStaff = new AcademicStaff();
            academicStaff.setFirstName("Sarah");
            academicStaff.setLastName("Williams");
            academicStaff.setEmail("sarah.academic@example.com");
            academicStaff.setPhoneNumber("0772223344");
            academicStaff.setPassword(passwordEncoder.encode("Password123"));
            academicStaff.setRole(UserRole.ROLE_ACADEMIC_STAFF);
            academicStaff.setEmployeeId("ACS002");
            academicStaff.setDepartment("Research and Development");
            academicStaff.setFaculty(FacultyType.COMPUTING);
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
            normalStudent.setFaculty(FacultyType.COMPUTING);
            normalStudent.setDateOfBirth(LocalDate.of(2002, 5, 15));
            normalStudent.setAddress("123 Main Street, Colombo");
            normalStudent.setGuardianName("Robert Doe");
            normalStudent.setGuardianPhone("+94777654321");
            normalStudent.setStudentRoleType(StudentRoleType.NORMAL);
            studentRepository.save(normalStudent);
            log.info("Created Normal Student: {}", normalStudent.getEmail());

            // 2. Club President
            Student clubPresident = new Student();
            clubPresident.setFirstName("Jane");
            clubPresident.setLastName("Smith");
            clubPresident.setEmail("club.president@iit.ac.lk");
            clubPresident.setPhoneNumber("+94771234568");
            clubPresident.setPassword(passwordEncoder.encode("Test@123"));
            clubPresident.setRole(UserRole.ROLE_STUDENT);
            clubPresident.setStudentId("IIT2023001");
            clubPresident.setBatch("2023");
            clubPresident.setProgram("BSc Computer Science");
            clubPresident.setFaculty(FacultyType.COMPUTING);
            clubPresident.setDateOfBirth(LocalDate.of(2001, 8, 20));
            clubPresident.setAddress("456 Park Road, Kandy");
            clubPresident.setGuardianName("Mary Smith");
            clubPresident.setGuardianPhone("+94777654322");
            clubPresident.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            clubPresident.setClubName("IEEE Student Branch");
            clubPresident.setClubPosition(ClubPositionsType.PRESIDENT);
            clubPresident.setClubJoinDate(LocalDate.of(2022, 3, 15));
            clubPresident.setClubMembershipId("IEEE-2024-001");
            studentRepository.save(clubPresident);
            log.info("Created Club President: {}", clubPresident.getEmail());

            // 3. Club Vice President
            Student clubVicePresident = new Student();
            clubVicePresident.setFirstName("Michael");
            clubVicePresident.setLastName("Johnson");
            clubVicePresident.setEmail("club.vp@iit.ac.lk");
            clubVicePresident.setPhoneNumber("+94771234569");
            clubVicePresident.setPassword(passwordEncoder.encode("Test@123"));
            clubVicePresident.setRole(UserRole.ROLE_STUDENT);
            clubVicePresident.setStudentId("IIT2023002");
            clubVicePresident.setBatch("2023");
            clubVicePresident.setProgram("BSc Software Engineering");
            clubVicePresident.setFaculty(FacultyType.COMPUTING);
            clubVicePresident.setDateOfBirth(LocalDate.of(2001, 3, 10));
            clubVicePresident.setAddress("789 Lake View, Galle");
            clubVicePresident.setGuardianName("David Johnson");
            clubVicePresident.setGuardianPhone("+94777654323");
            clubVicePresident.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            clubVicePresident.setClubName("IEEE Student Branch");
            clubVicePresident.setClubPosition(ClubPositionsType.VICE_PRESIDENT);
            clubVicePresident.setClubJoinDate(LocalDate.of(2022, 5, 20));
            clubVicePresident.setClubMembershipId("IEEE-2024-002");
            studentRepository.save(clubVicePresident);
            log.info("Created Club Vice President: {}", clubVicePresident.getEmail());

            // 4. Club Secretary
            Student clubSecretary = new Student();
            clubSecretary.setFirstName("Emily");
            clubSecretary.setLastName("Davis");
            clubSecretary.setEmail("club.secretary@iit.ac.lk");
            clubSecretary.setPhoneNumber("+94771234570");
            clubSecretary.setPassword(passwordEncoder.encode("Test@123"));
            clubSecretary.setRole(UserRole.ROLE_STUDENT);
            clubSecretary.setStudentId("IIT2023003");
            clubSecretary.setBatch("2023");
            clubSecretary.setProgram("BSc Computer Science");
            clubSecretary.setFaculty(FacultyType.COMPUTING);
            clubSecretary.setDateOfBirth(LocalDate.of(2001, 11, 25));
            clubSecretary.setAddress("321 Hill Street, Negombo");
            clubSecretary.setGuardianName("James Davis");
            clubSecretary.setGuardianPhone("+94777654324");
            clubSecretary.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            clubSecretary.setClubName("IEEE Student Branch");
            clubSecretary.setClubPosition(ClubPositionsType.SECRETARY);
            clubSecretary.setClubJoinDate(LocalDate.of(2022, 8, 10));
            clubSecretary.setClubMembershipId("IEEE-2024-003");
            studentRepository.save(clubSecretary);
            log.info("Created Club Secretary: {}", clubSecretary.getEmail());

            // 5. Club Treasurer
            Student clubTreasurer = new Student();
            clubTreasurer.setFirstName("Daniel");
            clubTreasurer.setLastName("Wilson");
            clubTreasurer.setEmail("club.treasurer@iit.ac.lk");
            clubTreasurer.setPhoneNumber("+94771234571");
            clubTreasurer.setPassword(passwordEncoder.encode("Test@123"));
            clubTreasurer.setRole(UserRole.ROLE_STUDENT);
            clubTreasurer.setStudentId("IIT2023004");
            clubTreasurer.setBatch("2023");
            clubTreasurer.setProgram("BSc Information Technology");
            clubTreasurer.setFaculty(FacultyType.COMPUTING);
            clubTreasurer.setDateOfBirth(LocalDate.of(2001, 6, 15));
            clubTreasurer.setAddress("555 Ocean Drive, Matara");
            clubTreasurer.setGuardianName("Richard Wilson");
            clubTreasurer.setGuardianPhone("+94777654325");
            clubTreasurer.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            clubTreasurer.setClubName("IEEE Student Branch");
            clubTreasurer.setClubPosition(ClubPositionsType.TREASURER);
            clubTreasurer.setClubJoinDate(LocalDate.of(2022, 9, 1));
            clubTreasurer.setClubMembershipId("IEEE-2024-004");
            studentRepository.save(clubTreasurer);
            log.info("Created Club Treasurer: {}", clubTreasurer.getEmail());

            // 6. Top Board Member
            Student topBoardMember = new Student();
            topBoardMember.setFirstName("Sophia");
            topBoardMember.setLastName("Brown");
            topBoardMember.setEmail("club.topboard@iit.ac.lk");
            topBoardMember.setPhoneNumber("+94771234572");
            topBoardMember.setPassword(passwordEncoder.encode("Test@123"));
            topBoardMember.setRole(UserRole.ROLE_STUDENT);
            topBoardMember.setStudentId("IIT2023005");
            topBoardMember.setBatch("2023");
            topBoardMember.setProgram("BSc Computer Science");
            topBoardMember.setFaculty(FacultyType.COMPUTING);
            topBoardMember.setDateOfBirth(LocalDate.of(2001, 4, 5));
            topBoardMember.setAddress("777 Valley Road, Colombo 7");
            topBoardMember.setGuardianName("Thomas Brown");
            topBoardMember.setGuardianPhone("+94777654326");
            topBoardMember.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            topBoardMember.setClubName("IEEE Student Branch");
            topBoardMember.setClubPosition(ClubPositionsType.Top_Board_MEMBER);
            topBoardMember.setClubJoinDate(LocalDate.of(2022, 10, 15));
            topBoardMember.setClubMembershipId("IEEE-2024-005");
            studentRepository.save(topBoardMember);
            log.info("Created Top Board Member: {}", topBoardMember.getEmail());

            // 7. Committee Member
            Student committeeMember = new Student();
            committeeMember.setFirstName("Oliver");
            committeeMember.setLastName("Taylor");
            committeeMember.setEmail("club.committee@iit.ac.lk");
            committeeMember.setPhoneNumber("+94771234573");
            committeeMember.setPassword(passwordEncoder.encode("Test@123"));
            committeeMember.setRole(UserRole.ROLE_STUDENT);
            committeeMember.setStudentId("IIT2024002");
            committeeMember.setBatch("2024");
            committeeMember.setProgram("BSc Software Engineering");
            committeeMember.setFaculty(FacultyType.COMPUTING);
            committeeMember.setDateOfBirth(LocalDate.of(2002, 7, 20));
            committeeMember.setAddress("888 Green Lane, Dehiwala");
            committeeMember.setGuardianName("William Taylor");
            committeeMember.setGuardianPhone("+94777654327");
            committeeMember.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            committeeMember.setClubName("IEEE Student Branch");
            committeeMember.setClubPosition(ClubPositionsType.COMMITTEE_MEMBER);
            committeeMember.setClubJoinDate(LocalDate.of(2024, 1, 10));
            committeeMember.setClubMembershipId("IEEE-2024-006");
            studentRepository.save(committeeMember);
            log.info("Created Committee Member: {}", committeeMember.getEmail());

            // 8. General Club Member
            Student generalMember1 = new Student();
            generalMember1.setFirstName("Emma");
            generalMember1.setLastName("Anderson");
            generalMember1.setEmail("club.member@iit.ac.lk");
            generalMember1.setPhoneNumber("+94771234574");
            generalMember1.setPassword(passwordEncoder.encode("Test@123"));
            generalMember1.setRole(UserRole.ROLE_STUDENT);
            generalMember1.setStudentId("IIT2024003");
            generalMember1.setBatch("2024");
            generalMember1.setProgram("BSc Computer Science");
            generalMember1.setFaculty(FacultyType.COMPUTING);
            generalMember1.setDateOfBirth(LocalDate.of(2002, 9, 12));
            generalMember1.setAddress("999 Palm Street, Mount Lavinia");
            generalMember1.setGuardianName("George Anderson");
            generalMember1.setGuardianPhone("+94777654328");
            generalMember1.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            generalMember1.setClubName("IEEE Student Branch");
            generalMember1.setClubPosition(ClubPositionsType.GENERAL_MEMBER);
            generalMember1.setClubJoinDate(LocalDate.of(2024, 2, 1));
            generalMember1.setClubMembershipId("IEEE-2024-007");
            studentRepository.save(generalMember1);
            log.info("Created General Club Member 1: {}", generalMember1.getEmail());

            // 10. General Club Member 2
            Student generalMember2 = new Student();
            generalMember2.setFirstName("Noah");
            generalMember2.setLastName("Clark");
            generalMember2.setEmail("club.member2@iit.ac.lk");
            generalMember2.setPhoneNumber("+94771234579");
            generalMember2.setPassword(passwordEncoder.encode("Test@123"));
            generalMember2.setRole(UserRole.ROLE_STUDENT);
            generalMember2.setStudentId("IIT2024006");
            generalMember2.setBatch("2024");
            generalMember2.setProgram("BSc Information Technology");
            generalMember2.setFaculty(FacultyType.COMPUTING);
            generalMember2.setDateOfBirth(LocalDate.of(2002, 4, 18));
            generalMember2.setAddress("123 Sunset Avenue, Moratuwa");
            generalMember2.setGuardianName("Peter Clark");
            generalMember2.setGuardianPhone("+94777654333");
            generalMember2.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            generalMember2.setClubName("IEEE Student Branch");
            generalMember2.setClubPosition(ClubPositionsType.GENERAL_MEMBER);
            generalMember2.setClubJoinDate(LocalDate.of(2024, 3, 15));
            generalMember2.setClubMembershipId("IEEE-2024-008");
            studentRepository.save(generalMember2);
            log.info("Created General Club Member 2: {}", generalMember2.getEmail());

            // 11. Senior Kuppi Student
            Student seniorKuppi = new Student();
            seniorKuppi.setFirstName("Lucas");
            seniorKuppi.setLastName("Martinez");
            seniorKuppi.setEmail("senior.kuppi@iit.ac.lk");
            seniorKuppi.setPhoneNumber("+94771234575");
            seniorKuppi.setPassword(passwordEncoder.encode("Test@123"));
            seniorKuppi.setRole(UserRole.ROLE_STUDENT);
            seniorKuppi.setStudentId("IIT2022001");
            seniorKuppi.setBatch("2022");
            seniorKuppi.setProgram("BSc Computer Science");
            seniorKuppi.setFaculty(FacultyType.COMPUTING);
            seniorKuppi.setDateOfBirth(LocalDate.of(2000, 3, 10));
            seniorKuppi.setAddress("111 Tech Park, Colombo 3");
            seniorKuppi.setGuardianName("Carlos Martinez");
            seniorKuppi.setGuardianPhone("+94777654329");
            seniorKuppi.setStudentRoleType(StudentRoleType.SENIOR_KUPPI);
            seniorKuppi.setKuppiSubjects(Set.of("Data Structures", "Algorithms", "Database Systems", "OOP"));
            seniorKuppi.setKuppiExperienceLevel("Advanced");
            seniorKuppi.setKuppiAvailability("Weekends 10AM-4PM, Weekdays after 6PM");
            seniorKuppi.setKuppiSessionsCompleted(0);
            seniorKuppi.setKuppiRating(0.0);
            studentRepository.save(seniorKuppi);
            log.info("Created Senior Kuppi Student: {}", seniorKuppi.getEmail());

            // 10. Batch Representative Student
            Student batchRep = new Student();
            batchRep.setFirstName("Isabella");
            batchRep.setLastName("Garcia");
            batchRep.setEmail("batch.rep@iit.ac.lk");
            batchRep.setPhoneNumber("+94771234576");
            batchRep.setPassword(passwordEncoder.encode("Test@123"));
            batchRep.setRole(UserRole.ROLE_STUDENT);
            batchRep.setStudentId("IIT2024004");
            batchRep.setBatch("2024");
            batchRep.setProgram("BSc Computer Science");
            batchRep.setFaculty(FacultyType.COMPUTING);
            batchRep.setDateOfBirth(LocalDate.of(2002, 11, 25));
            batchRep.setAddress("222 University Ave, Colombo 5");
            batchRep.setGuardianName("Roberto Garcia");
            batchRep.setGuardianPhone("+94777654330");
            batchRep.setStudentRoleType(StudentRoleType.BATCH_REP);
            batchRep.setBatchRepYear("2024");
            batchRep.setBatchRepSemester("Semester 1");
            batchRep.setBatchRepElectedDate(LocalDate.of(2024, 1, 10));
            batchRep.setBatchRepResponsibilities("Coordinate with faculty, organize batch events, represent student concerns to administration");
            studentRepository.save(batchRep);
            log.info("Created Batch Rep Student: {}", batchRep.getEmail());

            // 11. Computing Society President
            Student csPresident = new Student();
            csPresident.setFirstName("Ethan");
            csPresident.setLastName("Lee");
            csPresident.setEmail("cs.president@iit.ac.lk");
            csPresident.setPhoneNumber("+94771234577");
            csPresident.setPassword(passwordEncoder.encode("Test@123"));
            csPresident.setRole(UserRole.ROLE_STUDENT);
            csPresident.setStudentId("IIT2022002");
            csPresident.setBatch("2022");
            csPresident.setProgram("BSc Software Engineering");
            csPresident.setFaculty(FacultyType.COMPUTING);
            csPresident.setDateOfBirth(LocalDate.of(2000, 12, 8));
            csPresident.setAddress("333 Code Street, Colombo 4");
            csPresident.setGuardianName("David Lee");
            csPresident.setGuardianPhone("+94777654331");
            csPresident.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            csPresident.setClubName("Computing Society");
            csPresident.setClubPosition(ClubPositionsType.PRESIDENT);
            csPresident.setClubJoinDate(LocalDate.of(2021, 6, 1));
            csPresident.setClubMembershipId("CS-2024-001");
            studentRepository.save(csPresident);
            log.info("Created Computing Society President: {}", csPresident.getEmail());

            // 12. Business Club Member
            Student businessMember = new Student();
            businessMember.setFirstName("Ava");
            businessMember.setLastName("White");
            businessMember.setEmail("business.member@iit.ac.lk");
            businessMember.setPhoneNumber("+94771234578");
            businessMember.setPassword(passwordEncoder.encode("Test@123"));
            businessMember.setRole(UserRole.ROLE_STUDENT);
            businessMember.setStudentId("IIT2024005");
            businessMember.setBatch("2024");
            businessMember.setProgram("BSc Business Management");
            businessMember.setFaculty(FacultyType.BUSINESS);
            businessMember.setDateOfBirth(LocalDate.of(2002, 2, 14));
            businessMember.setAddress("444 Commerce Road, Colombo 2");
            businessMember.setGuardianName("John White");
            businessMember.setGuardianPhone("+94777654332");
            businessMember.setStudentRoleType(StudentRoleType.CLUB_MEMBER);
            businessMember.setClubName("Business Club");
            businessMember.setClubPosition(ClubPositionsType.GENERAL_MEMBER);
            businessMember.setClubJoinDate(LocalDate.of(2024, 3, 1));
            businessMember.setClubMembershipId("BC-2024-001");
            studentRepository.save(businessMember);
            log.info("Created Business Club Member: {}", businessMember.getEmail());
        }
    }

    private void createClubs() {
        if (clubRepository.count() == 0) {
            AcademicStaff advisor = academicStaffRepository.findAll().stream().findFirst().orElse(null);
            Student ieeePresident = studentRepository.findByEmail("club.president@iit.ac.lk").orElse(null);
            Student csPresident = studentRepository.findByEmail("cs.president@iit.ac.lk").orElse(null);

            // 1. IEEE Student Branch
            Club ieee = new Club();
            ieee.setClubCode("IEEE-2024");
            ieee.setName("IEEE Student Branch");
            ieee.setDescription("IEEE Student Branch of IIT focuses on advancing technology for humanity. We organize technical workshops, hackathons, and networking events.");
            ieee.setLogoUrl("https://example.com/logos/ieee.png");
            ieee.setFaculty(FacultyType.COMPUTING);
            ieee.setEmail("ieee@iit.ac.lk");
            ieee.setContactNumber("+94771234567");
            ieee.setEstablishedDate(LocalDate.of(2015, 1, 15));
            ieee.setSocialMediaLinks("https://facebook.com/iit.ieee,https://instagram.com/iit.ieee");
            ieee.setPresident(ieeePresident);
            ieee.setAdvisor(advisor);
            ieee.setMaxMembers(200);
            ieee.setIsRegistrationOpen(true);
            clubRepository.save(ieee);
            log.info("Created Club: {}", ieee.getName());

            // 2. Computing Society
            Club computingSociety = new Club();
            computingSociety.setClubCode("CS-2024");
            computingSociety.setName("Computing Society");
            computingSociety.setDescription("The Computing Society promotes software development, competitive programming, and tech innovation among students.");
            computingSociety.setLogoUrl("https://example.com/logos/computing-society.png");
            computingSociety.setFaculty(FacultyType.COMPUTING);
            computingSociety.setEmail("computing.society@iit.ac.lk");
            computingSociety.setContactNumber("+94772345678");
            computingSociety.setEstablishedDate(LocalDate.of(2018, 3, 20));
            computingSociety.setSocialMediaLinks("https://facebook.com/iit.cs,https://github.com/iit-cs");
            computingSociety.setPresident(csPresident);
            computingSociety.setAdvisor(advisor);
            computingSociety.setMaxMembers(150);
            computingSociety.setIsRegistrationOpen(true);
            clubRepository.save(computingSociety);
            log.info("Created Club: {}", computingSociety.getName());

            // 3. Business Club
            Club businessClub = new Club();
            businessClub.setClubCode("BC-2024");
            businessClub.setName("Business Club");
            businessClub.setDescription("The Business Club enhances entrepreneurial skills, organizes business competitions, and connects students with industry leaders.");
            businessClub.setLogoUrl("https://example.com/logos/business-club.png");
            businessClub.setFaculty(FacultyType.BUSINESS);
            businessClub.setEmail("business.club@iit.ac.lk");
            businessClub.setContactNumber("+94773456789");
            businessClub.setEstablishedDate(LocalDate.of(2016, 6, 10));
            businessClub.setSocialMediaLinks("https://linkedin.com/company/iit-bc");
            businessClub.setMaxMembers(100);
            businessClub.setIsRegistrationOpen(true);
            clubRepository.save(businessClub);
            log.info("Created Club: {}", businessClub.getName());

            // 4. Robotics Club
            Club roboticsClub = new Club();
            roboticsClub.setClubCode("RC-2024");
            roboticsClub.setName("Robotics Club");
            roboticsClub.setDescription("The Robotics Club focuses on building robots, competing in robotics competitions, and exploring AI and automation technologies.");
            roboticsClub.setLogoUrl("https://example.com/logos/robotics.png");
            roboticsClub.setFaculty(FacultyType.COMPUTING);
            roboticsClub.setEmail("robotics@iit.ac.lk");
            roboticsClub.setContactNumber("+94774567890");
            roboticsClub.setEstablishedDate(LocalDate.of(2019, 9, 1));
            roboticsClub.setSocialMediaLinks("https://youtube.com/iit-robotics");
            roboticsClub.setAdvisor(advisor);
            roboticsClub.setMaxMembers(80);
            roboticsClub.setIsRegistrationOpen(true);
            clubRepository.save(roboticsClub);
            log.info("Created Club: {}", roboticsClub.getName());
        }
    }

    private void createClubMemberships() {
        if (clubMembershipRepository.count() == 0) {
            Club ieee = clubRepository.findByClubCode("IEEE-2024").orElse(null);
            Club computingSociety = clubRepository.findByClubCode("CS-2024").orElse(null);
            Club businessClub = clubRepository.findByClubCode("BC-2024").orElse(null);

            // Get all students
            Student ieeePresident = studentRepository.findByEmail("club.president@iit.ac.lk").orElse(null);
            Student ieeeVP = studentRepository.findByEmail("club.vp@iit.ac.lk").orElse(null);
            Student ieeeSecretary = studentRepository.findByEmail("club.secretary@iit.ac.lk").orElse(null);
            Student ieeeTreasurer = studentRepository.findByEmail("club.treasurer@iit.ac.lk").orElse(null);
            Student ieeeTopBoard = studentRepository.findByEmail("club.topboard@iit.ac.lk").orElse(null);
            Student ieeeCommittee = studentRepository.findByEmail("club.committee@iit.ac.lk").orElse(null);
            Student ieeeGeneral1 = studentRepository.findByEmail("club.member@iit.ac.lk").orElse(null);
            Student ieeeGeneral2 = studentRepository.findByEmail("club.member2@iit.ac.lk").orElse(null);
            Student normalStudent = studentRepository.findByEmail("normal.student@iit.ac.lk").orElse(null);
            Student seniorKuppi = studentRepository.findByEmail("senior.kuppi@iit.ac.lk").orElse(null);
            Student batchRep = studentRepository.findByEmail("batch.rep@iit.ac.lk").orElse(null);
            Student csPresident = studentRepository.findByEmail("cs.president@iit.ac.lk").orElse(null);
            Student businessMember = studentRepository.findByEmail("business.member@iit.ac.lk").orElse(null);

            // IEEE Club Memberships
            if (ieee != null) {
                // President
                if (ieeePresident != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeePresident);
                    membership.setMembershipNumber("IEEE-M-001");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.PRESIDENT);
                    membership.setJoinDate(LocalDate.of(2022, 3, 15));
                    membership.setApprovedAt(LocalDateTime.of(2022, 3, 16, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as PRESIDENT", ieeePresident.getFullName());
                }

                // Vice President
                if (ieeeVP != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeVP);
                    membership.setMembershipNumber("IEEE-M-002");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.VICE_PRESIDENT);
                    membership.setJoinDate(LocalDate.of(2022, 5, 20));
                    membership.setApprovedAt(LocalDateTime.of(2022, 5, 21, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as VICE_PRESIDENT", ieeeVP.getFullName());
                }

                // Secretary
                if (ieeeSecretary != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeSecretary);
                    membership.setMembershipNumber("IEEE-M-003");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.SECRETARY);
                    membership.setJoinDate(LocalDate.of(2022, 8, 10));
                    membership.setApprovedAt(LocalDateTime.of(2022, 8, 11, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as SECRETARY", ieeeSecretary.getFullName());
                }

                // Treasurer
                if (ieeeTreasurer != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeTreasurer);
                    membership.setMembershipNumber("IEEE-M-004");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.TREASURER);
                    membership.setJoinDate(LocalDate.of(2022, 9, 1));
                    membership.setApprovedAt(LocalDateTime.of(2022, 9, 2, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as TREASURER", ieeeTreasurer.getFullName());
                }

                // Top Board Member
                if (ieeeTopBoard != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeTopBoard);
                    membership.setMembershipNumber("IEEE-M-005");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.Top_Board_MEMBER);
                    membership.setJoinDate(LocalDate.of(2022, 10, 15));
                    membership.setApprovedAt(LocalDateTime.of(2022, 10, 16, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as Top_Board_MEMBER", ieeeTopBoard.getFullName());
                }

                // Committee Member
                if (ieeeCommittee != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeCommittee);
                    membership.setMembershipNumber("IEEE-M-006");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.COMMITTEE_MEMBER);
                    membership.setJoinDate(LocalDate.of(2024, 1, 10));
                    membership.setApprovedAt(LocalDateTime.of(2024, 1, 11, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as COMMITTEE_MEMBER", ieeeCommittee.getFullName());
                }

                // General Member 1
                if (ieeeGeneral1 != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeGeneral1);
                    membership.setMembershipNumber("IEEE-M-007");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
                    membership.setJoinDate(LocalDate.of(2024, 2, 1));
                    membership.setApprovedAt(LocalDateTime.of(2024, 2, 2, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as GENERAL_MEMBER", ieeeGeneral1.getFullName());
                }

                // General Member 2
                if (ieeeGeneral2 != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(ieeeGeneral2);
                    membership.setMembershipNumber("IEEE-M-008");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
                    membership.setJoinDate(LocalDate.of(2023, 3, 15)); // Changed to 2023 to ensure > 3 months eligibility
                    membership.setApprovedAt(LocalDateTime.of(2023, 3, 16, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as GENERAL_MEMBER", ieeeGeneral2.getFullName());
                }

                // Normal student as general member
                if (normalStudent != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(ieee);
                    membership.setMember(normalStudent);
                    membership.setMembershipNumber("IEEE-M-009");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
                    membership.setJoinDate(LocalDate.of(2024, 4, 1));
                    membership.setApprovedAt(LocalDateTime.of(2024, 4, 2, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created IEEE Membership: {} as GENERAL_MEMBER", normalStudent.getFullName());
                }
            }

            // Computing Society Memberships
            if (computingSociety != null) {
                // President
                if (csPresident != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(computingSociety);
                    membership.setMember(csPresident);
                    membership.setMembershipNumber("CS-M-001");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.PRESIDENT);
                    membership.setJoinDate(LocalDate.of(2021, 6, 1));
                    membership.setApprovedAt(LocalDateTime.of(2021, 6, 2, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created CS Membership: {} as PRESIDENT", csPresident.getFullName());
                }

                // Senior Kuppi as Vice President
                if (seniorKuppi != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(computingSociety);
                    membership.setMember(seniorKuppi);
                    membership.setMembershipNumber("CS-M-002");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.VICE_PRESIDENT);
                    membership.setJoinDate(LocalDate.of(2022, 5, 10));
                    membership.setApprovedAt(LocalDateTime.of(2022, 5, 11, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created CS Membership: {} as VICE_PRESIDENT", seniorKuppi.getFullName());
                }

                // Batch Rep as Secretary
                if (batchRep != null) {
                    ClubMembership membership = new ClubMembership();
                    membership.setClub(computingSociety);
                    membership.setMember(batchRep);
                    membership.setMembershipNumber("CS-M-003");
                    membership.setStatus(ClubMembershipStatus.ACTIVE);
                    membership.setPosition(ClubPositionsType.SECRETARY);
                    membership.setJoinDate(LocalDate.of(2024, 1, 20));
                    membership.setApprovedAt(LocalDateTime.of(2024, 1, 21, 10, 0));
                    clubMembershipRepository.save(membership);
                    log.info("Created CS Membership: {} as SECRETARY", batchRep.getFullName());
                }
            }

            // Business Club Memberships
            if (businessClub != null && businessMember != null) {
                ClubMembership membership = new ClubMembership();
                membership.setClub(businessClub);
                membership.setMember(businessMember);
                membership.setMembershipNumber("BC-M-001");
                membership.setStatus(ClubMembershipStatus.ACTIVE);
                membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
                membership.setJoinDate(LocalDate.of(2024, 3, 1));
                membership.setApprovedAt(LocalDateTime.of(2024, 3, 2, 10, 0));
                clubMembershipRepository.save(membership);
                log.info("Created BC Membership: {} as GENERAL_MEMBER", businessMember.getFullName());
            }
        }
    }

    private void createElections() {
        if (electionRepository.count() == 0) {
            Club ieee = clubRepository.findByClubCode("IEEE-2024").orElse(null);
            Club computingSociety = clubRepository.findByClubCode("CS-2024").orElse(null);
            NonAcademicStaff creator = nonAcademicStaffRepository.findAll().stream().findFirst().orElse(null);

            if (ieee != null && creator != null) {
                // 1. IEEE President Election (Nomination Open)
                Election ieeePresidentElection = new Election();
                ieeePresidentElection.setTitle("IEEE President Election 2026");
                ieeePresidentElection.setDescription("Annual election for the position of IEEE Student Branch President for the academic year 2026-2027. All active members are eligible to vote.");
                ieeePresidentElection.setClub(ieee);
                ieeePresidentElection.setElectionType(ElectionType.PRESIDENT);
                ieeePresidentElection.setStatus(ElectionStatus.NOMINATION_OPEN);
                ieeePresidentElection.setNominationStartTime(LocalDateTime.now().minusDays(2));
                ieeePresidentElection.setNominationEndTime(LocalDateTime.now().plusDays(5));
                ieeePresidentElection.setVotingStartTime(LocalDateTime.now().plusDays(7));
                ieeePresidentElection.setVotingEndTime(LocalDateTime.now().plusDays(10));
                ieeePresidentElection.setCreatedBy(creator);
                ieeePresidentElection.setMaxCandidates(5);
                ieeePresidentElection.setWinnersCount(1);
                ieeePresidentElection.setIsAnonymousVoting(true);
                ieeePresidentElection.setRequireManifesto(true);
                ieeePresidentElection.setEligibilityCriteria("Must be an active IEEE member for at least 6 months. Must have a minimum GPA of 3.0.");
                electionRepository.save(ieeePresidentElection);
                log.info("Created Election: {}", ieeePresidentElection.getTitle());

                // 2. IEEE General Committee Election (Draft)
                Election ieeeCommitteeElection = new Election();
                ieeeCommitteeElection.setTitle("IEEE Committee Members Election 2026");
                ieeeCommitteeElection.setDescription("Election for selecting 3 committee members for the IEEE Student Branch. Committee members assist in organizing events and activities.");
                ieeeCommitteeElection.setClub(ieee);
                ieeeCommitteeElection.setElectionType(ElectionType.GENERAL);
                ieeeCommitteeElection.setStatus(ElectionStatus.DRAFT);
                ieeeCommitteeElection.setNominationStartTime(LocalDateTime.now().plusDays(15));
                ieeeCommitteeElection.setNominationEndTime(LocalDateTime.now().plusDays(22));
                ieeeCommitteeElection.setVotingStartTime(LocalDateTime.now().plusDays(25));
                ieeeCommitteeElection.setVotingEndTime(LocalDateTime.now().plusDays(28));
                ieeeCommitteeElection.setCreatedBy(creator);
                ieeeCommitteeElection.setMaxCandidates(10);
                ieeeCommitteeElection.setWinnersCount(3);
                ieeeCommitteeElection.setIsAnonymousVoting(true);
                ieeeCommitteeElection.setRequireManifesto(false);
                ieeeCommitteeElection.setEligibilityCriteria("Must be an active IEEE member.");
                electionRepository.save(ieeeCommitteeElection);
                log.info("Created Election: {}", ieeeCommitteeElection.getTitle());
            }

            if (computingSociety != null && creator != null) {
                // 3. Computing Society Vice President Election (Voting Open)
                Election csVPElection = new Election();
                csVPElection.setTitle("Computing Society Vice President Election 2026");
                csVPElection.setDescription("Election for the position of Vice President of the Computing Society. The VP assists the President and oversees technical events.");
                csVPElection.setClub(computingSociety);
                csVPElection.setElectionType(ElectionType.VICE_PRESIDENT);
                csVPElection.setStatus(ElectionStatus.VOTING_OPEN);
                csVPElection.setNominationStartTime(LocalDateTime.now().minusDays(10));
                csVPElection.setNominationEndTime(LocalDateTime.now().minusDays(3));
                csVPElection.setVotingStartTime(LocalDateTime.now().minusDays(1));
                csVPElection.setVotingEndTime(LocalDateTime.now().plusDays(2));
                csVPElection.setCreatedBy(creator);
                csVPElection.setMaxCandidates(4);
                csVPElection.setWinnersCount(1);
                csVPElection.setIsAnonymousVoting(true);
                csVPElection.setRequireManifesto(true);
                csVPElection.setEligibilityCriteria("Must be a Computing Society member for at least 1 year. Must have organized at least one technical event.");
                electionRepository.save(csVPElection);
                log.info("Created Election: {}", csVPElection.getTitle());

                // 4. Computing Society Poll (Completed)
                Election csPoll = new Election();
                csPoll.setTitle("Hackathon Theme Poll");
                csPoll.setDescription("Vote for the theme of the upcoming Computing Society Hackathon 2026. Options: AI/ML, Web3, IoT, HealthTech.");
                csPoll.setClub(computingSociety);
                csPoll.setElectionType(ElectionType.POLL);
                csPoll.setStatus(ElectionStatus.RESULTS_PUBLISHED);
                csPoll.setNominationStartTime(LocalDateTime.now().minusDays(20));
                csPoll.setNominationEndTime(LocalDateTime.now().minusDays(15));
                csPoll.setVotingStartTime(LocalDateTime.now().minusDays(14));
                csPoll.setVotingEndTime(LocalDateTime.now().minusDays(7));
                csPoll.setResultsPublishedAt(LocalDateTime.now().minusDays(6));
                csPoll.setCreatedBy(creator);
                csPoll.setMaxCandidates(4);
                csPoll.setWinnersCount(1);
                csPoll.setIsAnonymousVoting(true);
                csPoll.setRequireManifesto(false);
                csPoll.setEligibilityCriteria("All Computing Society members can vote.");
                electionRepository.save(csPoll);
                log.info("Created Election: {}", csPoll.getTitle());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INTRANET CONTENT SEEDING (moved from IntranetDataSeeder)
    // ═══════════════════════════════════════════════════════════════════

    private void seedIntranetContent() {
        log.info("Checking intranet content tables for seed data...");
        boolean needsReseed = isIntranetReseedRequired();
        if (needsReseed) {
            log.info("Incomplete or missing intranet data detected — clearing and re-seeding all tables...");
            clearAllIntranetData();
        }
        seedScheduleCategories();
        seedComplaints();
        seedAcademicCalendars();
        seedUndergraduatePrograms();
        seedPostgraduatePrograms();
        seedFoundationCategories();
        seedSruCategories();
        seedStudentPolicies();
        seedMitigationForms();
        seedStaffCategories();
        seedInfoCategories();
        log.info("Intranet content seed check complete.");
    }

    private boolean isIntranetReseedRequired() {
        if (!calendarRepo.existsByUniversitySlugAndIsDeletedFalse("uow")) return true;
        if (!calendarRepo.existsByUniversitySlugAndIsDeletedFalse("rgu")) return true;

        for (String slug : List.of("orientation", "temporary", "assessments", "annual-events")) {
            if (!scheduleRepo.existsByCategorySlugAndIsDeletedFalse(slug)) return true;
        }

        if (!complaintRepo.existsByCategorySlugAndIsDeletedFalse("academic-course-delivery")) return true;
        if (!complaintRepo.existsByCategorySlugAndIsDeletedFalse("facility-and-support-system")) return true;

        for (String slug : List.of("bsc-ai-ds", "bsc-cs", "beng-se", "bsc-bda", "bsc-bis", "ba-bm", "bsc-bc")) {
            if (!programRepo.existsByProgramSlugAndProgramLevelAndIsDeletedFalse(slug, "UNDERGRADUATE")) return true;
        }
        for (String slug : List.of("msc-ase", "msc-cs-f", "msc-it", "msc-bda", "msc-ba", "msc-fbm")) {
            if (!programRepo.existsByProgramSlugAndProgramLevelAndIsDeletedFalse(slug, "POSTGRADUATE")) return true;
        }

        for (String slug : List.of("academic-calendar", "program-specification", "important-contact-details", "time-table", "assessment-schedule", "lms-login-details", "mitigating-circumstances-form")) {
            if (!foundationRepo.existsByCategorySlugAndIsDeletedFalse(slug)) return true;
        }

        if (sruRepo.findRootCategory().isEmpty()) return true;
        if (!sruRepo.existsByCategorySlugAndIsDeletedFalse("help-desk-video-series")) return true;

        for (String slug : List.of("participation-at-conferences", "participation-at-competitions", "code-of-conduct", "club-policy", "it-policy")) {
            if (!policyRepo.existsByPolicySlugAndIsDeletedFalse(slug)) return true;
        }

        for (String slug : List.of("uow-late-mitigation-circumstances-form", "uow-mitigating-circumstances-form", "uow-self-certification-claim-form", "rgu-coursework-extension-form-self-certification", "rgu-deferral-request-form-self-certification")) {
            if (!mitigationRepo.existsByFormSlugAndIsDeletedFalse(slug)) return true;
        }

        for (String slug : List.of("soc", "common-info", "mail-groups", "doc-arch", "contacts")) {
            if (!staffRepo.existsByCategorySlugAndIsDeletedFalse(slug)) return true;
        }

        for (String slug : List.of("course-details", "houses", "students-union", "clubs-and-societies")) {
            if (!infoRepo.existsByCategorySlugAndIsDeletedFalse(slug)) return true;
        }

        return false;
    }

    private void clearAllIntranetData() {
        scheduleRepo.deleteAll();
        scheduleRepo.flush();
        calendarRepo.deleteAll();
        calendarRepo.flush();
        programRepo.deleteAll();
        programRepo.flush();
        foundationRepo.deleteAll();
        foundationRepo.flush();
        sruRepo.deleteAll();
        sruRepo.flush();
        policyRepo.deleteAll();
        policyRepo.flush();
        mitigationRepo.deleteAll();
        mitigationRepo.flush();
        complaintRepo.deleteAll();
        complaintRepo.flush();
        staffRepo.deleteAll();
        staffRepo.flush();
        infoRepo.deleteAll();
        infoRepo.flush();
        log.info("Cleared all intranet content tables for re-seeding.");
    }

    // ── Schedules ───────────────────────────────────────────────────
    private void seedScheduleCategories() {
        if (!scheduleRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc().isEmpty()) return;
        log.info("Seeding schedule categories...");

        ScheduleCategory orientation = ScheduleCategory.builder()
                .categoryName("Orientation").categorySlug("orientation")
                .description("Orientation schedules for new students including welcome events and campus tours")
                .build();
        orientation.addEvent(ScheduleEvent.builder().eventName("New Student Welcome Ceremony").description("Welcome ceremony for all new undergraduate and postgraduate students").startDate("2026-02-10").endDate("2026-02-10").venue("IIT Main Auditorium").eventType("CEREMONY").isEventActive(true).build());
        orientation.addEvent(ScheduleEvent.builder().eventName("Campus Tour - Day 1").description("Guided tour of IIT campus facilities including labs, library, and sports complex").startDate("2026-02-11").endDate("2026-02-11").venue("IIT Main Campus").eventType("TOUR").isEventActive(true).build());
        orientation.addEvent(ScheduleEvent.builder().eventName("IT Systems Orientation").description("Introduction to LMS, student portal, email setup, and Wi-Fi access").startDate("2026-02-12").endDate("2026-02-12").venue("Lab 1 & Lab 2").eventType("WORKSHOP").isEventActive(true).build());
        orientation.addEvent(ScheduleEvent.builder().eventName("Academic Policies Briefing").description("Overview of academic policies, assessment procedures, and student support services").startDate("2026-02-13").endDate("2026-02-13").venue("Lecture Hall A").eventType("BRIEFING").isEventActive(true).build());
        orientation.addEvent(ScheduleEvent.builder().eventName("Club Fair & Networking").description("Meet student clubs and societies, sign up for activities").startDate("2026-02-14").endDate("2026-02-14").venue("IIT Sports Complex").eventType("SOCIAL").isEventActive(true).build());
        scheduleRepo.save(orientation);

        ScheduleCategory temporary = ScheduleCategory.builder()
                .categoryName("Temporary").categorySlug("temporary")
                .description("Temporary schedule changes, room changes, and one-time scheduling updates")
                .build();
        temporary.addEvent(ScheduleEvent.builder().eventName("Room Change - CS5001 Database Systems").description("CS5001 moved from Lab 2 to Lab 4 due to equipment maintenance").startDate("2026-03-04").endDate("2026-03-04").venue("Lab 4 (was Lab 2)").eventType("ROOM_CHANGE").isEventActive(true).build());
        temporary.addEvent(ScheduleEvent.builder().eventName("Cancelled - FP004 Introduction to Business").description("Lecture cancelled due to lecturer illness. Replacement session on Friday").startDate("2026-03-03").endDate("2026-03-03").venue("N/A").eventType("CANCELLATION").isEventActive(true).build());
        temporary.addEvent(ScheduleEvent.builder().eventName("Extra Tutorial - AI5001 Machine Learning").description("Additional revision tutorial before mid-term assessment").startDate("2026-03-07").endDate("2026-03-07").venue("Tutorial Room 3").eventType("EXTRA_SESSION").isEventActive(true).build());
        scheduleRepo.save(temporary);

        ScheduleCategory assessments = ScheduleCategory.builder()
                .categoryName("Assessments").categorySlug("assessments")
                .description("Assessment and examination schedules across all programmes")
                .build();
        assessments.addEvent(ScheduleEvent.builder().eventName("AI4002 Programming Fundamentals - Coursework 1 Deadline").description("Submit via LMS by 23:59. Late penalties apply.").startDate("2026-03-14").endDate("2026-03-14").venue("Online (LMS)").eventType("COURSEWORK_DEADLINE").isEventActive(true).build());
        assessments.addEvent(ScheduleEvent.builder().eventName("CS4001 Introduction to CS - Mid-Term Test").description("Written test covering weeks 1-6 material. 1.5 hours.").startDate("2026-03-17").endDate("2026-03-17").venue("Exam Hall A").eventType("EXAM").isEventActive(true).build());
        assessments.addEvent(ScheduleEvent.builder().eventName("SE4001 Intro to SE - Group Presentation").description("Group presentations for SDGP project milestone 1").startDate("2026-03-20").endDate("2026-03-21").venue("Seminar Room 1").eventType("PRESENTATION").isEventActive(true).build());
        assessments.addEvent(ScheduleEvent.builder().eventName("BDA4001 Intro to Data Analytics - Lab Test").description("Practical lab test on Python data analysis. 2 hours.").startDate("2026-03-24").endDate("2026-03-24").venue("Lab 3").eventType("LAB_TEST").isEventActive(true).build());
        assessments.addEvent(ScheduleEvent.builder().eventName("Semester 2 Final Examination Period").description("Final examinations for all semester 2 modules").startDate("2026-06-01").endDate("2026-06-19").venue("Exam Hall A & B").eventType("EXAM_PERIOD").isEventActive(true).build());
        scheduleRepo.save(assessments);

        ScheduleCategory annualEvents = ScheduleCategory.builder()
                .categoryName("Annual Events").categorySlug("annual-events")
                .description("Annual institutional events, ceremonies, and important dates")
                .build();
        annualEvents.addEvent(ScheduleEvent.builder().eventName("IIT Hackathon 2026").description("Annual 24-hour hackathon open to all IIT students. Cash prizes for top 3 teams.").startDate("2026-03-15").endDate("2026-03-16").venue("IIT Main Auditorium & Labs").eventType("COMPETITION").isEventActive(true).build());
        annualEvents.addEvent(ScheduleEvent.builder().eventName("Career Fair 2026").description("Annual career fair with 50+ top IT and business companies in Sri Lanka").startDate("2026-04-10").endDate("2026-04-10").venue("IIT Sports Complex").eventType("CAREER").isEventActive(true).build());
        annualEvents.addEvent(ScheduleEvent.builder().eventName("IIT Sports Day").description("Inter-house sports competition. Track events, cricket, badminton, and football.").startDate("2026-05-02").endDate("2026-05-03").venue("IIT Sports Complex").eventType("SPORTS").isEventActive(true).build());
        annualEvents.addEvent(ScheduleEvent.builder().eventName("Annual Awards Ceremony").description("Recognition of academic excellence, club achievements, and outstanding contributions").startDate("2026-07-12").endDate("2026-07-12").venue("IIT Main Auditorium").eventType("CEREMONY").isEventActive(true).build());
        annualEvents.addEvent(ScheduleEvent.builder().eventName("Freshers' Welcome 2026").description("Welcome party for new intake students with entertainment and food").startDate("2026-09-20").endDate("2026-09-20").venue("IIT Sports Complex").eventType("SOCIAL").isEventActive(true).build());
        annualEvents.addEvent(ScheduleEvent.builder().eventName("IIT Convocation 2026").description("Graduation ceremony for all completing students").startDate("2026-11-15").endDate("2026-11-15").venue("BMICH, Colombo").eventType("CEREMONY").isEventActive(true).build());
        scheduleRepo.save(annualEvents);

        log.info("Seeded 4 schedule categories with events.");
    }

    // ── Student Complaints ──────────────────────────────────────────
    private void seedComplaints() {
        if (!complaintRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc().isEmpty()) return;
        log.info("Seeding student complaint categories...");

        complaintRepo.save(StudentComplaintCategory.builder()
                .categoryName("Academic Course Delivery").categorySlug("academic-course-delivery")
                .description("Submit complaints regarding academic course delivery including lecture quality, module content, assessment fairness, lecturer availability, and academic support.")
                .formUrl("https://forms.example.com/academic-course-delivery")
                .contactEmail("complaints.academic@iit.ac.lk").contactPhone("+94 11 234 5678")
                .instructions("Please describe your complaint in detail. Include the module code, lecturer name, and specific dates if applicable. All complaints are handled confidentially.")
                .responseTimeBusinessDays(5).build());

        complaintRepo.save(StudentComplaintCategory.builder()
                .categoryName("Facility and Support System").categorySlug("facility-and-support-system")
                .description("Submit complaints regarding campus facilities, IT infrastructure, library services, lab equipment, Wi-Fi, student support systems, and general campus maintenance.")
                .formUrl("https://forms.example.com/facility-support")
                .contactEmail("complaints.facility@iit.ac.lk").contactPhone("+94 11 234 5679")
                .instructions("Please specify the facility/service involved, the issue encountered, and the location. Attach photos if applicable.")
                .responseTimeBusinessDays(3).build());

        log.info("Seeded 2 student complaint categories.");
    }

    // ── Academic Calendars ──────────────────────────────────────────
    private void seedAcademicCalendars() {
        if (!calendarRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByUniversityNameAsc().isEmpty()) return;
        log.info("Seeding academic calendars...");

        AcademicCalendar uow = AcademicCalendar.builder().universityName("University of Wolverhampton (UoW)").universitySlug("uow").academicYear("2025/2026").calendarFileUrl("https://storage.example.com/calendars/uow-2025-2026.pdf").build();
        uow.addEvent(CalendarEvent.builder().eventName("Semester 1 Start").startDate("2025-09-15").endDate("2025-09-15").eventType("SEMESTER_START").build());
        uow.addEvent(CalendarEvent.builder().eventName("Mid-Semester Break").startDate("2025-11-03").endDate("2025-11-09").eventType("BREAK").build());
        uow.addEvent(CalendarEvent.builder().eventName("Semester 1 Examinations").startDate("2026-01-12").endDate("2026-01-30").eventType("EXAM_PERIOD").build());
        uow.addEvent(CalendarEvent.builder().eventName("Semester 2 Start").startDate("2026-02-10").endDate("2026-02-10").eventType("SEMESTER_START").build());
        uow.addEvent(CalendarEvent.builder().eventName("Semester 2 Examinations").startDate("2026-06-01").endDate("2026-06-19").eventType("EXAM_PERIOD").build());
        calendarRepo.save(uow);

        AcademicCalendar rgu = AcademicCalendar.builder().universityName("Robert Gordon University (RGU)").universitySlug("rgu").academicYear("2025/2026").calendarFileUrl("https://storage.example.com/calendars/rgu-2025-2026.pdf").build();
        rgu.addEvent(CalendarEvent.builder().eventName("Trimester 1 Start").startDate("2025-09-22").endDate("2025-09-22").eventType("SEMESTER_START").build());
        rgu.addEvent(CalendarEvent.builder().eventName("Winter Break").startDate("2025-12-20").endDate("2026-01-05").eventType("BREAK").build());
        rgu.addEvent(CalendarEvent.builder().eventName("Trimester 1 Examinations").startDate("2026-01-19").endDate("2026-02-06").eventType("EXAM_PERIOD").build());
        rgu.addEvent(CalendarEvent.builder().eventName("Trimester 2 Start").startDate("2026-02-17").endDate("2026-02-17").eventType("SEMESTER_START").build());
        rgu.addEvent(CalendarEvent.builder().eventName("Trimester 2 Examinations").startDate("2026-05-25").endDate("2026-06-12").eventType("EXAM_PERIOD").build());
        calendarRepo.save(rgu);

        log.info("Seeded 2 academic calendars with events.");
    }

    // ── Undergraduate Programs ──────────────────────────────────────
    private void seedUndergraduatePrograms() {
        if (!programRepo.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("UNDERGRADUATE").isEmpty())
            return;
        log.info("Seeding undergraduate programs...");

        saveProgram("BSC_AI_DS", "BSc (Hons) Artificial Intelligence and Data Science", "bsc-ai-ds", "University of Wolverhampton", "3 years", 360, "UNDERGRADUATE",
                "This programme provides a comprehensive foundation in Artificial Intelligence and Data Science, covering machine learning, deep learning, data mining, statistical analysis, and practical AI applications.",
                "Completion of IIT Foundation Programme or equivalent qualification. Minimum 3 A/L passes including Mathematics.",
                List.of("AI Engineer", "Data Scientist", "Machine Learning Engineer", "Business Intelligence Analyst", "Research Scientist"),
                List.of(mod(1, 1, "AI4001", "Introduction to Artificial Intelligence", 20), mod(1, 1, "AI4002", "Programming Fundamentals with Python", 20), mod(1, 2, "AI4003", "Data Structures and Algorithms", 20), mod(2, 1, "AI5001", "Machine Learning", 20), mod(3, 1, "AI6001", "Deep Learning and Neural Networks", 20)),
                "https://storage.example.com/specs/bsc-ai-ds-spec.pdf", "https://storage.example.com/handbooks/bsc-ai-ds-handbook.pdf");

        saveProgram("BSC_CS", "BSc (Hons) Computer Science", "bsc-cs", "University of Wolverhampton", "3 years", 360, "UNDERGRADUATE",
                "A comprehensive programme covering core computer science principles, software development, and emerging technologies.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes including Mathematics.",
                List.of("Software Developer", "Systems Architect", "DevOps Engineer", "Technical Consultant"),
                List.of(mod(1, 1, "CS4001", "Programming Fundamentals", 20), mod(1, 1, "CS4002", "Computer Architecture", 20), mod(1, 1, "CS4003", "Discrete Mathematics", 20), mod(1, 2, "CS4004", "Object-Oriented Programming", 20), mod(1, 2, "CS4005", "Databases", 20), mod(1, 2, "CS4006", "Web Technologies", 20)),
                "https://storage.example.com/specs/bsc-cs-spec.pdf", "https://storage.example.com/handbooks/bsc-cs-handbook.pdf");

        saveProgram("BENG_SE", "BEng (Hons) Software Engineering", "beng-se", "University of Wolverhampton", "3 years", 360, "UNDERGRADUATE",
                "A practical engineering programme focused on designing, building, and maintaining large-scale software systems.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes including Mathematics.",
                List.of("Software Engineer", "Full-Stack Developer", "QA Engineer", "Technical Lead"),
                List.of(mod(1, 1, "SE4001", "Software Engineering Principles", 20), mod(1, 1, "SE4002", "Programming I", 20), mod(1, 2, "SE4003", "Requirements Engineering", 20), mod(1, 2, "SE4004", "Data Structures & Algorithms", 20)),
                "https://storage.example.com/specs/beng-se-spec.pdf", "https://storage.example.com/handbooks/beng-se-handbook.pdf");

        saveProgram("BSC_BDA", "BSc (Hons) Big Data Analytics", "bsc-bda", "Robert Gordon University", "3 years", 360, "UNDERGRADUATE",
                "Programme focused on large-scale data processing, analytics platforms, and data-driven decision making.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes.",
                List.of("Big Data Engineer", "Data Analyst", "BI Developer", "Analytics Consultant"),
                List.of(mod(1, 1, "BD4001", "Data Fundamentals", 20), mod(1, 2, "BD4002", "Analytics Programming", 20)),
                "https://storage.example.com/specs/bsc-bda-spec.pdf", "https://storage.example.com/handbooks/bsc-bda-handbook.pdf");

        saveProgram("BSC_BIS", "BSc (Hons) Business Information Systems", "bsc-bis", "Robert Gordon University", "3 years", 360, "UNDERGRADUATE",
                "Bridging business strategy and IT systems, this programme prepares graduates for roles at the intersection of technology and management.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes.",
                List.of("Business Analyst", "IT Consultant", "Systems Analyst", "Project Manager"),
                List.of(mod(1, 1, "BI4001", "Business Information Systems", 20), mod(1, 2, "BI4002", "Systems Analysis & Design", 20)),
                "https://storage.example.com/specs/bsc-bis-spec.pdf", "https://storage.example.com/handbooks/bsc-bis-handbook.pdf");

        saveProgram("BA_BM", "BA (Hons) Business Management", "ba-bm", "Robert Gordon University", "3 years", 360, "UNDERGRADUATE",
                "A programme that develops critical thinking, leadership, and management skills for the modern business environment.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes.",
                List.of("Management Trainee", "Operations Manager", "HR Manager", "Entrepreneur"),
                List.of(mod(1, 1, "BM4001", "Principles of Management", 20), mod(1, 2, "BM4002", "Organizational Behaviour", 20)),
                "https://storage.example.com/specs/ba-bm-spec.pdf", "https://storage.example.com/handbooks/ba-bm-handbook.pdf");

        saveProgram("BSC_BC", "BSc (Hons) Business Computing", "bsc-bc", "Robert Gordon University", "3 years", 360, "UNDERGRADUATE",
                "Combining computing fundamentals with business acumen, this programme produces technology-savvy business professionals.",
                "Completion of IIT Foundation Programme or equivalent. Minimum 3 A/L passes.",
                List.of("IT Manager", "Business Systems Developer", "Technology Consultant", "Digital Transformation Lead"),
                List.of(mod(1, 1, "BC4001", "Computing Fundamentals", 20), mod(1, 2, "BC4002", "Business Process Modelling", 20)),
                "https://storage.example.com/specs/bsc-bc-spec.pdf", "https://storage.example.com/handbooks/bsc-bc-handbook.pdf");

        log.info("Seeded 7 undergraduate programs.");
    }

    // ── Postgraduate Programs ───────────────────────────────────────
    private void seedPostgraduatePrograms() {
        if (!programRepo.findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc("POSTGRADUATE").isEmpty())
            return;
        log.info("Seeding postgraduate programs...");

        saveProgram("MSC_ASE", "MSc Advanced Software Engineering", "msc-ase", "University of Wolverhampton", "1 year", 180, "POSTGRADUATE",
                "An advanced programme for professionals seeking to deepen their software engineering expertise.", "Bachelor's degree in Computing or related field. IELTS 6.5.",
                List.of("Senior Software Engineer", "Solutions Architect", "Technical Lead", "CTO"),
                List.of(mod(null, 1, "ASE7001", "Advanced Software Design", 20), mod(null, 1, "ASE7002", "Cloud Computing", 20), mod(null, 2, "ASE7003", "DevOps & CI/CD", 20), mod(null, 2, "ASE7004", "Dissertation", 60)),
                "https://storage.example.com/specs/msc-ase-spec.pdf", "https://storage.example.com/handbooks/msc-ase-handbook.pdf");

        saveProgram("MSC_CS_F", "MSc Cyber Security and Forensics", "msc-cs-f", "University of Wolverhampton", "1 year", 180, "POSTGRADUATE",
                "Specialist programme in cyber security, digital forensics, and ethical hacking.", "Bachelor's degree in Computing or related field. IELTS 6.5.",
                List.of("Cyber Security Analyst", "Penetration Tester", "Security Consultant", "CISO"),
                List.of(mod(null, 1, "CSF7001", "Network Security", 20), mod(null, 1, "CSF7002", "Digital Forensics", 20), mod(null, 2, "CSF7003", "Ethical Hacking", 20), mod(null, 2, "CSF7004", "Dissertation", 60)),
                "https://storage.example.com/specs/msc-cs-f-spec.pdf", "https://storage.example.com/handbooks/msc-cs-f-handbook.pdf");

        saveProgram("MSC_IT", "MSc Information Technology", "msc-it", "University of Wolverhampton", "1 year", 180, "POSTGRADUATE",
                "A broad programme covering modern IT management, systems, and infrastructure.", "Bachelor's degree. IELTS 6.5.",
                List.of("IT Manager", "Systems Administrator", "IT Consultant", "Enterprise Architect"),
                List.of(mod(null, 1, "IT7001", "IT Strategy & Governance", 20), mod(null, 2, "IT7002", "Cloud Infrastructure", 20)),
                "https://storage.example.com/specs/msc-it-spec.pdf", "https://storage.example.com/handbooks/msc-it-handbook.pdf");

        saveProgram("MSC_BDA", "MSc Big Data Analytics", "msc-bda", "Robert Gordon University", "1 year", 180, "POSTGRADUATE",
                "Advanced programme in big data technologies, analytics platforms, and data science.", "Bachelor's degree in Computing, Mathematics, or related field. IELTS 6.5.",
                List.of("Senior Data Engineer", "Lead Data Scientist", "Analytics Director"),
                List.of(mod(null, 1, "BDA7001", "Advanced Data Mining", 20), mod(null, 2, "BDA7002", "Scalable Data Systems", 20)),
                "https://storage.example.com/specs/msc-bda-spec.pdf", "https://storage.example.com/handbooks/msc-bda-handbook.pdf");

        saveProgram("MSC_BA", "MSc Business Analytics", "msc-ba", "Robert Gordon University", "1 year", 180, "POSTGRADUATE",
                "Programme combining business intelligence with advanced analytical methods.", "Bachelor's degree. IELTS 6.5.",
                List.of("Business Intelligence Manager", "Analytics Consultant", "Strategy Analyst"),
                List.of(mod(null, 1, "BA7001", "Predictive Analytics", 20), mod(null, 2, "BA7002", "Business Intelligence", 20)),
                "https://storage.example.com/specs/msc-ba-spec.pdf", "https://storage.example.com/handbooks/msc-ba-handbook.pdf");

        saveProgram("MSC_FBM", "MSc Finance and Business Management", "msc-fbm", "Robert Gordon University", "1 year", 180, "POSTGRADUATE",
                "A programme for those aspiring to leadership roles in finance and business management.", "Bachelor's degree. IELTS 6.5.",
                List.of("Financial Analyst", "Finance Manager", "Business Development Manager"),
                List.of(mod(null, 1, "FBM7001", "Corporate Finance", 20), mod(null, 2, "FBM7002", "Strategic Management", 20)),
                "https://storage.example.com/specs/msc-fbm-spec.pdf", "https://storage.example.com/handbooks/msc-fbm-handbook.pdf");

        log.info("Seeded 6 postgraduate programs.");
    }

    // ── Foundation Program Categories ────────────────────────────────
    private void seedFoundationCategories() {
        if (!foundationRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc().isEmpty()) return;
        log.info("Seeding foundation program categories...");

        foundationRepo.save(FoundationCategory.builder().categoryName("Academic Calendar").categorySlug("academic-calendar")
                .description("Foundation programme academic calendar for the current year")
                .academicYear("2025/2026").calendarFileUrl("https://storage.example.com/foundation/calendar-2025-2026.pdf")
                .contentJson(toJson(Map.of("events", List.of(
                        Map.of("eventName", "Foundation Semester 1 Start", "startDate", "2025-09-01", "endDate", "2025-09-01", "eventType", "SEMESTER_START"),
                        Map.of("eventName", "Mid-Semester Break", "startDate", "2025-11-10", "endDate", "2025-11-14", "eventType", "BREAK"),
                        Map.of("eventName", "Foundation Semester 1 Exams", "startDate", "2025-12-15", "endDate", "2025-12-22", "eventType", "EXAM_PERIOD"),
                        Map.of("eventName", "Foundation Semester 2 Start", "startDate", "2026-01-12", "endDate", "2026-01-12", "eventType", "SEMESTER_START")
                )))).build());

        foundationRepo.save(FoundationCategory.builder().categoryName("Program Specification").categorySlug("program-specification")
                .description("Foundation programme specification document")
                .programName("IIT Foundation Programme").duration("1 year (Full-time)").totalCredits(120)
                .specificationFileUrl("https://storage.example.com/foundation/programme-spec.pdf")
                .contentJson(toJson(Map.of("modules", List.of(
                        Map.of("moduleCode", "FND101", "moduleName", "Academic English", "credits", 20, "semester", 1),
                        Map.of("moduleCode", "FND102", "moduleName", "Mathematics for Computing", "credits", 20, "semester", 1),
                        Map.of("moduleCode", "FND103", "moduleName", "Introduction to Programming", "credits", 20, "semester", 1),
                        Map.of("moduleCode", "FND201", "moduleName", "Advanced English", "credits", 20, "semester", 2),
                        Map.of("moduleCode", "FND202", "moduleName", "Statistics", "credits", 20, "semester", 2),
                        Map.of("moduleCode", "FND203", "moduleName", "Web Development", "credits", 20, "semester", 2)
                )))).build());

        foundationRepo.save(FoundationCategory.builder().categoryName("Important Contact Details").categorySlug("important-contact-details")
                .description("Key contacts for the foundation programme")
                .contentJson(toJson(Map.of("contacts", List.of(
                        Map.of("role", "Programme Leader", "name", "Dr. Sarah Fernando", "email", "sarah.fernando@iit.ac.lk", "phone", "+94 11 234 5601", "officeHours", "Mon-Fri 9:00-17:00", "office", "Room 201, Main Building"),
                        Map.of("role", "Academic Coordinator", "name", "Mr. Kamal Perera", "email", "kamal.perera@iit.ac.lk", "phone", "+94 11 234 5602", "officeHours", "Mon-Fri 9:00-16:00", "office", "Room 202, Main Building"),
                        Map.of("role", "Student Support", "name", "Ms. Nimal Jayawardena", "email", "support.foundation@iit.ac.lk", "phone", "+94 11 234 5603", "officeHours", "Mon-Fri 8:30-17:30", "office", "Student Hub")
                )))).build());

        foundationRepo.save(FoundationCategory.builder().categoryName("Time Table").categorySlug("time-table")
                .description("Current semester timetable for foundation programme students")
                .semester("Semester 2").effectiveFrom("2026-01-12").timetableFileUrl("https://storage.example.com/foundation/timetable-s2.pdf")
                .contentJson(toJson(Map.of("schedule", List.of(
                        Map.of("day", "Monday", "slots", List.of(Map.of("startTime", "09:00", "endTime", "11:00", "moduleCode", "FND201", "moduleName", "Advanced English", "lecturer", "Ms. Amanda Silva", "venue", "LR-101"))),
                        Map.of("day", "Tuesday", "slots", List.of(Map.of("startTime", "09:00", "endTime", "11:00", "moduleCode", "FND202", "moduleName", "Statistics", "lecturer", "Dr. Kumara Dias", "venue", "LR-203"))),
                        Map.of("day", "Wednesday", "slots", List.of(Map.of("startTime", "13:00", "endTime", "15:00", "moduleCode", "FND203", "moduleName", "Web Development", "lecturer", "Mr. Ashan Rathnayake", "venue", "Lab-02")))
                )))).build());

        foundationRepo.save(FoundationCategory.builder().categoryName("Assessment Schedule").categorySlug("assessment-schedule")
                .description("Assessment schedule including deadlines and weightings")
                .scheduleFileUrl("https://storage.example.com/foundation/assessment-schedule.pdf")
                .contentJson(toJson(Map.of("assessments", List.of(
                        Map.of("moduleCode", "FND201", "moduleName", "Advanced English", "assessmentType", "Essay", "description", "Academic writing essay", "weightPercentage", 40, "submissionDeadline", "2026-03-15"),
                        Map.of("moduleCode", "FND202", "moduleName", "Statistics", "assessmentType", "Exam", "description", "Mid-semester exam", "weightPercentage", 50, "submissionDeadline", "2026-03-20"),
                        Map.of("moduleCode", "FND203", "moduleName", "Web Development", "assessmentType", "Project", "description", "Portfolio website", "weightPercentage", 60, "submissionDeadline", "2026-04-10")
                )))).build());

        foundationRepo.save(FoundationCategory.builder().categoryName("LMS Login Details").categorySlug("lms-login-details")
                .description("Learning Management System access information")
                .lmsName("Moodle").lmsUrl("https://lms.iit.ac.lk")
                .loginInstructions("Use your IIT student credentials to log in. First-time users must activate their account via the link sent to their registered email.")
                .usernameFormat("student_id@student.iit.ac.lk").defaultPasswordInfo("Your default password is your date of birth in DDMMYYYY format. You will be prompted to change it on first login.")
                .passwordResetUrl("https://lms.iit.ac.lk/login/forgot_password.php")
                .contentJson(toJson(Map.of("supportContact", Map.of("name", "IT Help Desk", "email", "it.help@iit.ac.lk", "phone", "+94 11 234 5600"))))
                .browserRequirements(new ArrayList<>(List.of("Google Chrome 90+", "Mozilla Firefox 88+", "Microsoft Edge 90+", "Safari 14+")))
                .additionalNotes("Ensure pop-ups are enabled for the LMS domain. Mobile access is available via the Moodle mobile app.")
                .build());

        foundationRepo.save(FoundationCategory.builder().categoryName("Mitigating Circumstances Form").categorySlug("mitigating-circumstances-form")
                .description("Mitigating circumstances claim form for foundation programme students")
                .formName("Foundation Programme Mitigating Circumstances Form")
                .formFileUrl("https://storage.example.com/foundation/mitigation-form.pdf")
                .submissionEmail("mitigation.foundation@iit.ac.lk").submissionDeadline("Within 5 working days of the affected assessment")
                .eligibleCircumstances(new ArrayList<>(List.of("Serious illness requiring medical treatment", "Bereavement of close family member", "Significant personal crisis")))
                .requiredEvidence(new ArrayList<>(List.of("Medical certificate", "Death certificate", "Police report", "Supporting letter")))
                .contentJson(toJson(Map.of("contactPerson", Map.of("name", "Foundation Academic Office", "email", "foundation.academic@iit.ac.lk", "phone", "+94 11 234 5610"))))
                .build());

        log.info("Seeded 7 foundation program categories.");
    }

    // ── SRU Categories ──────────────────────────────────────────────
    private void seedSruCategories() {
        if (sruRepo.findAll().stream().filter(s -> !Boolean.TRUE.equals(s.getIsDeleted())).count() >= 2) return;
        log.info("Seeding SRU categories...");

        sruRepo.save(SruCategory.builder().categorySlug("_root").unitName("Students Relations Unit")
                .description("The Students Relations Unit is dedicated to supporting students throughout their academic journey at IIT.")
                .location("Student Services Building, Ground Floor").email("sru@iit.ac.lk")
                .phone("+94 11 234 5690").officeHours("Mon-Fri 8:30 AM - 5:00 PM").build());

        SruCategory videos = SruCategory.builder().categorySlug("help-desk-video-series")
                .categoryName("Help Desk Video Series")
                .description("A collection of video tutorials created by the Students Relations Unit.")
                .build();
        videos.addVideo(SruVideo.builder().title("How to Register for Modules").description("Step-by-step guide on module registration.").videoUrl("https://storage.example.com/videos/module-registration.mp4").thumbnailUrl("https://storage.example.com/thumbnails/module-registration.jpg").duration("05:32").publishedDate("2026-01-15").build());
        videos.addVideo(SruVideo.builder().title("Accessing the LMS").description("How to log in and navigate the LMS.").videoUrl("https://storage.example.com/videos/lms-access.mp4").thumbnailUrl("https://storage.example.com/thumbnails/lms-access.jpg").duration("04:18").publishedDate("2026-01-15").build());
        videos.addVideo(SruVideo.builder().title("How to Submit an Assignment").description("Guide on submitting assignments.").videoUrl("https://storage.example.com/videos/assignment-submission.mp4").thumbnailUrl("https://storage.example.com/thumbnails/assignment-submission.jpg").duration("03:45").publishedDate("2026-01-20").build());
        videos.addVideo(SruVideo.builder().title("Requesting a Transcript").description("How to request official transcripts.").videoUrl("https://storage.example.com/videos/transcript-request.mp4").thumbnailUrl("https://storage.example.com/thumbnails/transcript-request.jpg").duration("03:10").publishedDate("2026-01-25").build());
        videos.addVideo(SruVideo.builder().title("Student ID Card Replacement").description("Process for replacing a lost ID card.").videoUrl("https://storage.example.com/videos/id-card-replacement.mp4").thumbnailUrl("https://storage.example.com/thumbnails/id-card-replacement.jpg").duration("02:55").publishedDate("2026-02-01").build());
        videos.addVideo(SruVideo.builder().title("Filing a Mitigating Circumstances Claim").description("How to complete and submit a mitigation form.").videoUrl("https://storage.example.com/videos/mitigation-claim.mp4").thumbnailUrl("https://storage.example.com/thumbnails/mitigation-claim.jpg").duration("06:20").publishedDate("2026-02-05").build());
        sruRepo.save(videos);

        log.info("Seeded 2 SRU categories with 6 videos.");
    }

    // ── Student Policies ────────────────────────────────────────────
    private void seedStudentPolicies() {
        if (!policyRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByPolicyNameAsc().isEmpty()) return;
        log.info("Seeding student policies...");

        StudentPolicy conferencePolicy = StudentPolicy.builder().policyName("Participation at Conferences").policySlug("participation-at-conferences").version("2.1").effectiveDate("2025-09-01")
                .description("Policy governing student participation at academic conferences and seminars.")
                .policyContent("Students are encouraged to participate in conferences. Prior approval must be obtained from the Head of Department...")
                .policyFileUrl("https://storage.example.com/policies/conference-participation.pdf")
                .contactName("Dr. Ravi Kumar").contactRole("Head of Student Affairs").contactEmail("student.affairs@iit.ac.lk").build();
        conferencePolicy.setKeyPoints(new ArrayList<>(List.of("Prior approval required from HOD", "IIT may provide partial funding", "Students must submit a report after attendance")));
        policyRepo.save(conferencePolicy);

        StudentPolicy competitionPolicy = StudentPolicy.builder().policyName("Participation at Competitions").policySlug("participation-at-competitions").version("1.5").effectiveDate("2025-09-01")
                .description("Policy for student participation in external competitions, hackathons, and coding challenges.")
                .policyContent("IIT supports student participation in competitions. Teams must register through the Student Affairs office...")
                .policyFileUrl("https://storage.example.com/policies/competition-participation.pdf")
                .contactName("Dr. Ravi Kumar").contactRole("Head of Student Affairs").contactEmail("student.affairs@iit.ac.lk").build();
        competitionPolicy.setKeyPoints(new ArrayList<>(List.of("Team registration required", "IIT branding guidelines must be followed", "Results must be reported")));
        policyRepo.save(competitionPolicy);

        StudentPolicy codeOfConduct = StudentPolicy.builder().policyName("Code of Conduct").policySlug("code-of-conduct").version("3.0").effectiveDate("2025-09-01")
                .description("The comprehensive student code of conduct governing behaviour on and off campus.")
                .policyContent("All students are expected to maintain the highest standards of academic integrity and personal conduct...")
                .policyFileUrl("https://storage.example.com/policies/code-of-conduct.pdf")
                .contactName("Dean of Students").contactRole("Dean of Student Affairs").contactEmail("dean.students@iit.ac.lk").build();
        codeOfConduct.setKeyPoints(new ArrayList<>(List.of("Academic integrity is paramount", "Respect for all community members", "Zero tolerance for plagiarism", "Dress code compliance required")));
        codeOfConduct.setDisciplinaryProcess(new ArrayList<>(List.of("Verbal warning", "Written warning", "Disciplinary hearing", "Suspension", "Expulsion")));
        policyRepo.save(codeOfConduct);

        StudentPolicy clubPolicy = StudentPolicy.builder().policyName("Club Policy").policySlug("club-policy").version("2.0").effectiveDate("2025-09-01")
                .description("Regulations for student clubs and societies, including formation, governance, and activities.")
                .policyFileUrl("https://storage.example.com/policies/club-policy.pdf")
                .contactName("Student Activities Coordinator").contactRole("Student Activities").contactEmail("clubs@iit.ac.lk").build();
        clubPolicy.setKeyPoints(new ArrayList<>(List.of("Minimum 15 members to form a club", "Faculty advisor required", "Annual budget submission mandatory")));
        policyRepo.save(clubPolicy);

        StudentPolicy itPolicy = StudentPolicy.builder().policyName("IT Policy").policySlug("it-policy").version("4.0").effectiveDate("2025-09-01")
                .description("IT usage policy covering network access, lab usage, software licensing, and data security.")
                .policyFileUrl("https://storage.example.com/policies/it-policy.pdf")
                .contactName("IT Security Officer").contactRole("IT Department").contactEmail("it.security@iit.ac.lk").build();
        itPolicy.setKeyPoints(new ArrayList<>(List.of("No unauthorized software installation", "VPN required for remote access", "Personal devices must be registered", "Data classification compliance")));
        policyRepo.save(itPolicy);

        log.info("Seeded 5 student policies.");
    }

    // ── Mitigation Forms ────────────────────────────────────────────
    private void seedMitigationForms() {
        if (!mitigationRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByFormNameAsc().isEmpty()) return;
        log.info("Seeding mitigation forms...");

        mitigationRepo.save(MitigationForm.builder().formName("UoW - Late Mitigation Circumstances Form").formSlug("uow-late-mitigation-circumstances-form").university("University of Wolverhampton")
                .description("This form should be completed if you are submitting a mitigating circumstances claim after the standard deadline.")
                .formFileUrl("https://storage.example.com/mitigation/uow-late-mitigation-form.pdf").submissionEmail("mitigation.uow@iit.ac.lk").submissionDeadline("As soon as possible after the original deadline")
                .processingTimeBusinessDays(10).contactName("Academic Registry - UoW Programmes").contactEmail("mitigation.uow@iit.ac.lk").contactPhone("+94 11 234 5691")
                .eligibleCircumstances(new ArrayList<>(List.of("Medical emergency requiring hospitalization", "Bereavement of immediate family member", "Serious accident or injury")))
                .requiredDocuments(new ArrayList<>(List.of("Completed Late Mitigation Form", "Original evidence", "Written explanation of delay"))).build());

        mitigationRepo.save(MitigationForm.builder().formName("UoW - Mitigating Circumstances Form").formSlug("uow-mitigating-circumstances-form").university("University of Wolverhampton")
                .description("Standard mitigating circumstances claim form for students on University of Wolverhampton programmes.")
                .formFileUrl("https://storage.example.com/mitigation/uow-mitigating-circumstances-form.pdf").submissionEmail("mitigation.uow@iit.ac.lk").submissionDeadline("Within 5 working days of the affected assessment deadline")
                .processingTimeBusinessDays(10).contactName("Academic Registry - UoW Programmes").contactEmail("mitigation.uow@iit.ac.lk").contactPhone("+94 11 234 5691")
                .eligibleCircumstances(new ArrayList<>(List.of("Serious illness or injury", "Bereavement", "Significant family or personal crisis", "Victim of crime")))
                .requiredDocuments(new ArrayList<>(List.of("Completed Mitigation Form", "Medical certificate or evidence", "Supporting statement"))).build());

        mitigationRepo.save(MitigationForm.builder().formName("UoW - Self Certification Claim Form").formSlug("uow-self-certification-claim-form").university("University of Wolverhampton")
                .description("Self-certification form for minor short-term circumstances (up to 5 working days).")
                .formFileUrl("https://storage.example.com/mitigation/uow-self-cert-form.pdf").submissionEmail("mitigation.uow@iit.ac.lk").submissionDeadline("Within 3 working days")
                .processingTimeBusinessDays(5).contactName("Academic Registry - UoW Programmes").contactEmail("mitigation.uow@iit.ac.lk").contactPhone("+94 11 234 5691")
                .extensionDuration("Up to 5 working days")
                .eligibleCircumstances(new ArrayList<>(List.of("Short-term illness (up to 5 days)", "Minor personal emergency")))
                .limitations(new ArrayList<>(List.of("Maximum one self-certification per semester", "Cannot be used for examinations"))).build());

        mitigationRepo.save(MitigationForm.builder().formName("RGU - Coursework Extension Form (Self-Certification)").formSlug("rgu-coursework-extension-form-self-certification").university("Robert Gordon University")
                .description("Self-certification form for coursework extension requests on RGU programmes.")
                .formFileUrl("https://storage.example.com/mitigation/rgu-extension-form.pdf").submissionEmail("mitigation.rgu@iit.ac.lk").submissionDeadline("Before the original submission deadline")
                .processingTimeBusinessDays(3).contactName("Academic Registry - RGU Programmes").contactEmail("mitigation.rgu@iit.ac.lk").contactPhone("+94 11 234 5692")
                .extensionDuration("Up to 7 calendar days")
                .eligibleCircumstances(new ArrayList<>(List.of("Short-term illness", "Minor personal circumstances")))
                .limitations(new ArrayList<>(List.of("One extension per module per trimester", "Not applicable for examinations or group work"))).build());

        mitigationRepo.save(MitigationForm.builder().formName("RGU - Deferral Request Form (Self-Certification)").formSlug("rgu-deferral-request-form-self-certification").university("Robert Gordon University")
                .description("Self-certification deferral request form for RGU programme assessments.")
                .formFileUrl("https://storage.example.com/mitigation/rgu-deferral-form.pdf").submissionEmail("mitigation.rgu@iit.ac.lk").submissionDeadline("Within 5 working days of the affected assessment")
                .processingTimeBusinessDays(5).contactName("Academic Registry - RGU Programmes").contactEmail("mitigation.rgu@iit.ac.lk").contactPhone("+94 11 234 5692")
                .deferralDetails("Assessment will be deferred to the next available assessment period. The deferred assessment will be treated as a first attempt.")
                .eligibleCircumstances(new ArrayList<>(List.of("Serious illness", "Bereavement", "Significant adverse personal circumstances")))
                .requiredDocuments(new ArrayList<>(List.of("Completed Deferral Form", "Medical evidence or other documentation")))
                .possibleOutcomes(new ArrayList<>(List.of("Deferral granted", "Deferral denied", "Partial deferral"))).build());

        log.info("Seeded 5 mitigation forms.");
    }

    // ── Staff Categories ────────────────────────────────────────────
    private void seedStaffCategories() {
        if (!staffRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc().isEmpty()) return;
        log.info("Seeding staff categories...");

        staffRepo.save(StaffCategory.builder().categoryName("SOC").categorySlug("soc").description("School of Computing staff information")
                .departmentFullName("School of Computing - Informatics Institute of Technology")
                .contentJson(toJson(Map.of("staffMembers", List.of(
                        Map.of("id", 1, "name", "Prof. Koliya Pulasinghe", "designation", "Dean / Senior Lecturer", "email", "koliya.p@iit.ac.lk", "phone", "+94 11 234 5710", "specialization", "Software Engineering, AI", "officeHours", "Mon-Thu 10:00-16:00"),
                        Map.of("id", 2, "name", "Dr. Dilshan Silva", "designation", "Senior Lecturer", "email", "dilshan.s@iit.ac.lk", "phone", "+94 11 234 5711", "specialization", "Data Science, Machine Learning", "officeHours", "Mon-Fri 09:00-15:00"),
                        Map.of("id", 3, "name", "Ms. Tharushi Weerasinghe", "designation", "Lecturer", "email", "tharushi.w@iit.ac.lk", "phone", "+94 11 234 5712", "specialization", "Web Technologies, UX Design"))))).build());

        staffRepo.save(StaffCategory.builder().categoryName("Common Info").categorySlug("common-info").description("Common staff information and general guidelines")
                .contentJson(toJson(Map.of("generalInfo", Map.of("institutionName", "Informatics Institute of Technology (IIT)", "mainAddress", "57, Ramakrishna Road, Colombo 06, Sri Lanka", "mainPhone", "+94 11 236 0212", "mainEmail", "info@iit.ac.lk", "website", "https://www.iit.ac.lk", "workingHours", "Monday - Friday: 8:30 AM - 5:30 PM", "academicYear", "2025/2026")))).build());

        staffRepo.save(StaffCategory.builder().categoryName("Mail Groups").categorySlug("mail-groups").description("Staff and department email groups")
                .contentJson(toJson(Map.of("mailGroups", List.of(
                        Map.of("groupName", "All Academic Staff", "email", "academic-staff@iit.ac.lk", "description", "All lecturers and academic staff", "accessLevel", "Staff Only"),
                        Map.of("groupName", "SOC Staff", "email", "soc-staff@iit.ac.lk", "description", "School of Computing staff", "accessLevel", "SOC Staff"),
                        Map.of("groupName", "Student Announcements", "email", "student-announce@iit.ac.lk", "description", "Official student announcements", "accessLevel", "Admin Only"))))).build());

        staffRepo.save(StaffCategory.builder().categoryName("Doc Arch").categorySlug("doc-arch").description("Document archive and templates")
                .contentJson(toJson(Map.of("documents", List.of(
                        Map.of("id", 1, "documentName", "Staff Handbook 2025-2026", "category", "Handbooks", "fileUrl", "https://storage.example.com/docs/staff-handbook.pdf", "fileType", "PDF", "fileSizeKb", 2048, "uploadedDate", "2025-08-15"),
                        Map.of("id", 2, "documentName", "Assessment Guidelines", "category", "Academic", "fileUrl", "https://storage.example.com/docs/assessment-guidelines.pdf", "fileType", "PDF", "fileSizeKb", 512, "uploadedDate", "2025-09-01"))))).build());

        staffRepo.save(StaffCategory.builder().categoryName("Contacts").categorySlug("contacts").description("Staff contact directory")
                .contentJson(toJson(Map.of(
                        "departments", List.of(Map.of("departmentName", "Academic Affairs", "headOfDepartment", "Prof. Koliya Pulasinghe", "email", "academic.affairs@iit.ac.lk", "phone", "+94 11 234 5720"), Map.of("departmentName", "Student Services", "headOfDepartment", "Ms. Chamari Perera", "email", "student.services@iit.ac.lk", "phone", "+94 11 234 5730")),
                        "emergencyContacts", Map.of("Security", "+94 11 234 5800", "Medical", "+94 11 234 5801", "Fire", "+94 11 234 5802")))).build());

        log.info("Seeded 5 staff categories.");
    }

    // ── Info Categories ─────────────────────────────────────────────
    private void seedInfoCategories() {
        if (!infoRepo.findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc().isEmpty()) return;
        log.info("Seeding info categories...");

        infoRepo.save(InfoCategory.builder().categoryName("Course Details").categorySlug("course-details").description("Overview of all academic programmes offered at IIT")
                .contentJson(toJson(Map.of(
                        "programmeCategories", List.of(
                                Map.of("category", "Undergraduate", "programmes", List.of(Map.of("programName", "BSc (Hons) AI and Data Science", "duration", "3 years", "awardingBody", "University of Wolverhampton", "intake", "September", "fee", "Contact admissions"), Map.of("programName", "BSc (Hons) Computer Science", "duration", "3 years", "awardingBody", "University of Wolverhampton", "intake", "September", "fee", "Contact admissions"))),
                                Map.of("category", "Postgraduate", "programmes", List.of(Map.of("programName", "MSc Advanced Software Engineering", "duration", "1 year", "awardingBody", "University of Wolverhampton", "intake", "September / January", "fee", "Contact admissions")))),
                        "admissionsContact", Map.of("email", "admissions@iit.ac.lk", "phone", "+94 11 236 0212", "whatsapp", "+94 77 123 4567")))).build());

        infoRepo.save(InfoCategory.builder().categoryName("Houses").categorySlug("houses").description("Student house system information and standings")
                .contentJson(toJson(Map.of("houses", List.of(
                        Map.of("id", 1, "houseName", "Phoenix", "color", "#FF4500", "motto", "Rise from the ashes", "description", "House of resilience and determination", "housemaster", "Dr. Dilshan Silva", "totalPoints", 2450, "rank", 1),
                        Map.of("id", 2, "houseName", "Titan", "color", "#4169E1", "motto", "Strength in unity", "description", "House of teamwork and collaboration", "housemaster", "Ms. Tharushi Weerasinghe", "totalPoints", 2380, "rank", 2),
                        Map.of("id", 3, "houseName", "Vortex", "color", "#32CD32", "motto", "Innovation unleashed", "description", "House of creativity and innovation", "housemaster", "Mr. Ashan Rathnayake", "totalPoints", 2290, "rank", 3),
                        Map.of("id", 4, "houseName", "Zenith", "color", "#FFD700", "motto", "Reach the peak", "description", "House of excellence and ambition", "housemaster", "Dr. Kumara Dias", "totalPoints", 2150, "rank", 4))))).build());

        infoRepo.save(InfoCategory.builder().categoryName("Students' Union").categorySlug("students-union").description("IIT Students' Union information and office bearers")
                .contentJson(toJson(Map.of(
                        "currentAcademicYear", "2025/2026", "office", "Room G05, Student Hub", "email", "su@iit.ac.lk", "phone", "+94 11 234 5750",
                        "socialMedia", Map.of("instagram", "@iit_su", "facebook", "IIT Students Union", "linkedin", "IIT Students Union"),
                        "currentOffice", Map.of("academicYear", "2025/2026", "officeBearers", List.of(
                                Map.of("position", "President", "name", "Kasun Rajapaksha", "email", "president.su@iit.ac.lk", "programme", "BSc Computer Science", "year", 3),
                                Map.of("position", "Vice President", "name", "Amaya Perera", "email", "vp.su@iit.ac.lk", "programme", "BSc AI and Data Science", "year", 3),
                                Map.of("position", "Secretary", "name", "Dineth Fernando", "email", "secretary.su@iit.ac.lk", "programme", "BEng Software Engineering", "year", 2),
                                Map.of("position", "Treasurer", "name", "Rashmi De Silva", "email", "treasurer.su@iit.ac.lk", "programme", "BSc Business Computing", "year", 2))),
                        "upcomingEvents", List.of(Map.of("eventName", "IIT Freshers' Night 2026", "date", "2026-03-15", "venue", "Main Auditorium", "description", "Annual welcome event for new students"), Map.of("eventName", "Hackathon 2026", "date", "2026-04-20", "venue", "Innovation Lab", "description", "24-hour coding competition"))))).build());

        infoRepo.save(InfoCategory.builder().categoryName("Clubs and Societies").categorySlug("clubs-and-societies").description("Student clubs and societies at IIT")
                .contentJson(toJson(Map.of(
                        "totalClubs", 8, "joinInstructions", "Visit the Students' Union office or fill out the online form at clubs.iit.ac.lk to join a club.",
                        "clubs", List.of(
                                Map.of("id", 1, "clubName", "IIT Computing Society", "clubCode", "IITCS", "category", "Academic", "description", "Premier computing and technology club", "president", "Nisal Gamage", "email", "cs.club@iit.ac.lk", "memberCount", 120, "isOpenForRegistration", true),
                                Map.of("id", 2, "clubName", "IEEE Student Branch", "clubCode", "IEEE-IIT", "category", "Professional", "description", "IEEE student chapter for networking and professional development", "president", "Sachini Pathirana", "email", "ieee@iit.ac.lk", "memberCount", 85, "isOpenForRegistration", true),
                                Map.of("id", 3, "clubName", "Toastmasters Club", "clubCode", "TM-IIT", "category", "Communication", "description", "Public speaking and leadership development", "president", "Kavinda Silva", "email", "toastmasters@iit.ac.lk", "memberCount", 45, "isOpenForRegistration", true),
                                Map.of("id", 4, "clubName", "IIT Sports Club", "clubCode", "IITSC", "category", "Sports", "description", "Organizes sports events and inter-university competitions", "president", "Tharindu Perera", "email", "sports@iit.ac.lk", "memberCount", 200, "isOpenForRegistration", true))))).build());

        log.info("Seeded 4 info categories.");
    }

    // ── Intranet helpers ────────────────────────────────────────────

    private void saveProgram(String code, String name, String slug, String uni, String duration,
                             int credits, String level, String desc, String entry,
                             List<String> careers, List<ProgramModule> modules,
                             String specUrl, String handbookUrl) {
        Program p = Program.builder()
                .programCode(code).programName(name).programSlug(slug)
                .awardingUniversity(uni).duration(duration).totalCredits(credits)
                .programLevel(level).description(desc).entryRequirements(entry)
                .careerProspects(new ArrayList<>(careers))
                .programSpecificationUrl(specUrl).handbookUrl(handbookUrl)
                .build();
        modules.forEach(p::addModule);
        programRepo.save(p);
    }

    // ── Event seeding ─────────────────────────────────────────────────
    private void createEvents() {
        if (eventRepository.count() == 0) {
            // Get existing users to assign as event creators
            Student student = studentRepository.findAll().stream().findFirst().orElse(null);
            Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);
            AcademicStaff lecturer = academicStaffRepository.findAll().stream().findFirst().orElse(null);

            if (student == null || admin == null || lecturer == null) {
                log.warn("Skipping event seeding — required users not found");
                return;
            }

            // 1. Published upcoming workshop (created by student)
            Event workshop = new Event();
            workshop.setTitle("Full-Stack Development Workshop");
            workshop.setDescription("A comprehensive hands-on workshop covering React, Spring Boot, and PostgreSQL. Learn to build production-ready applications from scratch with industry best practices.");
            workshop.setStartAt(LocalDateTime.now().plusDays(15).withHour(9).withMinute(0));
            workshop.setEndAt(LocalDateTime.now().plusDays(15).withHour(17).withMinute(0));
            workshop.setLocation("Colombo, Sri Lanka");
            workshop.setVenue("IIT Main Auditorium, Block A");
            workshop.setEventType(EventType.WORKSHOP);
            workshop.setStatus(EventStatus.PUBLISHED);
            workshop.setCreatedBy(student);
            workshop.setMaxAttendees(50);
            workshop.setViewCount(125L);
            workshop.setRegistrationLink("https://forms.google.com/workshop-2026");
            eventRepository.save(workshop);
            log.info("Created Event: {}", workshop.getTitle());

            // 2. Published upcoming seminar (created by lecturer)
            Event seminar = new Event();
            seminar.setTitle("AI & Machine Learning in Healthcare");
            seminar.setDescription("Join Dr. James Smith for an insightful seminar on how artificial intelligence and machine learning are revolutionizing healthcare diagnostics and treatment planning.");
            seminar.setStartAt(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0));
            seminar.setEndAt(LocalDateTime.now().plusDays(7).withHour(16).withMinute(30));
            seminar.setLocation("Colombo, Sri Lanka");
            seminar.setVenue("Seminar Hall, Block B, Room 301");
            seminar.setEventType(EventType.SEMINAR);
            seminar.setStatus(EventStatus.PUBLISHED);
            seminar.setCreatedBy(lecturer);
            seminar.setMaxAttendees(200);
            seminar.setViewCount(340L);
            eventRepository.save(seminar);
            log.info("Created Event: {}", seminar.getTitle());

            // 3. Published upcoming hackathon (created by admin)
            Event hackathon = new Event();
            hackathon.setTitle("Nextora Hackathon 2026");
            hackathon.setDescription("48-hour coding challenge! Build innovative solutions using AI, IoT, or Blockchain. Amazing prizes for top 3 teams. Open to all university students.");
            hackathon.setStartAt(LocalDateTime.now().plusDays(30).withHour(8).withMinute(0));
            hackathon.setEndAt(LocalDateTime.now().plusDays(32).withHour(8).withMinute(0));
            hackathon.setLocation("Kandy, Sri Lanka");
            hackathon.setVenue("IIT Innovation Hub");
            hackathon.setEventType(EventType.HACKATHON);
            hackathon.setStatus(EventStatus.PUBLISHED);
            hackathon.setCreatedBy(admin);
            hackathon.setMaxAttendees(100);
            hackathon.setViewCount(510L);
            hackathon.setRegistrationLink("https://nextora-hack.dev/register");
            eventRepository.save(hackathon);
            log.info("Created Event: {}", hackathon.getTitle());

            // 4. Published upcoming sports event (created by student)
            Event sportsDay = new Event();
            sportsDay.setTitle("Inter-Faculty Sports Championship");
            sportsDay.setDescription("Annual inter-faculty sports competition featuring cricket, football, badminton, and athletics. Come support your faculty!");
            sportsDay.setStartAt(LocalDateTime.now().plusDays(20).withHour(7).withMinute(30));
            sportsDay.setEndAt(LocalDateTime.now().plusDays(20).withHour(18).withMinute(0));
            sportsDay.setLocation("Colombo, Sri Lanka");
            sportsDay.setVenue("University Sports Complex");
            sportsDay.setEventType(EventType.SPORTS);
            sportsDay.setStatus(EventStatus.PUBLISHED);
            sportsDay.setCreatedBy(student);
            sportsDay.setMaxAttendees(500);
            sportsDay.setViewCount(230L);
            eventRepository.save(sportsDay);
            log.info("Created Event: {}", sportsDay.getTitle());

            // 5. Published upcoming cultural event (created by admin)
            Event culturalNight = new Event();
            culturalNight.setTitle("Cultural Night - Unity in Diversity");
            culturalNight.setDescription("A celebration of Sri Lanka's diverse cultures through music, dance, drama, and food. Featuring performances from all student clubs and special guest artists.");
            culturalNight.setStartAt(LocalDateTime.now().plusDays(25).withHour(17).withMinute(0));
            culturalNight.setEndAt(LocalDateTime.now().plusDays(25).withHour(22).withMinute(0));
            culturalNight.setLocation("Colombo, Sri Lanka");
            culturalNight.setVenue("Main Auditorium & Outdoor Stage");
            culturalNight.setEventType(EventType.CULTURAL);
            culturalNight.setStatus(EventStatus.PUBLISHED);
            culturalNight.setCreatedBy(admin);
            culturalNight.setMaxAttendees(300);
            culturalNight.setViewCount(415L);
            eventRepository.save(culturalNight);
            log.info("Created Event: {}", culturalNight.getTitle());

            // 6. Published upcoming academic conference (created by lecturer)
            Event conference = new Event();
            conference.setTitle("International Research Symposium 2026");
            conference.setDescription("Presenting cutting-edge research in Computer Science, Data Science, and Cybersecurity. Keynote speakers from MIT and Stanford.");
            conference.setStartAt(LocalDateTime.now().plusDays(45).withHour(9).withMinute(0));
            conference.setEndAt(LocalDateTime.now().plusDays(46).withHour(17).withMinute(0));
            conference.setLocation("Galle, Sri Lanka");
            conference.setVenue("Galle International Convention Centre");
            conference.setEventType(EventType.ACADEMIC);
            conference.setStatus(EventStatus.PUBLISHED);
            conference.setCreatedBy(lecturer);
            conference.setMaxAttendees(150);
            conference.setViewCount(189L);
            conference.setRegistrationLink("https://iit-symposium.lk/register");
            eventRepository.save(conference);
            log.info("Created Event: {}", conference.getTitle());

            // 7. Draft event (created by student)
            Event draftEvent = new Event();
            draftEvent.setTitle("Python for Data Science - Beginner Bootcamp");
            draftEvent.setDescription("A 2-day bootcamp covering Python fundamentals, Pandas, NumPy, and basic ML with scikit-learn. Perfect for beginners!");
            draftEvent.setStartAt(LocalDateTime.now().plusDays(60).withHour(9).withMinute(0));
            draftEvent.setEndAt(LocalDateTime.now().plusDays(61).withHour(17).withMinute(0));
            draftEvent.setLocation("Colombo, Sri Lanka");
            draftEvent.setVenue("Computer Lab, Block C");
            draftEvent.setEventType(EventType.WORKSHOP);
            draftEvent.setStatus(EventStatus.DRAFT);
            draftEvent.setCreatedBy(student);
            draftEvent.setMaxAttendees(30);
            draftEvent.setViewCount(0L);
            eventRepository.save(draftEvent);
            log.info("Created Event (Draft): {}", draftEvent.getTitle());

            // 8. Cancelled event (created by admin)
            Event cancelledEvent = new Event();
            cancelledEvent.setTitle("Outdoor Movie Night");
            cancelledEvent.setDescription("An outdoor screening of a popular movie with popcorn and refreshments under the stars.");
            cancelledEvent.setStartAt(LocalDateTime.now().plusDays(5).withHour(18).withMinute(0));
            cancelledEvent.setEndAt(LocalDateTime.now().plusDays(5).withHour(21).withMinute(0));
            cancelledEvent.setLocation("Colombo, Sri Lanka");
            cancelledEvent.setVenue("University Lawn");
            cancelledEvent.setEventType(EventType.SOCIAL);
            cancelledEvent.setStatus(EventStatus.CANCELLED);
            cancelledEvent.setCreatedBy(admin);
            cancelledEvent.setMaxAttendees(200);
            cancelledEvent.setViewCount(78L);
            cancelledEvent.setCancellationReason("Venue unavailable due to maintenance work");
            cancelledEvent.setCancelledAt(LocalDateTime.now().minusDays(2));
            eventRepository.save(cancelledEvent);
            log.info("Created Event (Cancelled): {}", cancelledEvent.getTitle());

            // 9. Completed (past) event (created by lecturer)
            Event pastEvent = new Event();
            pastEvent.setTitle("Introduction to Cloud Computing");
            pastEvent.setDescription("A seminar covering AWS, Azure, and GCP fundamentals. Learn about cloud architecture, deployment, and best practices.");
            pastEvent.setStartAt(LocalDateTime.now().minusDays(10).withHour(10).withMinute(0));
            pastEvent.setEndAt(LocalDateTime.now().minusDays(10).withHour(12).withMinute(30));
            pastEvent.setLocation("Colombo, Sri Lanka");
            pastEvent.setVenue("Lecture Hall 2, Block A");
            pastEvent.setEventType(EventType.SEMINAR);
            pastEvent.setStatus(EventStatus.COMPLETED);
            pastEvent.setCreatedBy(lecturer);
            pastEvent.setMaxAttendees(100);
            pastEvent.setViewCount(290L);
            eventRepository.save(pastEvent);
            log.info("Created Event (Completed): {}", pastEvent.getTitle());

            // 10. Published social event with no attendee limit (created by student)
            Event socialEvent = new Event();
            socialEvent.setTitle("Freshers Welcome Party 2026");
            socialEvent.setDescription("Welcome our new batch of students! Join us for games, music, food, and fun. A great opportunity to meet your seniors and make new friends.");
            socialEvent.setStartAt(LocalDateTime.now().plusDays(10).withHour(16).withMinute(0));
            socialEvent.setEndAt(LocalDateTime.now().plusDays(10).withHour(21).withMinute(0));
            socialEvent.setLocation("Colombo, Sri Lanka");
            socialEvent.setVenue("Student Centre & Rooftop");
            socialEvent.setEventType(EventType.SOCIAL);
            socialEvent.setStatus(EventStatus.PUBLISHED);
            socialEvent.setCreatedBy(student);
            socialEvent.setViewCount(620L);
            eventRepository.save(socialEvent);
            log.info("Created Event: {}", socialEvent.getTitle());

            log.info("Created 10 seed events");

            // ── Event Registrations ──────────────────────────────────
            List<Student> students = studentRepository.findAll();
            List<Event> publishedEvents = eventRepository.findAll().stream()
                    .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                    .toList();

            for (Event event : publishedEvents) {
                int regCount = 0;
                for (Student s : students) {
                    if (regCount >= 3) break; // register up to 3 students per event
                    if (s.getId().equals(event.getCreatedBy().getId())) continue; // skip creator

                    EventRegistration reg = EventRegistration.builder()
                            .event(event)
                            .user(s)
                            .registeredAt(LocalDateTime.now().minusDays(regCount + 1))
                            .isCancelled(false)
                            .build();
                    eventRegistrationRepository.save(reg);
                    regCount++;
                }
            }

            // Add a cancelled registration for variety
            if (students.size() > 4 && !publishedEvents.isEmpty()) {
                EventRegistration cancelledReg = EventRegistration.builder()
                        .event(publishedEvents.get(0))
                        .user(students.get(4))
                        .registeredAt(LocalDateTime.now().minusDays(5))
                        .isCancelled(true)
                        .cancelledAt(LocalDateTime.now().minusDays(3))
                        .build();
                eventRegistrationRepository.save(cancelledReg);
            }

            log.info("Created event registrations for seed data");
        }
    }

    private ProgramModule mod(Integer year, int sem, String code, String name, int credits) {
        return ProgramModule.builder().year(year).semester(sem).moduleCode(code).moduleName(name).credits(credits).isCore(true).build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize seed data to JSON", e);
            return "{}";
        }
    }
}

