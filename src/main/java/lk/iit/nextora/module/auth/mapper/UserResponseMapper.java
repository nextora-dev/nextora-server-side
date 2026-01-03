package lk.iit.nextora.module.auth.mapper;

import lk.iit.nextora.module.auth.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserResponseMapper {

    default Map<String, Object> extractRoleSpecificData(BaseUser user) {
        if (user instanceof Student student) {
            return extractStudentData(student);
        } else if (user instanceof Lecturer lecturer) {
            return extractLecturerData(lecturer);
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
     * Extract Student-specific data
     */
    @Named("extractStudentData")
    default Map<String, Object> extractStudentData(Student student) {
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getStudentId());
        data.put("batch", student.getBatch());
        data.put("program", student.getProgram());
        data.put("faculty", student.getFaculty());
        data.put("enrollmentDate", student.getEnrollmentDate());
        data.put("dateOfBirth", student.getDateOfBirth());
        data.put("address", student.getAddress());
        data.put("guardianName", student.getGuardianName());
        data.put("guardianPhone", student.getGuardianPhone());
        return data;
    }

    /**
     * Extract Lecturer-specific data
     */
    @Named("extractLecturerData")
    default Map<String, Object> extractLecturerData(Lecturer lecturer) {
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", lecturer.getEmployeeId());
        data.put("department", lecturer.getDepartment());
        data.put("faculty", lecturer.getFaculty());
        data.put("designation", lecturer.getDesignation());
        data.put("specialization", lecturer.getSpecialization());
        data.put("qualifications", lecturer.getQualifications());
        data.put("joinDate", lecturer.getJoinDate());
        data.put("officeLocation", lecturer.getOfficeLocation());
        data.put("bio", lecturer.getBio());
        data.put("availableForMeetings", lecturer.getAvailableForMeetings());
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
     * Extract AcademicStaff-specific data
     */
    @Named("extractAcademicStaffData")
    default Map<String, Object> extractAcademicStaffData(AcademicStaff staff) {
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", staff.getEmployeeId());
        data.put("department", staff.getDepartment());
        data.put("position", staff.getPosition());
        data.put("officeLocation", staff.getOfficeLocation());
        data.put("joinDate", staff.getJoinDate());
        data.put("responsibilities", staff.getResponsibilities());
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

