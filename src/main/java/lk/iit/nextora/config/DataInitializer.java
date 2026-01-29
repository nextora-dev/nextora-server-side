package lk.iit.nextora.config;

import lk.iit.nextora.common.enums.*;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.repository.*;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubMembership;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.voting.entity.*;
import lk.iit.nextora.module.voting.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
}
