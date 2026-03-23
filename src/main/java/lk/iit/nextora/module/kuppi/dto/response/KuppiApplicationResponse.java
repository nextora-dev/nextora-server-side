package lk.iit.nextora.module.kuppi.dto.response;

import lk.iit.nextora.common.enums.ExperienceLevel;
import lk.iit.nextora.common.enums.KuppiApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for Kuppi Student application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiApplicationResponse {

    private Long id;
    private KuppiApplicationStatus status;
    private String statusDisplayName;

    // ==================== Student Info ====================
    private Long studentId;
    private String studentUserId;
    private String studentName;
    private String studentEmail;
    private String studentBatch;
    private String studentProgram;
    private String studentFaculty;
    private String studentProfilePictureUrl;

    // ==================== Application Details ====================
    private String motivation;
    private String relevantExperience;
    private Set<String> subjectsToTeach;
    private ExperienceLevel preferredExperienceLevel;
    private String availability;
    private Double currentGpa;
    private String currentSemester;
    private String academicResultsUrl;
    private String academicResultsFileName;

    // ==================== Review Details ====================
    private Long reviewedById;
    private String reviewedByName;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private String rejectionReason;

    // ==================== Timestamps ====================
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Flags ====================
    private Boolean canBeApproved;
    private Boolean canBeRejected;
    private Boolean canBeCancelled;
    private Boolean isFinalState;
}

