package lk.iit.nextora.module.election.dto.response;

import lk.iit.nextora.common.enums.CandidateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for candidate details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {

    private Long id;
    private Long electionId;
    private String electionTitle;

    // Student details
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentBatch;
    private String studentProgram;

    // Candidate details
    private String manifesto;
    private String slogan;
    private String photoUrl;
    private String qualifications;
    private String previousExperience;
    private CandidateStatus status;
    private Integer displayOrder;

    // Nomination details
    private LocalDateTime nominatedAt;
    private Long nominatedById;
    private String nominatedByName;

    // Review details
    private LocalDateTime reviewedAt;
    private Long reviewedById;
    private String reviewedByName;
    private String rejectionReason;

    // Vote count (only visible after results published or for admins)
    private Integer voteCount;
    private Double votePercentage;
    private Integer rank;
    private Boolean isWinner;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
