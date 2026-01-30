package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.*;
import lk.iit.nextora.module.auth.dto.response.UserCreatedResponse;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.*;
import lk.iit.nextora.module.auth.service.AdminUserManagementService;
import lk.iit.nextora.module.auth.service.EmailService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserLookupService userLookupService;
    private final AuthMapper authMapper;
    private final UserResponseMapper userResponseMapper;
    private final SecurityService securityService;
    private final EmailService emailService;

    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Override
    public UserCreatedResponse createUser(AdminCreateUserRequest request) {
        validateAdminAccess();
        validateAllowedRole(request.getRole());

        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireFalse(userLookupService.emailExists(request.getEmail()), "Email already registered");

        log.info("Admin {} creating user: {} with role: {}",
                securityService.getCurrentUserEmail(), StringUtils.maskEmail(request.getEmail()), request.getRole());

        String tempPassword = generateTemporaryPassword();
        BaseUser user = mapToEntity(request);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);

        entityManager.persist(user);
        entityManager.flush();

        sendCredentialsEmail(user, tempPassword);

        log.info("User created successfully: {} - {}", StringUtils.maskEmail(user.getEmail()), user.getUserType());

        return UserCreatedResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .message("User created successfully. Credentials sent to " + user.getEmail())
                .roleSpecificData(userResponseMapper.extractRoleSpecificData(user))
                .build();
    }

    private void validateAdminAccess() {
        if (!securityService.isAdmin() && !securityService.isSuperAdmin()) {
            throw new BadRequestException("Only Admin or Super Admin can create users");
        }
    }

    private void validateAllowedRole(UserRole role) {
        Set<UserRole> allowedRoles = Set.of(UserRole.ROLE_STUDENT, UserRole.ROLE_ACADEMIC_STAFF, UserRole.ROLE_NON_ACADEMIC_STAFF);
        if (!allowedRoles.contains(role)) {
            throw new BadRequestException("Cannot create users with role: " + role);
        }
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        return password.toString();
    }

    private BaseUser mapToEntity(AdminCreateUserRequest request) {
        return switch (request.getRole()) {
            case ROLE_STUDENT -> mapToStudent((AdminCreateStudentRequest) request);
            case ROLE_ACADEMIC_STAFF -> mapToAcademicStaff((AdminCreateAcademicStaffRequest) request);
            case ROLE_NON_ACADEMIC_STAFF -> mapToNonAcademicStaff((AdminCreateNonAcademicStaffRequest) request);
            default -> throw new BadRequestException("Invalid role for user creation");
        };
    }

    private Student mapToStudent(AdminCreateStudentRequest request) {
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }

        Student student = new Student();
        student.setEmail(request.getEmail());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhoneNumber(request.getPhone());
        student.setStudentId(request.getStudentId());
        student.setBatch(request.getBatch());
        student.setProgram(request.getProgram());
        student.setFaculty(request.getFaculty());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setAddress(request.getAddress());
        student.setGuardianName(request.getGuardianName());
        student.setGuardianPhone(request.getGuardianPhone());

        Set<StudentRoleType> roleTypes = request.getEffectiveRoleTypes();
        student.setStudentRoleTypes(EnumSet.copyOf(roleTypes));

        if (roleTypes.contains(StudentRoleType.CLUB_MEMBER)) {
            student.setClubName(request.getClubName());
            student.setClubJoinDate(request.getClubJoinDate());
            student.setClubMembershipId(request.getClubMembershipId());
        }
        // Handle both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
        if (roleTypes.contains(StudentRoleType.KUPPI_STUDENT) || roleTypes.contains(StudentRoleType.SENIOR_KUPPI)) {
            student.setKuppiSubjects(request.getKuppiSubjects());
            student.setKuppiExperienceLevel(request.getKuppiExperienceLevel());
            student.setKuppiAvailability(request.getKuppiAvailability());
            student.setKuppiSessionsCompleted(0);
            student.setKuppiRating(0.0);
        }
        if (roleTypes.contains(StudentRoleType.BATCH_REP)) {
            student.setBatchRepYear(request.getBatchRepYear());
            student.setBatchRepSemester(request.getBatchRepSemester());
            student.setBatchRepElectedDate(request.getBatchRepElectedDate());
            student.setBatchRepResponsibilities(request.getBatchRepResponsibilities());
        }

        return student;
    }

    private AcademicStaff mapToAcademicStaff(AdminCreateAcademicStaffRequest request) {
        if (academicStaffRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        AcademicStaff staff = new AcademicStaff();
        staff.setEmail(request.getEmail());
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhone());
        staff.setEmployeeId(request.getEmployeeId());
        staff.setDepartment(request.getDepartment());
        staff.setFaculty(request.getFaculty());
        staff.setPosition(request.getPosition());
        staff.setOfficeLocation(request.getOfficeLocation());
        staff.setJoinDate(request.getJoinDate());
        staff.setResponsibilities(request.getResponsibilities());
        staff.setDesignation(request.getDesignation());
        staff.setSpecialization(request.getSpecialization());
        staff.setQualifications(request.getQualifications());
        staff.setBio(request.getBio());

        return staff;
    }

    private NonAcademicStaff mapToNonAcademicStaff(AdminCreateNonAcademicStaffRequest request) {
        if (nonAcademicStaffRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        NonAcademicStaff staff = new NonAcademicStaff();
        staff.setEmail(request.getEmail());
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhone());
        staff.setEmployeeId(request.getEmployeeId());
        staff.setDepartment(request.getDepartment());
        staff.setPosition(request.getPosition());
        staff.setWorkLocation(request.getOfficeLocation());
        staff.setJoinDate(request.getJoinDate());

        return staff;
    }

    private void sendCredentialsEmail(BaseUser user, String tempPassword) {
        try {
            emailService.sendAccountCredentialsEmail(user.getEmail(), user.getFirstName(), user.getEmail(), tempPassword);
            log.info("Credentials email sent to: {}", StringUtils.maskEmail(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send credentials email to: {}", StringUtils.maskEmail(user.getEmail()), e);
        }
    }
}
