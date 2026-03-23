package lk.iit.nextora.module.auth.mapper;

import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.auth.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MapStruct mapper for extracting role-specific user data
 */
@Mapper(config = MapperConfiguration.class)
public interface UserResponseMapper {

    default Map<String, Object> extractRoleSpecificData(BaseUser user) {
        if (user instanceof Student student) {
            return extractStudentData(student);
        } else if (user instanceof Admin admin) {
            return extractAdminData(admin);
        } else if (user instanceof AcademicStaff academicStaff) {
            return extractAcademicStaffData(academicStaff);
        } else if (user instanceof NonAcademicStaff nonAcademicStaff) {
            return extractNonAcademicStaffData(nonAcademicStaff);
        } else if (user instanceof SuperAdmin superAdmin) {
            return extractSuperAdminData(superAdmin);
        }
        return new HashMap<>();
    }

    /**
     * Extract Student-specific data including sub-role specific fields
     */
    @Named("extractStudentData")
    default Map<String, Object> extractStudentData(Student student) {
        Map<String, Object> data = new HashMap<>();

        // Common student fields
        data.put("studentId", student.getStudentId());
        data.put("batch", student.getBatch());
        data.put("program", student.getProgram());
        data.put("faculty", student.getFaculty());

        // Convert to new HashSet to avoid Hibernate lazy loading proxy issues when caching
        Set<StudentRoleType> roleTypes = student.getStudentRoleTypes();
        data.put("studentRoleTypes", roleTypes != null ? new HashSet<>(roleTypes) : new HashSet<>());

        data.put("primaryRoleType", student.getPrimaryRoleType());
        data.put("studentRoleDisplayName", student.getStudentRoleDisplayName());
        data.put("enrollmentDate", student.getEnrollmentDate());
        data.put("dateOfBirth", student.getDateOfBirth());
        data.put("address", student.getAddress());
        data.put("guardianName", student.getGuardianName());
        data.put("guardianPhone", student.getGuardianPhone());

        // Add role-specific data based on all studentRoleTypes
        if (student.getStudentRoleTypes() != null) {
            // CLUB_MEMBER data
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.CLUB_MEMBER)) {
                Map<String, Object> clubData = new HashMap<>();
                clubData.put("clubName", student.getClubName());
                clubData.put("clubPosition", student.getClubPosition());
                clubData.put("clubJoinDate", student.getClubJoinDate());
                clubData.put("clubMembershipId", student.getClubMembershipId());
                data.put("clubMemberData", clubData);
            }

            // KUPPI_STUDENT or SENIOR_KUPPI data (check both for compatibility)
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.KUPPI_STUDENT) ||
                student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.SENIOR_KUPPI)) {
                Map<String, Object> kuppiData = new HashMap<>();
                // Convert to new HashSet to avoid Hibernate lazy loading proxy issues
                Set<String> subjects = student.getKuppiSubjects();
                kuppiData.put("kuppiSubjects", subjects != null ? new HashSet<>(subjects) : new HashSet<>());
                kuppiData.put("kuppiExperienceLevel", student.getKuppiExperienceLevel());
                kuppiData.put("kuppiSessionsCompleted", student.getKuppiSessionsCompleted());
                kuppiData.put("kuppiRating", student.getKuppiRating());
                kuppiData.put("kuppiAvailability", student.getKuppiAvailability());
                data.put("seniorKuppiData", kuppiData);
            }

            // BATCH_REP data
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.BATCH_REP)) {
                Map<String, Object> batchRepData = new HashMap<>();
                batchRepData.put("batchRepYear", student.getBatchRepYear());
                batchRepData.put("batchRepSemester", student.getBatchRepSemester());
                batchRepData.put("batchRepElectedDate", student.getBatchRepElectedDate());
                batchRepData.put("batchRepResponsibilities", student.getBatchRepResponsibilities());
                data.put("batchRepData", batchRepData);
            }
        }

        return data;
    }

    /**
     * Extract Admin-specific data
     */
    @Named("extractAdminData")
    default Map<String, Object> extractAdminData(Admin admin) {
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", admin.getAdminId());
        data.put("department", admin.getDepartment());
        // Convert to new HashSet to avoid Hibernate lazy loading proxy issues when caching
        var permissions = admin.getPermissions();
        data.put("permissions", permissions != null ? new HashSet<>(permissions) : new HashSet<>());
        data.put("assignedDate", admin.getAssignedDate());
        return data;
    }

    /**
     * Extract AcademicStaff-specific data (includes lecturer fields)
     */
    @Named("extractAcademicStaffData")
    default Map<String, Object> extractAcademicStaffData(AcademicStaff staff) {
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", staff.getEmployeeId());
        data.put("department", staff.getDepartment());
        data.put("faculty", staff.getFaculty());
        data.put("position", staff.getPosition());
        data.put("officeLocation", staff.getOfficeLocation());
        data.put("joinDate", staff.getJoinDate());
        data.put("responsibilities", staff.getResponsibilities());
        // Lecturer-specific fields
        data.put("designation", staff.getDesignation());
        data.put("specialization", staff.getSpecialization());
        // Convert to new HashSet to avoid Hibernate lazy loading proxy issues when caching
        var qualifications = staff.getQualifications();
        data.put("qualifications", qualifications != null ? new HashSet<>(qualifications) : new HashSet<>());
        data.put("bio", staff.getBio());
        data.put("availableForMeetings", staff.getAvailableForMeetings());
        return data;
    }

    /**
     * Extract NonAcademicStaff-specific data
     */
    @Named("extractNonAcademicStaffData")
    default Map<String, Object> extractNonAcademicStaffData(NonAcademicStaff staff) {
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", staff.getEmployeeId());
        data.put("department", staff.getDepartment());
        data.put("position", staff.getPosition());
        data.put("workLocation", staff.getWorkLocation());
        data.put("joinDate", staff.getJoinDate());
        data.put("shift", staff.getShift());
        return data;
    }

    /**
     * Extract SuperAdmin-specific data
     */
    @Named("extractSuperAdminData")
    default Map<String, Object> extractSuperAdminData(SuperAdmin superAdmin) {
        Map<String, Object> data = new HashMap<>();
        data.put("superAdminId", superAdmin.getSuperAdminId());
        data.put("assignedDate", superAdmin.getAssignedDate());
        data.put("accessLevel", superAdmin.getAccessLevel());
        return data;
    }
}

