package lk.iit.nextora.module.auth.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.auth.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.Map;

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
        data.put("studentRoleType", student.getStudentRoleType());
        data.put("studentRoleDisplayName", student.getStudentRoleDisplayName());
        data.put("enrollmentDate", student.getEnrollmentDate());
        data.put("dateOfBirth", student.getDateOfBirth());
        data.put("address", student.getAddress());
        data.put("guardianName", student.getGuardianName());
        data.put("guardianPhone", student.getGuardianPhone());

        // Add role-specific data based on studentRoleType
        if (student.getStudentRoleType() != null) {
            switch (student.getStudentRoleType()) {
                case CLUB_MEMBER -> {
                    Map<String, Object> clubData = new HashMap<>();
                    clubData.put("clubName", student.getClubName());
                    clubData.put("clubPosition", student.getClubPosition());
                    clubData.put("clubJoinDate", student.getClubJoinDate());
                    clubData.put("clubMembershipId", student.getClubMembershipId());
                    data.put("clubMemberData", clubData);
                }
                case SENIOR_KUPPI -> {
                    Map<String, Object> kuppiData = new HashMap<>();
                    kuppiData.put("kuppiSubjects", student.getKuppiSubjects());
                    kuppiData.put("kuppiExperienceLevel", student.getKuppiExperienceLevel());
                    kuppiData.put("kuppiSessionsCompleted", student.getKuppiSessionsCompleted());
                    kuppiData.put("kuppiRating", student.getKuppiRating());
                    kuppiData.put("kuppiAvailability", student.getKuppiAvailability());
                    data.put("seniorKuppiData", kuppiData);
                }
                case BATCH_REP -> {
                    Map<String, Object> batchRepData = new HashMap<>();
                    batchRepData.put("batchRepYear", student.getBatchRepYear());
                    batchRepData.put("batchRepSemester", student.getBatchRepSemester());
                    batchRepData.put("batchRepElectedDate", student.getBatchRepElectedDate());
                    batchRepData.put("batchRepResponsibilities", student.getBatchRepResponsibilities());
                    data.put("batchRepData", batchRepData);
                }
                default -> {
                    // NORMAL student - no additional data
                }
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
        data.put("permissions", admin.getPermissions());
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
        data.put("qualifications", staff.getQualifications());
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

