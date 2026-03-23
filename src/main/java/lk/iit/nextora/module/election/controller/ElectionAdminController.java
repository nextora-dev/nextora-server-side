package lk.iit.nextora.module.election.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.election.dto.request.UpdateCandidateRequest;
import lk.iit.nextora.module.election.dto.response.CandidateResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lk.iit.nextora.module.election.dto.response.ElectionResultsResponse;
import lk.iit.nextora.module.election.dto.response.VotingStatisticsResponse;
import lk.iit.nextora.module.election.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Admin Election operations.
 * Handles administrative actions like force approvals, cancellations, and platform management.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.ELECTION_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Election Admin", description = "Admin endpoints for election management")
public class ElectionAdminController {

    private final ElectionService electionService;

    // ==================== ELECTION ADMIN OPERATIONS ====================

    @GetMapping
    @Operation(summary = "Get all elections", description = "Get all elections with admin view (includes all statuses)")
    @PreAuthorize("hasAuthority('ELECTION:ADMIN_READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> getAllElections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all elections");
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ElectionResponse> response = electionService.getAllElectionsAdmin(pageable);
        return ApiResponse.success("Elections retrieved successfully", response);
    }

    @DeleteMapping(ApiConstants.ELECTION_ADMIN_ELECTION_PERMANENT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Permanently delete election", description = "Permanently delete an election (Super Admin only)")
    @PreAuthorize("hasAuthority('ELECTION:PERMANENT_DELETE')")
    public ApiResponse<Void> permanentlyDeleteElection(@PathVariable Long electionId) {
        log.info("Super Admin permanently deleting election: {}", electionId);
        electionService.permanentlyDeleteElection(electionId);
        return ApiResponse.success("Election permanently deleted", null);
    }

    // ==================== ELECTION LIFECYCLE FORCE OPERATIONS ====================

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_OPEN_NOMINATIONS)
    @Operation(summary = "Force open nominations", description = "Admin override to force open nominations")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<ElectionResponse> forceOpenNominations(@PathVariable Long electionId) {
        log.info("Admin force opening nominations for election: {}", electionId);
        ElectionResponse response = electionService.forceOpenNominations(electionId);
        return ApiResponse.success("Nominations force opened successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_CLOSE_NOMINATIONS)
    @Operation(summary = "Force close nominations", description = "Admin override to force close nominations")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<ElectionResponse> forceCloseNominations(@PathVariable Long electionId) {
        log.info("Admin force closing nominations for election: {}", electionId);
        ElectionResponse response = electionService.forceCloseNominations(electionId);
        return ApiResponse.success("Nominations force closed successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_OPEN_VOTING)
    @Operation(summary = "Force open voting", description = "Admin override to force open voting")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<ElectionResponse> forceOpenVoting(@PathVariable Long electionId) {
        log.info("Admin force opening voting for election: {}", electionId);
        ElectionResponse response = electionService.forceOpenVoting(electionId);
        return ApiResponse.success("Voting force opened successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_CLOSE_VOTING)
    @Operation(summary = "Force close voting", description = "Admin override to force close voting")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<ElectionResponse> forceCloseVoting(@PathVariable Long electionId) {
        log.info("Admin force closing voting for election: {}", electionId);
        ElectionResponse response = electionService.forceCloseVoting(electionId);
        return ApiResponse.success("Voting force closed successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_PUBLISH_RESULTS)
    @Operation(summary = "Force publish results", description = "Admin override to force publish results")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<ElectionResponse> forcePublishResults(@PathVariable Long electionId) {
        log.info("Admin force publishing results for election: {}", electionId);
        ElectionResponse response = electionService.forcePublishResults(electionId);
        return ApiResponse.success("Results force published successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_FORCE_CANCEL)
    @Operation(summary = "Force cancel election", description = "Admin override to force cancel any election")
    @PreAuthorize("hasAuthority('ELECTION:FORCE_MANAGE')")
    public ApiResponse<Void> forceCancelElection(
            @PathVariable Long electionId,
            @RequestParam(required = false) String reason) {
        log.info("Admin force cancelling election: {} with reason: {}", electionId, reason);
        electionService.forceCancelElection(electionId, reason != null ? reason : "Cancelled by admin");
        return ApiResponse.success("Election force cancelled successfully", null);
    }

    // ==================== CANDIDATE ADMIN OPERATIONS ====================

    @GetMapping(ApiConstants.ELECTION_ADMIN_CANDIDATES_BY_ELECTION)
    @Operation(summary = "Get all candidates for election", description = "Get all candidates including pending/rejected")
    @PreAuthorize("hasAuthority('CANDIDATE:ADMIN_VIEW')")
    public ApiResponse<PagedResponse<CandidateResponse>> getAllCandidates(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin fetching all candidates for election: {}", electionId);
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getAllCandidatesAdmin(electionId, pageable);
        return ApiResponse.success("Candidates retrieved successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_CANDIDATE_FORCE_APPROVE_PATH)
    @Operation(summary = "Force approve candidate", description = "Admin override to force approve a candidate")
    @PreAuthorize("hasAuthority('CANDIDATE:FORCE_APPROVE')")
    public ApiResponse<CandidateResponse> forceApproveCandidate(
            @PathVariable Long electionId,
            @PathVariable Long candidateId) {
        log.info("Admin force approving candidate: {} in election: {}", candidateId, electionId);
        CandidateResponse response = electionService.forceApproveCandidate(candidateId);
        return ApiResponse.success("Candidate force approved successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_CANDIDATE_FORCE_REJECT_PATH)
    @Operation(summary = "Force reject candidate", description = "Admin override to force reject a candidate")
    @PreAuthorize("hasAuthority('CANDIDATE:FORCE_REJECT')")
    public ApiResponse<CandidateResponse> forceRejectCandidate(
            @PathVariable Long electionId,
            @PathVariable Long candidateId,
            @RequestParam(required = false) String reason) {
        log.info("Admin force rejecting candidate: {} in election: {} with reason: {}", candidateId, electionId, reason);
        CandidateResponse response = electionService.forceRejectCandidate(candidateId, reason);
        return ApiResponse.success("Candidate force rejected successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_CANDIDATE_DISQUALIFY_PATH)
    @Operation(summary = "Disqualify candidate", description = "Disqualify a candidate from election. Reason is required.")
    @PreAuthorize("hasAuthority('CANDIDATE:DISQUALIFY')")
    public ApiResponse<CandidateResponse> disqualifyCandidate(
            @PathVariable Long electionId,
            @PathVariable Long candidateId,
            @RequestParam(required = false) String reason) {
        String disqualifyReason = (reason != null && !reason.isBlank()) ? reason : "Disqualified by admin";
        log.info("Admin disqualifying candidate: {} in election: {} with reason: {}", candidateId, electionId, disqualifyReason);
        CandidateResponse response = electionService.disqualifyCandidate(candidateId, disqualifyReason);
        return ApiResponse.success("Candidate disqualified successfully", response);
    }

    @PutMapping(value = ApiConstants.ELECTION_ADMIN_CANDIDATES_UPDATE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Admin update candidate", description = "Admin override to update any candidate's details")
    @PreAuthorize("hasAuthority('CANDIDATE:ADMIN_UPDATE')")
    public ApiResponse<CandidateResponse> adminUpdateCandidate(
            @PathVariable Long candidateId,
            @RequestParam(value = "manifesto", required = false) String manifesto,
            @RequestParam(value = "slogan", required = false) String slogan,
            @RequestParam(value = "qualifications", required = false) String qualifications,
            @RequestParam(value = "previousExperience", required = false) String previousExperience,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        log.info("Admin updating candidate: {}", candidateId);

        UpdateCandidateRequest request = UpdateCandidateRequest.builder()
                .manifesto(manifesto)
                .slogan(slogan)
                .qualifications(qualifications)
                .previousExperience(previousExperience)
                .build();

        CandidateResponse response = electionService.adminUpdateCandidate(candidateId, request, photo);
        return ApiResponse.success("Candidate updated successfully", response);
    }

    // ==================== STATISTICS & MONITORING ====================

    @GetMapping(ApiConstants.ELECTION_ADMIN_LIVE_VOTES)
    @Operation(summary = "Get live vote statistics", description = "Get real-time voting statistics")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ElectionResultsResponse> getLiveVoteStatistics(@PathVariable Long electionId) {
        log.info("Admin fetching live vote statistics for election: {}", electionId);
        ElectionResultsResponse response = electionService.getLiveVoteCount(electionId);
        return ApiResponse.success("Live vote statistics retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_ADMIN_STATISTICS_PATH)
    @Operation(summary = "Get platform voting statistics", description = "Get overall voting platform statistics including clubs, elections, and voting metrics")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<VotingStatisticsResponse> getPlatformStatistics() {
        log.info("Admin fetching platform voting statistics");
        VotingStatisticsResponse response = electionService.getPlatformStatistics();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_ADMIN_STATISTICS_CLUBS)
    @Operation(summary = "Get club voting statistics", description = "Get voting statistics for a specific club")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<VotingStatisticsResponse> getClubStatistics(@PathVariable Long clubId) {
        log.info("Admin fetching voting statistics for club: {}", clubId);
        VotingStatisticsResponse response = electionService.getClubStatistics(clubId);
        return ApiResponse.success("Club statistics retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_ADMIN_STATISTICS_ELECTIONS)
    @Operation(summary = "Get election detailed statistics", description = "Get detailed statistics for a specific election")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ElectionResultsResponse> getElectionStatistics(@PathVariable Long electionId) {
        log.info("Admin fetching detailed statistics for election: {}", electionId);
        ElectionResultsResponse response = electionService.getElectionStatistics(electionId);
        return ApiResponse.success("Election statistics retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_ADMIN_STATISTICS_SUMMARY)
    @Operation(summary = "Get quick statistics summary", description = "Get a quick summary of key voting metrics")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<VotingStatisticsResponse> getStatisticsSummary() {
        log.info("Admin fetching quick statistics summary");
        VotingStatisticsResponse response = electionService.getStatisticsSummary();
        return ApiResponse.success("Statistics summary retrieved successfully", response);
    }

    // ==================== SUPER ADMIN OPERATIONS ====================

    @PostMapping(ApiConstants.ELECTION_ADMIN_PROCESS_STATUS_PATH)
    @Operation(summary = "Process status updates", description = "Manually trigger election status updates (Super Admin)")
    @PreAuthorize("hasAuthority('ELECTION:SUPER_ADMIN')")
    public ApiResponse<Void> processElectionStatusUpdates() {
        log.info("Super Admin triggering election status updates");
        electionService.processElectionStatusUpdates();
        return ApiResponse.success("Election status updates processed successfully", null);
    }

    @PostMapping(ApiConstants.ELECTION_ADMIN_RESET_VOTES_PATH)
    @Operation(summary = "Reset election votes", description = "Reset all votes for an election (Super Admin only)")
    @PreAuthorize("hasAuthority('ELECTION:SUPER_ADMIN')")
    public ApiResponse<Void> resetElectionVotes(@PathVariable Long electionId) {
        log.info("Super Admin resetting votes for election: {}", electionId);
        electionService.resetElectionVotes(electionId);
        return ApiResponse.success("Election votes reset successfully", null);
    }
}
