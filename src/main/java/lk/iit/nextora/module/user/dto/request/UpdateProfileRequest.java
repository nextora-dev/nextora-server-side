package lk.iit.nextora.module.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    // ==================== Student Fields ====================

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Guardian name must not exceed 50 characters")
    private String guardianName;

    @Size(max = 15, message = "Guardian phone must not exceed 15 characters")
    private String guardianPhone;

    // ==================== Lecturer Fields ====================

    @Size(max = 50, message = "Specialization must not exceed 50 characters")
    private String specialization;

    @Size(max = 100, message = "Office location must not exceed 100 characters")
    private String officeLocation;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private Boolean availableForMeetings;

    // ==================== Staff Fields (Academic & Non-Academic) ====================

    @Size(max = 500, message = "Responsibilities must not exceed 500 characters")
    private String responsibilities;

    @Size(max = 100, message = "Work location must not exceed 100 characters")
    private String workLocation;

    @Size(max = 50, message = "Shift must not exceed 50 characters")
    private String shift;
}

