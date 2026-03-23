package lk.iit.nextora.module.club.dto.response;

import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for club membership details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembershipResponse {

    private Long id;
    private String membershipNumber;

    // Club details
    private Long clubId;
    private String clubCode;
    private String clubName;

    // Member details
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String memberProfilePictureUrl;
    private String memberStudentId;
    private String memberBatch;

    // Membership details
    private ClubMembershipStatus status;
    private ClubPositionsType position;
    private LocalDate joinDate;
    private LocalDate expiryDate;
    private String remarks;

    // Approval details
    private LocalDateTime approvedAt;
    private Long approvedById;
    private String approvedByName;

    // Eligibility flags
    private Boolean canVote;
    private Boolean canNominate;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
