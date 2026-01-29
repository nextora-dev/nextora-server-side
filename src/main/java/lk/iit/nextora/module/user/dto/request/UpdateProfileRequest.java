package lk.iit.nextora.module.user.dto.request;

import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request DTO for updating user profile.
 * Contains common fields and role-specific fields.
 * Only applicable fields for the user's role will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    // ==================== Common Fields ====================

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    // ==================== Student Common Fields ====================

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Guardian name must not exceed 50 characters")
    private String guardianName;

    @Size(max = 15, message = "Guardian phone must not exceed 15 characters")
    private String guardianPhone;

    // ==================== CLUB_MEMBER Specific Fields ====================

    @Size(max = 100, message = "Club name must not exceed 100 characters")
    private String clubName;

    private ClubPositionsType clubPosition;

    private LocalDate clubJoinDate;

    @Size(max = 50, message = "Club membership ID must not exceed 50 characters")
    private String clubMembershipId;

    // ==================== SENIOR_KUPPI Specific Fields ====================

    private Set<String> kuppiSubjects;

    @Size(max = 20, message = "Experience level must not exceed 20 characters")
    private String kuppiExperienceLevel;

    @Size(max = 500, message = "Availability must not exceed 500 characters")
    private String kuppiAvailability;

    // ==================== BATCH_REP Specific Fields ====================

    @Size(max = 10, message = "Batch rep year must not exceed 10 characters")
    private String batchRepYear;

    @Size(max = 20, message = "Batch rep semester must not exceed 20 characters")
    private String batchRepSemester;

    private LocalDate batchRepElectedDate;

    @Size(max = 500, message = "Batch rep responsibilities must not exceed 500 characters")
    private String batchRepResponsibilities;

    // ==================== Academic Staff Fields (includes former Lecturer fields) ====================

    @Size(max = 50, message = "Designation must not exceed 50 characters")
    private String designation;

    @Size(max = 50, message = "Specialization must not exceed 50 characters")
    private String specialization;

    @Size(max = 100, message = "Office location must not exceed 100 characters")
    private String officeLocation;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private Boolean availableForMeetings;

    @Size(max = 500, message = "Responsibilities must not exceed 500 characters")
    private String responsibilities;

    // ==================== Non-Academic Staff Fields ====================

    @Size(max = 100, message = "Work location must not exceed 100 characters")
    private String workLocation;

    @Size(max = 50, message = "Shift must not exceed 50 characters")
    private String shift;
}

