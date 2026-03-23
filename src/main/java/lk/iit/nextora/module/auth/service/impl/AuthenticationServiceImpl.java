package lk.iit.nextora.module.auth.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.security.jwt.JwtTokenProvider;
import lk.iit.nextora.module.auth.dto.request.*;
import lk.iit.nextora.module.auth.dto.response.AuthResponse;
import lk.iit.nextora.module.auth.dto.response.ForgotPasswordResponse;
import lk.iit.nextora.module.auth.entity.*;
import lk.iit.nextora.module.auth.mapper.AuthMapper;
import lk.iit.nextora.module.auth.mapper.UserMapper;
import lk.iit.nextora.module.auth.mapper.UserResponseMapper;
import lk.iit.nextora.module.auth.repository.*;
import lk.iit.nextora.module.auth.service.AuthenticationService;
import lk.iit.nextora.infrastructure.notification.email.service.EmailService;
import lk.iit.nextora.module.auth.service.LoginAttemptService;
import lk.iit.nextora.module.auth.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final LoginAttemptService loginAttemptService;

    // Repositories
    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final AdminRepository adminRepository;
    private final SuperAdminRepository superAdminRepository;

    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;

    private static final int MAX_ACTIVE_TOKENS = 3;
    private static final int TOKEN_EXPIRY_MINUTES = 60;

    // ==================== LOGIN ====================

    @Override
    public AuthResponse login(LoginRequest request) {

        ValidationUtils.requireValidEmail(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");
        ValidationUtils.requireNonNull(request.getRole(), "Role");

        log.info(
                "Login attempt for: {} as role: {}",
                StringUtils.maskEmail(request.getEmail()),
                request.getRole()
        );

        BaseUser user = userLookupService
                .findUserByEmailAndRole(request.getEmail(), request.getRole())
                .orElseThrow(() -> new BadRequestException(
                        "No account found with email " +
                                StringUtils.maskEmail(request.getEmail()) +
                                " for role " + request.getRole().getDisplayName()
                ));

        // Suspended account handling
        if (UserStatus.SUSPENDED.equals(user.getStatus())) {

            LocalDateTime lastFailedAt = user.getLastFailedLoginAt();
            boolean isNewDay = lastFailedAt == null ||
                    !lastFailedAt.toLocalDate().equals(LocalDate.now());

            if (!isNewDay) {
                throw new BadRequestException(
                        "Your account has been suspended due to multiple failed login attempts today. " +
                                "Please try again tomorrow or contact an administrator."
                );
            }

            loginAttemptService.resetFailedAttempts(user.getId());
            log.info("Account auto-unlocked for new day: {}", StringUtils.maskEmail(user.getEmail()));
        }

        // Inactive account (PASSWORD_CHANGE_REQUIRED is allowed)
        if (!user.isActive() && user.getStatus() != UserStatus.PASSWORD_CHANGE_REQUIRED) {
            throw new BadRequestException("Account is inactive. Please contact an administrator.");
        }

        try {
            // Authenticate (email + password)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Reset failed attempts
            loginAttemptService.resetFailedAttempts(user.getId());

            // FIRST LOGIN → FORCE PASSWORD CHANGE
            if (UserStatus.PASSWORD_CHANGE_REQUIRED.equals(user.getStatus())) {

                String accessToken = tokenProvider.generateAccessToken(user);

                return authMapper.toPasswordChangeRequiredResponse(
                        user,
                        accessToken,
                        "Password change required. Please change your password to continue."
                );
            }

            // Normal login
            String accessToken = tokenProvider.generateAccessToken(user);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            return authMapper.toAuthResponseWithRoleData(
                    user,
                    accessToken,
                    refreshToken,
                    tokenProvider.getAccessTokenExpiryDate(),
                    userResponseMapper.extractRoleSpecificData(user)
            );

        } catch (AuthenticationException ex) {

            boolean suspended = loginAttemptService.recordFailedAttempt(
                    user.getId(),
                    user.getRole()
            );

            if (suspended) {
                throw new BadRequestException(
                        "Your account has been suspended due to 5 failed login attempts today."
                );
            }

            throw new BadRequestException("Invalid email or password");
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private BaseUser validateAndMapToEntity(RegisterRequest request) {
        return switch (request.getRole()) {
            case ROLE_STUDENT -> validateAndMapStudent(request);
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

        // Get effective role types (supports both new and deprecated fields)
        java.util.Set<StudentRoleType> roleTypes = studentRequest.getEffectiveRoleTypes();

        // Validate role-specific fields for each role type
        for (StudentRoleType roleType : roleTypes) {
            if (roleType != StudentRoleType.NORMAL) {
                validateStudentRoleSpecificFields(studentRequest, roleType);
            }
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


    @Override
    @Transactional
    public ForgotPasswordResponse initiatePasswordReset(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", StringUtils.maskEmail(request.getEmail()));

        // Find user by email (and role if provided)
        BaseUser user;
        if (request.getRole() != null) {
            user = userLookupService.findUserByEmailAndRole(request.getEmail(), request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No account found with this email and role",
                            "email", request.getEmail()
                    ));
        } else {
            user = userLookupService.findUserByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No account found with this email",
                            "email", request.getEmail()
                    ));
        }

        // Check if user account is active
        if (!user.isActive() && user.getStatus() != UserStatus.PASSWORD_CHANGE_REQUIRED) {
            throw new BadRequestException("This account is inactive. Please contact an administrator.");
        }

        // Check for spam prevention - limit active tokens
//        long activeTokenCount = tokenRepository.countValidTokensForUser(user.getId(), LocalDateTime.now());
//        if (activeTokenCount >= MAX_ACTIVE_TOKENS) {
//            throw new BadRequestException(
//                    "Too many password reset requests. Please check your email or wait for the previous tokens to expire."
//            );
//        }
        // Invalidate any existing valid tokens for this user before creating a new one
        tokenRepository.invalidateAllTokensForUser(user.getId(), LocalDateTime.now());

        // Create new password reset token
        PasswordResetToken resetToken = new PasswordResetToken(user);
        tokenRepository.save(resetToken);

        // Send email with reset link
        emailService.sendPasswordResetEmail(user, resetToken.getToken());

        log.info("Password reset token generated and email sent to: {}", StringUtils.maskEmail(user.getEmail()));

        return authMapper.toForgotPasswordResponse(user, TOKEN_EXPIRY_MINUTES);
    }


    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset");

        // Validate passwords are provided
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            throw new BadRequestException("Confirm password is required");
        }

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Find and validate token
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token. Please request a new password reset."));

        if (token.isUsed()) {
            throw new BadRequestException("This token has already been used. Please request a new password reset.");
        }

        if (token.isExpired()) {
            throw new BadRequestException("This token has expired. Please request a new password reset.");
        }

        BaseUser user = token.getUser();

        // Check if new password is same as old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as your current password");
        }

        // Update user password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // If user was in PASSWORD_CHANGE_REQUIRED status, mark as ACTIVE
        if (UserStatus.PASSWORD_CHANGE_REQUIRED.equals(user.getStatus())) {
            user.setStatus(UserStatus.ACTIVE);
        }

        user.setUpdatedAt(LocalDateTime.now());
        entityManager.merge(user);

        // Mark token as used
        token.markAsUsed();
        tokenRepository.save(token);

        // Invalidate all other tokens for this user
        tokenRepository.invalidateAllTokensForUser(user.getId(), LocalDateTime.now());

        log.info("Password successfully reset for user: {}", StringUtils.maskEmail(user.getEmail()));
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.debug("Cleaning up expired password reset tokens");
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Expired password reset tokens cleaned up");
    }
}

