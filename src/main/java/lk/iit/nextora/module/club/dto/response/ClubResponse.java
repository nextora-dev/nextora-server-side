package lk.iit.nextora.module.club.dto.response;

import lk.iit.nextora.common.enums.FacultyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for club details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubResponse {

    private Long id;
    private String clubCode;
    private String name;
    private String description;
    private String logoUrl;
    private FacultyType faculty;
    private String email;
    private String contactNumber;
    private LocalDate establishedDate;
    private String socialMediaLinks;

    // President details
    private Long presidentId;
    private String presidentName;
    private String presidentEmail;

    // Advisor details (AcademicStaff)
    private Long advisorId;
    private String advisorName;
    private String advisorEmail;
    private String advisorDepartment;

    // Settings
    private Integer maxMembers;
    private Boolean isRegistrationOpen;

    // Statistics
    private Integer totalMembers;
    private Integer activeMembers;
    private Integer totalElections;
    private Integer activeElections;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
