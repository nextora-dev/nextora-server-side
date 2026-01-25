package lk.iit.nextora.module.voting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.dto.response.ClubStatisticsResponse;
import lk.iit.nextora.module.club.service.ClubService;
import lk.iit.nextora.module.voting.dto.request.CreateElectionRequest;
import lk.iit.nextora.module.voting.dto.request.ReviewCandidateRequest;
import lk.iit.nextora.module.voting.dto.request.UpdateElectionRequest;
import lk.iit.nextora.module.voting.dto.response.*;
import lk.iit.nextora.module.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for Voting Module.
 * Provides administrative endpoints for Super Admin and Admin users.
 * All endpoints require ROLE_ADMIN or ROLE_SUPER_ADMIN.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.VOTING_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Voting Admin", description = "Administrative endpoints for voting module management")
public class VotingAdminController {

    private final ClubService clubService;
    private final ElectionService electionService;

    // ==================== CLUB ADMIN ENDPOINTS ====================

    /**
     * Create a new club - Admin only
     */
    @PostMapping("/clubs")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create club (Admin)",
            description = "Create a new club. Admin only."
    )
    @PreAuthorize("hasAuthority('CLUB:CREATE')")
    public ApiResponse<ClubResponse> createClub(@Valid @RequestBody CreateClubRequest request) {
        log.info("Admin creating new club: {}", request.getClubCode());
        ClubResponse response = clubService.createClub(request);
        return ApiResponse.success("Club created successfully by admin", response);
    }

    /**
     * Get all clubs with full details - Admin view
     */
    @GetMapping("/clubs")
    @Operation(
            summary = "Get all clubs (Admin)",
            description = "Retrieve all clubs including inactive ones."
    )
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<PagedResponse<ClubResponse>> getAllClubsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ClubResponse> response = clubService.getAllClubs(pageable);
        return ApiResponse.success("Clubs retrieved successfully", response);
    }

    /**
     * Update any club - Admin override
     */
    @PutMapping("/clubs/{clubId}")
    @Operation(
            summary = "Update club (Admin)",
            description = "Update any club details. Admin override."
    )
    @PreAuthorize("hasAuthority('CLUB:UPDATE')")
    public ApiResponse<ClubResponse> updateClubAdmin(
            @PathVariable Long clubId,
            @Valid @RequestBody CreateClubRequest request) {
        log.info("Admin updating club: {}", clubId);
        ClubResponse response = clubService.updateClub(clubId, request);
        return ApiResponse.success("Club updated successfully by admin", response);
    }

    /**
     * Delete any club - Admin only
     */
    @DeleteMapping("/clubs/{clubId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete club (Admin)",
            description = "Delete a club. Admin only."
    )
    @PreAuthorize("hasAuthority('CLUB:DELETE')")
    public ApiResponse<Void> deleteClubAdmin(@PathVariable Long clubId) {
        log.info("Admin deleting club: {}", clubId);
        clubService.deleteClub(clubId);
        return ApiResponse.success("Club deleted successfully by admin", null);
    }

    /**
     * Force approve membership - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_MEMBERSHIP_FORCE_APPROVE)
    @Operation(
            summary = "Force approve membership (Admin)",
            description = "Force approve a membership. Admin override."
    )
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<ClubMembershipResponse> forceApproveMembership(@PathVariable Long membershipId) {
        log.info("Admin force approving membership: {}", membershipId);
        ClubMembershipResponse response = clubService.approveMembership(membershipId);
        return ApiResponse.success("Membership force approved by admin", response);
    }

    /**
     * Force reject membership - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_MEMBERSHIP_FORCE_REJECT)
    @Operation(
            summary = "Force reject membership (Admin)",
            description = "Force reject a membership. Admin override."
    )
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<Void> forceRejectMembership(
            @PathVariable Long membershipId,
            @RequestParam(defaultValue = "Rejected by administrator") String reason) {
        log.info("Admin force rejecting membership: {}", membershipId);
        clubService.rejectMembership(membershipId, reason);
        return ApiResponse.success("Membership force rejected by admin", null);
    }

    /**
     * Get all memberships for a club - Admin view
     */
    @GetMapping("/clubs/{clubId}/memberships")
    @Operation(
            summary = "Get all memberships (Admin)",
            description = "Get all memberships for a club."
    )
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<PagedResponse<ClubMembershipResponse>> getAllMembershipsAdmin(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubMembershipResponse> response = clubService.getClubMembers(clubId, pageable);
        return ApiResponse.success("Memberships retrieved successfully", response);
    }

    /**
     * Suspend any membership - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_MEMBERSHIP_SUSPEND)
    @Operation(
            summary = "Suspend membership (Admin)",
            description = "Suspend any club membership. Admin override."
    )
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<Void> suspendMembershipAdmin(
            @PathVariable Long membershipId,
            @RequestParam String reason) {
        log.info("Admin suspending membership: {} with reason: {}", membershipId, reason);
        clubService.suspendMembership(membershipId, reason);
        return ApiResponse.success("Membership suspended by admin", null);
    }

    // ==================== ELECTION ADMIN ENDPOINTS ====================

    /**
     * Create election for any club - Admin override
     */
    @PostMapping("/elections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create election (Admin)",
            description = "Create an election for any club. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:CREATE')")
    public ApiResponse<ElectionResponse> createElectionAdmin(@Valid @RequestBody CreateElectionRequest request) {
        log.info("Admin creating election for club: {}", request.getClubId());
        ElectionResponse response = electionService.createElection(request);
        return ApiResponse.success("Election created successfully by admin", response);
    }

    /**
     * Get all elections across all clubs - Admin view
     */
    @GetMapping("/elections")
    @Operation(
            summary = "Get all elections (Admin)",
            description = "Retrieve all elections across all clubs."
    )
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> getAllElectionsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) ElectionStatus status) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ElectionResponse> response;
        if (status != null) {
            response = electionService.getElectionsByStatus(status, pageable);
        } else {
            response = electionService.getUpcomingElections(pageable);
        }
        return ApiResponse.success("Elections retrieved successfully", response);
    }

    /**
     * Update any election - Admin override
     */
    @PutMapping("/elections/{electionId}")
    @Operation(
            summary = "Update election (Admin)",
            description = "Update any election. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:UPDATE')")
    public ApiResponse<ElectionResponse> updateElectionAdmin(
            @PathVariable Long electionId,
            @Valid @RequestBody UpdateElectionRequest request) {
        log.info("Admin updating election: {}", electionId);
        ElectionResponse response = electionService.updateElection(electionId, request);
        return ApiResponse.success("Election updated successfully by admin", response);
    }

    /**
     * Delete any election - Admin override
     */
    @DeleteMapping("/elections/{electionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete election (Admin)",
            description = "Delete any election. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:DELETE')")
    public ApiResponse<Void> deleteElectionAdmin(@PathVariable Long electionId) {
        log.info("Admin deleting election: {}", electionId);
        electionService.deleteElection(electionId);
        return ApiResponse.success("Election deleted successfully by admin", null);
    }

    /**
     * Force open nominations - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_OPEN_NOMINATIONS)
    @Operation(
            summary = "Force open nominations (Admin)",
            description = "Force open nominations. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> forceOpenNominations(@PathVariable Long electionId) {
        log.info("Admin force opening nominations for election: {}", electionId);
        ElectionResponse response = electionService.openNominations(electionId);
        return ApiResponse.success("Nominations force opened by admin", response);
    }

    /**
     * Force close nominations - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_CLOSE_NOMINATIONS)
    @Operation(
            summary = "Force close nominations (Admin)",
            description = "Force close nominations. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> forceCloseNominations(@PathVariable Long electionId) {
        log.info("Admin force closing nominations for election: {}", electionId);
        ElectionResponse response = electionService.closeNominations(electionId);
        return ApiResponse.success("Nominations force closed by admin", response);
    }

    /**
     * Force open voting - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_OPEN_VOTING)
    @Operation(
            summary = "Force open voting (Admin)",
            description = "Force open voting. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> forceOpenVoting(@PathVariable Long electionId) {
        log.info("Admin force opening voting for election: {}", electionId);
        ElectionResponse response = electionService.openVoting(electionId);
        return ApiResponse.success("Voting force opened by admin", response);
    }

    /**
     * Force close voting - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_CLOSE_VOTING)
    @Operation(
            summary = "Force close voting (Admin)",
            description = "Force close voting. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> forceCloseVoting(@PathVariable Long electionId) {
        log.info("Admin force closing voting for election: {}", electionId);
        ElectionResponse response = electionService.closeVoting(electionId);
        return ApiResponse.success("Voting force closed by admin", response);
    }

    /**
     * Force publish results - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_PUBLISH_RESULTS)
    @Operation(
            summary = "Force publish results (Admin)",
            description = "Force publish results. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:PUBLISH_RESULTS')")
    public ApiResponse<ElectionResponse> forcePublishResults(@PathVariable Long electionId) {
        log.info("Admin force publishing results for election: {}", electionId);
        ElectionResponse response = electionService.publishResults(electionId);
        return ApiResponse.success("Results force published by admin", response);
    }

    /**
     * Force cancel election - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_FORCE_CANCEL)
    @Operation(
            summary = "Force cancel election (Admin)",
            description = "Force cancel any election. Admin override."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<Void> forceCancelElection(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "Cancelled by administrator") String reason) {
        log.info("Admin force cancelling election: {} with reason: {}", electionId, reason);
        electionService.cancelElection(electionId, reason);
        return ApiResponse.success("Election force cancelled by admin", null);
    }

    // ==================== CANDIDATE ADMIN ENDPOINTS ====================

    /**
     * Get all candidates for an election - Admin view
     */
    @GetMapping("/elections/{electionId}/candidates")
    @Operation(
            summary = "Get all candidates (Admin)",
            description = "Get all candidates including rejected/withdrawn."
    )
    @PreAuthorize("hasAuthority('CANDIDATE:VIEW')")
    public ApiResponse<PagedResponse<CandidateResponse>> getAllCandidatesAdmin(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getCandidates(electionId, pageable);
        return ApiResponse.success("All candidates retrieved successfully", response);
    }

    /**
     * Force approve candidate - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_CANDIDATE_FORCE_APPROVE)
    @Operation(
            summary = "Force approve candidate (Admin)",
            description = "Force approve a candidate. Admin override."
    )
    @PreAuthorize("hasAuthority('CANDIDATE:APPROVE')")
    public ApiResponse<CandidateResponse> forceApproveCandidate(@PathVariable Long candidateId) {
        log.info("Admin force approving candidate: {}", candidateId);
        ReviewCandidateRequest request = ReviewCandidateRequest.builder()
                .candidateId(candidateId)
                .approved(true)
                .build();
        CandidateResponse response = electionService.reviewCandidate(request);
        return ApiResponse.success("Candidate force approved by admin", response);
    }

    /**
     * Force reject candidate - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_CANDIDATE_FORCE_REJECT)
    @Operation(
            summary = "Force reject candidate (Admin)",
            description = "Force reject a candidate. Admin override."
    )
    @PreAuthorize("hasAuthority('CANDIDATE:APPROVE')")
    public ApiResponse<CandidateResponse> forceRejectCandidate(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "Rejected by administrator") String reason) {
        log.info("Admin force rejecting candidate: {}", candidateId);
        ReviewCandidateRequest request = ReviewCandidateRequest.builder()
                .candidateId(candidateId)
                .approved(false)
                .rejectionReason(reason)
                .build();
        CandidateResponse response = electionService.reviewCandidate(request);
        return ApiResponse.success("Candidate force rejected by admin", response);
    }

    /**
     * Force disqualify candidate - Admin override
     */
    @PostMapping(ApiConstants.VOTING_ADMIN_CANDIDATE_DISQUALIFY)
    @Operation(
            summary = "Disqualify candidate (Admin)",
            description = "Disqualify an approved candidate. Admin only."
    )
    @PreAuthorize("hasAuthority('CANDIDATE:APPROVE')")
    public ApiResponse<Void> disqualifyCandidate(
            @PathVariable Long candidateId,
            @RequestParam String reason) {
        log.info("Admin disqualifying candidate: {} with reason: {}", candidateId, reason);
        electionService.withdrawCandidacy(candidateId);
        return ApiResponse.success("Candidate disqualified by admin", null);
    }

    // ==================== VOTING ADMIN ENDPOINTS ====================

    /**
     * Get live vote count for any election - Admin view
     */
    @GetMapping(ApiConstants.VOTING_ADMIN_LIVE_VOTES)
    @Operation(
            summary = "Get live vote count (Admin)",
            description = "Get real-time vote counts for any election."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ElectionResultsResponse> getLiveVoteCountAdmin(@PathVariable Long electionId) {
        ElectionResultsResponse response = electionService.getLiveVoteCount(electionId);
        return ApiResponse.success("Live vote count retrieved successfully", response);
    }

    /**
     * Get election results - Admin can view before publication
     */
    @GetMapping("/elections/{electionId}/results")
    @Operation(
            summary = "Get election results (Admin)",
            description = "Get results. Admin can view before publication."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ElectionResultsResponse> getElectionResultsAdmin(@PathVariable Long electionId) {
        ElectionResultsResponse response = electionService.getLiveVoteCount(electionId);
        return ApiResponse.success("Election results retrieved successfully", response);
    }

    // ==================== STATISTICS ENDPOINTS ====================

    /**
     * Get voting module statistics - Admin dashboard
     */
    @GetMapping("/statistics")
    @Operation(
            summary = "Get voting statistics (Admin)",
            description = "Get overall voting module statistics."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<VotingStatisticsResponse> getVotingStatistics() {
        log.info("Admin retrieving voting statistics");
        VotingStatisticsResponse stats = VotingStatisticsResponse.builder()
                .message("Statistics retrieved successfully")
                .build();
        return ApiResponse.success("Voting statistics retrieved successfully", stats);
    }

    /**
     * Get club statistics
     */
    @GetMapping("/clubs/{clubId}/statistics")
    @Operation(
            summary = "Get club statistics (Admin)",
            description = "Get detailed statistics for a specific club."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ClubStatisticsResponse> getClubStatistics(@PathVariable Long clubId) {
        log.info("Admin retrieving statistics for club: {}", clubId);
        ClubStatisticsResponse stats = ClubStatisticsResponse.builder()
                .clubId(clubId)
                .message("Club statistics retrieved successfully")
                .build();
        return ApiResponse.success("Club statistics retrieved successfully", stats);
    }
}
