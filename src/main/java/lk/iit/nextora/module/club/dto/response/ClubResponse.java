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
    private ClubOfficerResponse president;

    // Vice President details
    private ClubOfficerResponse vicePresident;

    // Secretary details
    private ClubOfficerResponse secretary;

    // Treasurer details
    private ClubOfficerResponse treasurer;

    // Advisor details (AcademicStaff)
    private ClubOfficerResponse advisor;

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

    /**
     * DTO for club officer details (Vice President, Secretary, Treasurer)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClubOfficerResponse {
        private Long id;
        private String name;
        private String email;
        private String profilePictureUrl;
    }
}
