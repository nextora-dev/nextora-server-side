package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.*;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.*;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lk.iit.nextora.module.auth.service.EmailVerificationService;
import lk.iit.nextora.module.auth.service.LoginAttemptService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserLookupService userLookupService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final UserResponseMapper userResponseMapper;
    private final EmailVerificationService emailVerificationService;
    private final LoginAttemptService loginAttemptService;

    // Repositories
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final AdminRepository adminRepository;
    private final SuperAdminRepository superAdminRepository;

    // ==================== REGISTRATION ====================

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Validate common input
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireMinLength(request.getPassword(), 8, "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");
        ValidationUtils.requireNonBlank(request.getFirstName(), "First name");
        ValidationUtils.requireNonBlank(request.getLastName(), "Last name");

        log.info("Registration attempt for: {} as role: {}",
                StringUtils.maskEmail(request.getEmail()), request.getRole());

        // Validate passwords match
        ValidationUtils.requireEquals(
                request.getPassword(),
                request.getConfirmPassword(),
                "Passwords do not match"
        );

        // Check if email already exists
        ValidationUtils.requireFalse(
                userLookupService.emailExists(request.getEmail()),
                "Email already registered"
        );

        // Validate role-specific fields and map to entity
        BaseUser user = validateAndMapToEntity(request);

        // Set common fields
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Set status based on role
        boolean requiresEmailVerification = !isAdminRole(request.getRole());
        if (requiresEmailVerification) {
            user.setStatus(UserStatus.PENDING_VERIFICATION);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        // Persist user
        entityManager.persist(user);
        entityManager.flush();

        // Post-registration logging
        logPostRegistration(user);

        // Send verification email for non-admin users
        if (requiresEmailVerification) {
            emailVerificationService.sendVerificationEmail(user);
            log.info("Verification email sent to: {}", StringUtils.maskEmail(user.getEmail()));

            return authMapper.toPendingVerificationResponse(
                    user,
                    "Registration successful. Please check your email to verify your account."
            );
        }

        // Generate tokens only for admin users
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {} - {}",
                StringUtils.maskEmail(user.getEmail()), user.getUserType());

        return authMapper.toAuthResponseWithRoleData(
                user,
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpiryDate(),
                userResponseMapper.extractRoleSpecificData(user)
        );
    }

    // ==================== LOGIN ====================

    @Override
    public AuthResponse login(LoginRequest request) {
        // Validate input
        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");

        log.info("Login attempt for: {} as role: {}", StringUtils.maskEmail(request.getEmail()), request.getRole());

        // Check if user exists
        BaseUser user = userLookupService
                .findUserByEmailAndRole(request.getEmail(), request.getRole())
                .orElse(null);

        if (user != null) {
            // Check if email is pending verification
            if (UserStatus.PENDING_VERIFICATION.equals(user.getStatus())) {
                log.warn("Login attempt for unverified account: {}", StringUtils.maskEmail(request.getEmail()));
                throw new BadRequestException(
                        "Your email is not verified. Please check your email and click the verification link to activate your account."
                );
            }

            // Check if account is suspended
            if (UserStatus.SUSPENDED.equals(user.getStatus())) {
                LocalDateTime lastFailedAt = user.getLastFailedLoginAt();
                boolean isNewDay = lastFailedAt == null ||
                        !lastFailedAt.toLocalDate().equals(LocalDateTime.now().toLocalDate());

                if (isNewDay) {
                    loginAttemptService.resetFailedAttempts(user.getId());
                    user = userLookupService.findUserByEmailAndRole(request.getEmail(), request.getRole())
                            .orElse(null);
                    log.info("Account auto-unlocked for new day: {}", StringUtils.maskEmail(request.getEmail()));
                } else {
                    log.warn("Login attempt for suspended account: {}", StringUtils.maskEmail(request.getEmail()));
                    throw new BadRequestException(
                            "Your account has been suspended due to multiple failed login attempts today. " +
                                    "Please try again tomorrow or contact an administrator to reactivate your account."
                    );
                }
            }
        }

        try {
            // Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (user == null) {
                throw new BadRequestException(
                        "No account found with email " + StringUtils.maskEmail(request.getEmail()) +
                                " for role " + request.getRole().getDisplayName()
                );
            }

            // Verify user is active
            ValidationUtils.requireTrue(user.isActive(), "Account is inactive");

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
                loginAttemptService.resetFailedAttempts(user.getId());
                log.info("Reset failed login attempts for user: {}", StringUtils.maskEmail(user.getEmail()));
            }

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(user);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            log.info("User logged in successfully: {} - {}",
                    StringUtils.maskEmail(user.getEmail()), user.getUserType());

            return authMapper.toAuthResponseWithRoleData(
                    user,
                    accessToken,
                    refreshToken,
                    tokenProvider.getAccessTokenExpiryDate(),
                    userResponseMapper.extractRoleSpecificData(user)
            );

        } catch (AuthenticationException ex) {
            log.error("Authentication failed for: {}", StringUtils.maskEmail(request.getEmail()));

            if (user != null) {
                boolean suspended = loginAttemptService.recordFailedAttempt(user.getId(), user.getRole());
                if (suspended) {
                    throw new BadRequestException(
                            "Your account has been suspended due to 5 failed login attempts today. " +
                                    "Please try again tomorrow or contact an administrator to reactivate your account."
                    );
                }
            }

            throw new BadRequestException("Invalid email or password");
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private BaseUser validateAndMapToEntity(RegisterRequest request) {
        return switch (request.getRole()) {
            case ROLE_STUDENT -> validateAndMapStudent(request);
            case ROLE_LECTURER -> validateAndMapLecturer(request);
            case ROLE_ACADEMIC_STAFF -> validateAndMapAcademicStaff(request);
            case ROLE_NON_ACADEMIC_STAFF -> validateAndMapNonAcademicStaff(request);
            case ROLE_ADMIN -> validateAndMapAdmin(request);
            case ROLE_SUPER_ADMIN -> validateAndMapSuperAdmin(request);
        };
    }

    // --- Student ---
    private BaseUser validateAndMapStudent(RegisterRequest request) {
        if (!(request instanceof StudentRegisterRequest studentRequest)) {
            throw new BadRequestException("Invalid request type for student registration");
        }

        if (studentRepository.existsByStudentId(studentRequest.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }

        if (studentRequest.getDateOfBirth() != null &&
                studentRequest.getDateOfBirth().isAfter(LocalDate.now().minusYears(16))) {
            throw new BadRequestException("Student must be at least 16 years old");
        }

        StudentRoleType roleType = studentRequest.getStudentRoleType();
        if (roleType != null && roleType != StudentRoleType.NORMAL) {
            validateStudentRoleSpecificFields(studentRequest, roleType);
        }

        Student student = userMapper.toStudent(studentRequest);
        student.setEmail(studentRequest.getEmail());
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());
        return student;
    }

    private void validateStudentRoleSpecificFields(StudentRegisterRequest request, StudentRoleType roleType) {
        switch (roleType) {
            case CLUB_MEMBER -> {
                ValidationUtils.requireNonBlank(request.getClubName(), "Club name");
                ValidationUtils.requireNonBlank(request.getClubPosition(), "Club position");
                ValidationUtils.requireNonNull(request.getClubJoinDate(), "Club join date");
            }
            case SENIOR_KUPPI -> {
                ValidationUtils.requireNonEmpty(request.getKuppiSubjects(), "Kuppi subjects");
                ValidationUtils.requireNonBlank(request.getKuppiExperienceLevel(), "Experience level");
            }
            case BATCH_REP -> {
                ValidationUtils.requireNonBlank(request.getBatchRepYear(), "Batch representative year");
                ValidationUtils.requireNonBlank(request.getBatchRepSemester(), "Batch representative semester");
            }
            default -> {
                // NORMAL - no extra validation needed
            }
        }
    }

    // --- Lecturer ---
    private BaseUser validateAndMapLecturer(RegisterRequest request) {
        if (!(request instanceof LecturerRegisterRequest lecturerRequest)) {
            throw new BadRequestException("Invalid request type for lecturer registration");
        }

        if (lecturerRepository.existsByEmployeeId(lecturerRequest.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        Lecturer lecturer = userMapper.toLecturer(lecturerRequest);
        lecturer.setEmail(lecturerRequest.getEmail());
        lecturer.setFirstName(lecturerRequest.getFirstName());
        lecturer.setLastName(lecturerRequest.getLastName());
        return lecturer;
    }

    // --- Academic Staff ---
    private BaseUser validateAndMapAcademicStaff(RegisterRequest request) {
        if (!(request instanceof AcademicStaffRegisterRequest staffRequest)) {
            throw new BadRequestException("Invalid request type for Academic Staff registration");
        }

        if (academicStaffRepository.existsByEmployeeId(staffRequest.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        if (staffRequest.getJoinDate() != null &&
                staffRequest.getJoinDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Join date cannot be in the future");
        }

        AcademicStaff staff = userMapper.toAcademicStaff(staffRequest);
        staff.setEmail(staffRequest.getEmail());
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());
        return staff;
    }

    // --- Non-Academic Staff ---
    private BaseUser validateAndMapNonAcademicStaff(RegisterRequest request) {
        if (!(request instanceof NonAcademicStaffRegisterRequest staffRequest)) {
            throw new BadRequestException("Invalid request type for Non-Academic Staff registration");
        }

        if (nonAcademicStaffRepository.existsByEmployeeId(staffRequest.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        if (staffRequest.getJoinDate() != null &&
                staffRequest.getJoinDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Join date cannot be in the future");
        }

        NonAcademicStaff staff = userMapper.toNonAcademicStaff(staffRequest);
        staff.setEmail(staffRequest.getEmail());
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());
        return staff;
    }

    // --- Admin ---
    private BaseUser validateAndMapAdmin(RegisterRequest request) {
        if (!(request instanceof AdminRegisterRequest adminRequest)) {
            throw new BadRequestException("Invalid request type for Admin registration");
        }

        if (adminRepository.existsByAdminId(adminRequest.getAdminId())) {
            throw new BadRequestException("Admin ID already exists");
        }

        if (adminRequest.getAssignedDate() != null &&
                adminRequest.getAssignedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Assigned date cannot be in the future");
        }

        Admin admin = userMapper.toAdmin(adminRequest);
        admin.setEmail(adminRequest.getEmail());
        admin.setFirstName(adminRequest.getFirstName());
        admin.setLastName(adminRequest.getLastName());
        return admin;
    }

    // --- Super Admin ---
    private BaseUser validateAndMapSuperAdmin(RegisterRequest request) {
        if (!(request instanceof SuperAdminRegisterRequest superAdminRequest)) {
            throw new BadRequestException("Invalid request type for Super Admin registration");
        }

        if (superAdminRepository.existsBySuperAdminId(superAdminRequest.getSuperAdminId())) {
            throw new BadRequestException("Super Admin ID already exists");
        }

        if (superAdminRequest.getAssignedDate() != null &&
                superAdminRequest.getAssignedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Assigned date cannot be in the future");
        }

        SuperAdmin superAdmin = userMapper.toSuperAdmin(superAdminRequest);
        superAdmin.setEmail(superAdminRequest.getEmail());
        superAdmin.setFirstName(superAdminRequest.getFirstName());
        superAdmin.setLastName(superAdminRequest.getLastName());
        return superAdmin;
    }

    private void logPostRegistration(BaseUser user) {
        if (user instanceof Student student) {
            log.info("Student registered successfully: {} - {} [Sub-Role: {}]",
                    student.getStudentId(),
                    StringUtils.maskEmail(student.getEmail()),
                    student.getStudentRoleDisplayName());
        } else if (user instanceof Lecturer lecturer) {
            log.info("Lecturer registered successfully: {} - {}",
                    lecturer.getEmployeeId(), lecturer.getEmail());
        } else if (user instanceof AcademicStaff staff) {
            log.info("Academic Staff registered successfully: {} - {}",
                    staff.getEmployeeId(), staff.getEmail());
        } else if (user instanceof NonAcademicStaff staff) {
            log.info("Non-Academic Staff registered successfully: {} - {}",
                    staff.getEmployeeId(), staff.getEmail());
        } else if (user instanceof Admin admin) {
            log.info("Admin registered successfully: {} - {}",
                    admin.getAdminId(), admin.getEmail());
        } else if (user instanceof SuperAdmin superAdmin) {
            log.info("Super Admin registered successfully: {} - {}",
                    superAdmin.getSuperAdminId(), superAdmin.getEmail());
        } else {
            log.info("User registered: {}", StringUtils.maskEmail(user.getEmail()));
        }
    }

    private boolean isAdminRole(UserRole role) {
        return UserRole.ROLE_ADMIN.equals(role) || UserRole.ROLE_SUPER_ADMIN.equals(role);
    }
}
