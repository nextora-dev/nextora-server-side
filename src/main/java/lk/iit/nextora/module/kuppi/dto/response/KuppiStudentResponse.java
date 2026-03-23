package lk.iit.nextora.module.kuppi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.iit.nextora.common.enums.FacultyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for Kuppi Student information.
 * Contains public-facing information about students who can host Kuppi sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KuppiStudentResponse {

    private Long id;

    private String studentId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String profilePictureUrl;

    private String batch;

    private String program;

    private FacultyType faculty;

    // ==================== Kuppi Specific Fields ====================

    private Set<String> kuppiSubjects;

    private String kuppiExperienceLevel;

    private Integer kuppiSessionsCompleted;

    private Double kuppiRating;

    private String kuppiAvailability;

    // ==================== Statistics ====================

    private Long totalSessionsHosted;

    private Long totalViews;

    private Long upcomingSessions;


    // ==================== Metadata ====================

    private LocalDateTime approvedAt;

    private Boolean isActive;
}

