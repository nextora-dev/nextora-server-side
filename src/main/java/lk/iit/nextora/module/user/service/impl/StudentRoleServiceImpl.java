package lk.iit.nextora.module.user.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.util.SecurityUtils;
import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.common.util.ValidationUtils;
import lk.iit.nextora.config.redis.CacheService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.user.dto.request.AddStudentRoleRequest;
import lk.iit.nextora.module.user.dto.request.RemoveStudentRoleRequest;
import lk.iit.nextora.module.user.dto.response.StudentRoleResponse;
import lk.iit.nextora.module.user.service.StudentRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StudentRoleService for managing student role upgrades.
 *
 * Student Lifecycle Flow:
 * 1. Admin creates Normal Student → ROLE_STUDENT with StudentRoleType.NORMAL
 * 2. Student joins club → System adds CLUB_MEMBER role
 * 3. Admin assigns Batch Rep → System adds BATCH_REP role
 * 4. Admin approves Kuppi request → System adds KUPPI_STUDENT role
 *
 * All roles are additive - never removed automatically.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentRoleServiceImpl implements StudentRoleService {

    @PersistenceContext
    private EntityManager entityManager;

    private final StudentRepository studentRepository;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public StudentRoleResponse getStudentRoles(Long studentId) {
        ValidationUtils.requireNonNull(studentId, "Student ID");
        log.debug("Fetching roles for student ID: {}", studentId);

        Student student = findStudentById(studentId);
        return mapToStudentRoleResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentRoleResponse getStudentRolesByStudentId(String studentId) {
        ValidationUtils.requireNonBlank(studentId, "Student ID");
        log.debug("Fetching roles for student with university ID: {}", studentId);

        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found", "studentId", studentId));
        return mapToStudentRoleResponse(student);
    }

    @Override
    public StudentRoleResponse addRole(Long studentId, AddStudentRoleRequest request) {
        validateAdminAccess();
        ValidationUtils.requireNonNull(studentId, "Student ID");
        ValidationUtils.requireNonNull(request.getRoleType(), "Role type");

        Student student = findStudentById(studentId);
        StudentRoleType roleType = request.getRoleType();

        log.info("Admin {} adding role {} to student {}",
                SecurityUtils.getCurrentUserEmail().orElse("unknown"),
                roleType, StringUtils.maskEmail(student.getEmail()));

        // Validate role type is not deprecated
        if (roleType.isDeprecated()) {
            roleType = roleType.getReplacementRole();
            log.info("Deprecated role {} replaced with {}", request.getRoleType(), roleType);
        }

        // Check if student already has the role
        if (student.hasRoleType(roleType)) {
            throw new BadRequestException("Student already has the role: " + roleType.getDisplayName());
        }

        // Add the role
        student.addRoleType(roleType);

        // Update role-specific fields
        updateRoleSpecificFields(student, roleType, request);

        entityManager.merge(student);
        entityManager.flush();

        // Evict cache
        evictStudentCaches(student);

        log.info("Role {} added successfully to student {}", roleType, StringUtils.maskEmail(student.getEmail()));
        return mapToStudentRoleResponse(student);
    }

    @Override
    public StudentRoleResponse removeRole(Long studentId, RemoveStudentRoleRequest request) {
        validateAdminAccess();
        ValidationUtils.requireNonNull(studentId, "Student ID");
        ValidationUtils.requireNonNull(request.getRoleType(), "Role type");

        Student student = findStudentById(studentId);
        StudentRoleType roleType = request.getRoleType();

        log.info("Admin {} removing role {} from student {} - Reason: {}",
                SecurityUtils.getCurrentUserEmail().orElse("unknown"),
                roleType, StringUtils.maskEmail(student.getEmail()), request.getReason());

        // Cannot remove base NORMAL role
        if (roleType.isBaseRole()) {
            throw new BadRequestException("Cannot remove base role: " + roleType.getDisplayName());
        }

        // Check if student has the role
        if (!student.hasRoleType(roleType)) {
            throw new BadRequestException("Student does not have the role: " + roleType.getDisplayName());
        }

        // Remove the role
        student.removeRoleType(roleType);

        entityManager.merge(student);
        entityManager.flush();

        // Evict cache
        evictStudentCaches(student);

        log.info("Role {} removed from student {}", roleType, StringUtils.maskEmail(student.getEmail()));
        return mapToStudentRoleResponse(student);
    }

    @Override
    public StudentRoleResponse addClubMemberRole(Long studentId, AddStudentRoleRequest request) {
        ValidationUtils.requireNonNull(studentId, "Student ID");

        Student student = findStudentById(studentId);

        log.info("Adding CLUB_MEMBER role to student {}", StringUtils.maskEmail(student.getEmail()));

        // Check if student already has the role
        if (student.hasRoleType(StudentRoleType.CLUB_MEMBER)) {
            log.info("Student {} already has CLUB_MEMBER role, updating club info",
                    StringUtils.maskEmail(student.getEmail()));
        } else {
            student.addRoleType(StudentRoleType.CLUB_MEMBER);
        }

        // Update club-specific fields
        if (request != null) {
            updateClubMemberFields(student, request);
        }

        entityManager.merge(student);
        entityManager.flush();

        // Evict cache
        evictStudentCaches(student);

        log.info("CLUB_MEMBER role added/updated for student {}", StringUtils.maskEmail(student.getEmail()));
        return mapToStudentRoleResponse(student);
    }

    @Override
    public StudentRoleResponse addBatchRepRole(Long studentId, AddStudentRoleRequest request) {
        validateAdminAccess();
        ValidationUtils.requireNonNull(studentId, "Student ID");

        Student student = findStudentById(studentId);

        log.info("Admin {} assigning BATCH_REP role to student {}",
                SecurityUtils.getCurrentUserEmail().orElse("unknown"),
                StringUtils.maskEmail(student.getEmail()));

        // Check if student already has the role
        if (student.hasRoleType(StudentRoleType.BATCH_REP)) {
            throw new BadRequestException("Student is already a Batch Representative");
        }

        // Add the role
        student.addRoleType(StudentRoleType.BATCH_REP);

        // Update batch rep specific fields
        if (request != null) {
            updateBatchRepFields(student, request);
        }

        // Set default elected date if not provided
        if (student.getBatchRepElectedDate() == null) {
            student.setBatchRepElectedDate(LocalDate.now());
        }

        entityManager.merge(student);
        entityManager.flush();

        // Evict cache
        evictStudentCaches(student);

        log.info("BATCH_REP role assigned to student {}", StringUtils.maskEmail(student.getEmail()));
        return mapToStudentRoleResponse(student);
    }

    @Override
    public StudentRoleResponse addKuppiStudentRole(Long studentId, AddStudentRoleRequest request) {
        validateAdminAccess();
        ValidationUtils.requireNonNull(studentId, "Student ID");

        Student student = findStudentById(studentId);

        log.info("Admin {} approving KUPPI_STUDENT role for student {}",
                SecurityUtils.getCurrentUserEmail().orElse("unknown"),
                StringUtils.maskEmail(student.getEmail()));

        // Check if student already has Kuppi capability
        if (student.hasKuppiCapability()) {
            throw new BadRequestException("Student already has Kuppi Student capabilities");
        }

        // Add the new KUPPI_STUDENT role (not the deprecated SENIOR_KUPPI)
        student.addRoleType(StudentRoleType.KUPPI_STUDENT);

        // Update kuppi specific fields
        if (request != null) {
            updateKuppiStudentFields(student, request);
        }

        // Initialize kuppi stats if not set
        if (student.getKuppiSessionsCompleted() == null) {
            student.setKuppiSessionsCompleted(0);
        }
        if (student.getKuppiRating() == null) {
            student.setKuppiRating(0.0);
        }

        entityManager.merge(student);
        entityManager.flush();

        // Evict cache
        evictStudentCaches(student);

        log.info("KUPPI_STUDENT role approved for student {}", StringUtils.maskEmail(student.getEmail()));
        return mapToStudentRoleResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentRoleResponse> getStudentsByRole(StudentRoleType roleType) {
        ValidationUtils.requireNonNull(roleType, "Role type");
        log.debug("Fetching students with role: {}", roleType);

        List<Student> students = entityManager
                .createQuery("SELECT s FROM Student s JOIN s.studentRoleTypes srt WHERE srt = :roleType", Student.class)
                .setParameter("roleType", roleType)
                .getResultList();

        return students.stream()
                .map(this::mapToStudentRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long studentId, StudentRoleType roleType) {
        ValidationUtils.requireNonNull(studentId, "Student ID");
        ValidationUtils.requireNonNull(roleType, "Role type");

        Student student = findStudentById(studentId);
        return student.hasRoleType(roleType);
    }

    @Override
    public int migrateDeprecatedRoles() {
        log.info("Starting migration of deprecated SENIOR_KUPPI roles to KUPPI_STUDENT");

        List<Student> studentsWithDeprecatedRole = entityManager
                .createQuery("SELECT s FROM Student s JOIN s.studentRoleTypes srt WHERE srt = :roleType", Student.class)
                .setParameter("roleType", StudentRoleType.SENIOR_KUPPI)
                .getResultList();

        int migratedCount = 0;
        for (Student student : studentsWithDeprecatedRole) {
            if (!student.hasRoleType(StudentRoleType.KUPPI_STUDENT)) {
                student.addRoleType(StudentRoleType.KUPPI_STUDENT);
                entityManager.merge(student);
                migratedCount++;
                log.debug("Migrated student {} from SENIOR_KUPPI to KUPPI_STUDENT",
                        StringUtils.maskEmail(student.getEmail()));
            }
        }

        entityManager.flush();
        log.info("Migration completed. {} students migrated from SENIOR_KUPPI to KUPPI_STUDENT", migratedCount);
        return migratedCount;
    }

    // ==================== Private Helper Methods ====================

    private void validateAdminAccess() {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isSuperAdmin()) {
            throw new BadRequestException("Only Admin or Super Admin can manage student roles");
        }
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found", "id", studentId));
    }

    private void updateRoleSpecificFields(Student student, StudentRoleType roleType, AddStudentRoleRequest request) {
        switch (roleType) {
            case CLUB_MEMBER -> updateClubMemberFields(student, request);
            case BATCH_REP -> updateBatchRepFields(student, request);
            case KUPPI_STUDENT -> updateKuppiStudentFields(student, request);
            default -> log.debug("No specific fields to update for role: {}", roleType);
        }
    }

    private void updateClubMemberFields(Student student, AddStudentRoleRequest request) {
        if (StringUtils.isNotBlank(request.getClubName())) {
            student.setClubName(request.getClubName().trim());
        }
        if (request.getClubPosition() != null) {
            student.setClubPosition(request.getClubPosition());
        }
        if (request.getClubJoinDate() != null) {
            student.setClubJoinDate(request.getClubJoinDate());
        } else if (student.getClubJoinDate() == null) {
            student.setClubJoinDate(LocalDate.now());
        }
        if (StringUtils.isNotBlank(request.getClubMembershipId())) {
            student.setClubMembershipId(request.getClubMembershipId().trim());
        }
    }

    private void updateBatchRepFields(Student student, AddStudentRoleRequest request) {
        if (StringUtils.isNotBlank(request.getBatchRepYear())) {
            student.setBatchRepYear(request.getBatchRepYear().trim());
        }
        if (StringUtils.isNotBlank(request.getBatchRepSemester())) {
            student.setBatchRepSemester(request.getBatchRepSemester().trim());
        }
        if (request.getBatchRepElectedDate() != null) {
            student.setBatchRepElectedDate(request.getBatchRepElectedDate());
        }
        if (StringUtils.isNotBlank(request.getBatchRepResponsibilities())) {
            student.setBatchRepResponsibilities(request.getBatchRepResponsibilities().trim());
        }
    }

    private void updateKuppiStudentFields(Student student, AddStudentRoleRequest request) {
        if (request.getKuppiSubjects() != null && !request.getKuppiSubjects().isEmpty()) {
            student.setKuppiSubjects(request.getKuppiSubjects());
        }
        if (StringUtils.isNotBlank(request.getKuppiExperienceLevel())) {
            student.setKuppiExperienceLevel(request.getKuppiExperienceLevel().trim());
        }
        if (StringUtils.isNotBlank(request.getKuppiAvailability())) {
            student.setKuppiAvailability(request.getKuppiAvailability().trim());
        }
    }

    private void evictStudentCaches(Student student) {
        cacheService.evictUserProfile(student.getId());
        cacheService.evictUsersList();
    }

    private StudentRoleResponse mapToStudentRoleResponse(Student student) {
        StudentRoleResponse.StudentRoleResponseBuilder builder = StudentRoleResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .email(student.getEmail())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFullName())
                .batch(student.getBatch())
                .program(student.getProgram())
                .faculty(student.getFaculty() != null ? student.getFaculty().name() : null)
                .status(student.getStatus())
                .roles(student.getStudentRoleTypes())
                .primaryRole(student.getPrimaryRoleType())
                .roleDisplayNames(student.getStudentRoleDisplayName())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt());

        // Add role-specific data if student has those roles
        if (student.hasRoleType(StudentRoleType.CLUB_MEMBER)) {
            builder.clubMemberData(StudentRoleResponse.ClubMemberData.builder()
                    .clubName(student.getClubName())
                    .clubPosition(student.getClubPosition() != null ? student.getClubPosition().name() : null)
                    .clubJoinDate(student.getClubJoinDate() != null ? student.getClubJoinDate().toString() : null)
                    .clubMembershipId(student.getClubMembershipId())
                    .build());
        }

        if (student.hasRoleType(StudentRoleType.BATCH_REP)) {
            builder.batchRepData(StudentRoleResponse.BatchRepData.builder()
                    .batchRepYear(student.getBatchRepYear())
                    .batchRepSemester(student.getBatchRepSemester())
                    .batchRepElectedDate(student.getBatchRepElectedDate() != null ? student.getBatchRepElectedDate().toString() : null)
                    .batchRepResponsibilities(student.getBatchRepResponsibilities())
                    .build());
        }

        if (student.hasKuppiCapability()) {
            builder.kuppiStudentData(StudentRoleResponse.KuppiStudentData.builder()
                    .kuppiSubjects(student.getKuppiSubjects())
                    .kuppiExperienceLevel(student.getKuppiExperienceLevel())
                    .kuppiAvailability(student.getKuppiAvailability())
                    .kuppiSessionsCompleted(student.getKuppiSessionsCompleted())
                    .kuppiRating(student.getKuppiRating())
                    .build());
        }

        return builder.build();
    }
}
