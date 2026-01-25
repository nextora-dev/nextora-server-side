package lk.iit.nextora.module.club.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.common.enums.FacultyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new club
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClubRequest {

    @NotBlank(message = "Club code is required")
    @Size(max = 20, message = "Club code must not exceed 20 characters")
    private String clubCode;

    @NotBlank(message = "Club name is required")
    @Size(max = 100, message = "Club name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    private FacultyType faculty;

    @Email(message = "Invalid email format")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 15, message = "Contact number must not exceed 15 characters")
    private String contactNumber;

    private LocalDate establishedDate;

    @Size(max = 500, message = "Social media links must not exceed 500 characters")
    private String socialMediaLinks;

    private Long presidentId;

    private Long advisorId;

    @Builder.Default
    private Integer maxMembers = 500;

    @Builder.Default
    private Boolean isRegistrationOpen = true;
}
