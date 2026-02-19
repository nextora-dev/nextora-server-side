package lk.iit.nextora.module.user.service.helper;

import lk.iit.nextora.common.util.StringUtils;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.user.dto.request.UpdateProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserUpdateHelper {

    /**
     * Update common fields for all user types
     */
    public void updateCommonFields(BaseUser user, UpdateProfileRequest request) {
        if (StringUtils.isNotBlank(request.getFirstName())) {
            user.setFirstName(StringUtils.trim(request.getFirstName()));
        }
        if (StringUtils.isNotBlank(request.getLastName())) {
            user.setLastName(StringUtils.trim(request.getLastName()));
        }
        if (request.getPhone() != null) {
            user.setPhoneNumber(StringUtils.trim(request.getPhone()));
        }
    }

    /**
     * Update role-specific fields based on user type
     */
    public void updateRoleSpecificFields(BaseUser user, UpdateProfileRequest request) {
        if (user instanceof Student student) {
            updateStudentFields(student, request);
        } else if (user instanceof AcademicStaff staff) {
            updateAcademicStaffFields(staff, request);
        } else if (user instanceof NonAcademicStaff staff) {
            updateNonAcademicStaffFields(staff, request);
        } else {
            log.debug("No role-specific fields to update for user type: {}", user.getUserType());
        }
    }

    public void updateStudentFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        // Update common student fields
        if (request.getAddress() != null) {
            student.setAddress(StringUtils.trim(request.getAddress()));
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGuardianName() != null) {
            student.setGuardianName(StringUtils.trim(request.getGuardianName()));
        }
        if (request.getGuardianPhone() != null) {
            student.setGuardianPhone(StringUtils.trim(request.getGuardianPhone()));
        }

        // Update role-specific fields based on all studentRoleTypes
        if (student.getStudentRoleTypes() != null) {
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.CLUB_MEMBER)) {
                updateClubMemberFields(student, request);
            }
            // Handle both KUPPI_STUDENT (new) and SENIOR_KUPPI (deprecated) for backward compatibility
            if (student.hasKuppiCapability()) {
                updateKuppiStudentFields(student, request);
            }
            if (student.hasRoleType(lk.iit.nextora.common.enums.StudentRoleType.BATCH_REP)) {
                updateBatchRepFields(student, request);
            }
        }
    }

    public void updateClubMemberFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getClubName() != null) {
            student.setClubName(StringUtils.trim(request.getClubName()));
        }
        if (request.getClubPosition() != null) {
            student.setClubPosition(request.getClubPosition());
        }
        if (request.getClubJoinDate() != null) {
            student.setClubJoinDate(request.getClubJoinDate());
        }
        if (request.getClubMembershipId() != null) {
            student.setClubMembershipId(StringUtils.trim(request.getClubMembershipId()));
        }
    }

    public void updateKuppiStudentFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getKuppiSubjects() != null && !request.getKuppiSubjects().isEmpty()) {
            student.setKuppiSubjects(request.getKuppiSubjects());
        }
        if (request.getKuppiExperienceLevel() != null) {
            student.setKuppiExperienceLevel(StringUtils.trim(request.getKuppiExperienceLevel()));
        }
        if (request.getKuppiAvailability() != null) {
            student.setKuppiAvailability(StringUtils.trim(request.getKuppiAvailability()));
        }
    }

    public void updateBatchRepFields(lk.iit.nextora.module.auth.entity.Student student, UpdateProfileRequest request) {
        if (request.getBatchRepYear() != null) {
            student.setBatchRepYear(StringUtils.trim(request.getBatchRepYear()));
        }
        if (request.getBatchRepSemester() != null) {
            student.setBatchRepSemester(StringUtils.trim(request.getBatchRepSemester()));
        }
        if (request.getBatchRepElectedDate() != null) {
            student.setBatchRepElectedDate(request.getBatchRepElectedDate());
        }
        if (request.getBatchRepResponsibilities() != null) {
            student.setBatchRepResponsibilities(StringUtils.trim(request.getBatchRepResponsibilities()));
        }
    }

    public void updateAcademicStaffFields(lk.iit.nextora.module.auth.entity.AcademicStaff staff, UpdateProfileRequest request) {
        if (request.getOfficeLocation() != null) {
            staff.setOfficeLocation(StringUtils.trim(request.getOfficeLocation()));
        }
        if (request.getResponsibilities() != null) {
            staff.setResponsibilities(StringUtils.trim(request.getResponsibilities()));
        }
        // Lecturer-specific fields (merged into AcademicStaff)
        if (request.getSpecialization() != null) {
            staff.setSpecialization(StringUtils.trim(request.getSpecialization()));
        }
        if (request.getBio() != null) {
            staff.setBio(StringUtils.trim(request.getBio()));
        }
        if (request.getAvailableForMeetings() != null) {
            staff.setAvailableForMeetings(request.getAvailableForMeetings());
        }
        if (request.getDesignation() != null) {
            staff.setDesignation(StringUtils.trim(request.getDesignation()));
        }
    }

    public void updateNonAcademicStaffFields(lk.iit.nextora.module.auth.entity.NonAcademicStaff staff, UpdateProfileRequest request) {
        if (request.getWorkLocation() != null) {
            staff.setWorkLocation(StringUtils.trim(request.getWorkLocation()));
        }
        if (request.getShift() != null) {
            staff.setShift(StringUtils.trim(request.getShift()));
        }
    }
}

