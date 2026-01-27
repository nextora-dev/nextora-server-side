package lk.iit.nextora.module.voting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.module.voting.dto.request.CancelElectionRequest;
import lk.iit.nextora.module.voting.dto.request.CastVoteRequest;
import lk.iit.nextora.module.voting.dto.request.CreateElectionRequest;
import lk.iit.nextora.module.voting.dto.request.NominateCandidateRequest;
import lk.iit.nextora.module.voting.dto.request.ReviewCandidateRequest;
import lk.iit.nextora.module.voting.dto.request.UpdateCandidateRequest;
import lk.iit.nextora.module.voting.dto.request.UpdateElectionRequest;
import lk.iit.nextora.module.voting.dto.response.CandidateResponse;
import lk.iit.nextora.module.voting.dto.response.ElectionResponse;
import lk.iit.nextora.module.voting.dto.response.ElectionResultsResponse;
import lk.iit.nextora.module.voting.dto.response.VoteResponse;
import lk.iit.nextora.module.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Election and Voting operations.
 * Handles all endpoints related to club elections, candidate nominations, and voting.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.ELECTIONS)
@RequiredArgsConstructor
@Tag(name = "Election & Voting", description = "Club election and voting management endpoints")
public class ElectionController {

    private final ElectionService electionService;

    // ==================== ELECTION CRUD ENDPOINTS ====================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create election", description = "Create a new election for a club")
    @PreAuthorize("hasAuthority('ELECTION:CREATE')")
    public ApiResponse<ElectionResponse> createElection(@Valid @RequestBody CreateElectionRequest request) {
        log.info("Creating new election for club: {}", request.getClubId());
        ElectionResponse response = electionService.createElection(request);
        return ApiResponse.success("Election created successfully", response);
    }

    @PutMapping(ApiConstants.ELECTION_BY_ID)
    @Operation(summary = "Update election", description = "Update election details")
    @PreAuthorize("hasAuthority('ELECTION:UPDATE')")
    public ApiResponse<ElectionResponse> updateElection(
            @PathVariable Long electionId,
            @Valid @RequestBody UpdateElectionRequest request) {
        log.info("Updating election: {}", electionId);
        ElectionResponse response = electionService.updateElection(electionId, request);
        return ApiResponse.success("Election updated successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_BY_ID)
    @Operation(summary = "Get election by ID", description = "Retrieve election details by ID")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<ElectionResponse> getElectionById(@PathVariable Long electionId) {
        ElectionResponse response = electionService.getElectionById(electionId);
        return ApiResponse.success("Election retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_DETAILS)
    @Operation(summary = "Get election with candidates", description = "Retrieve election with approved candidates")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<ElectionResponse> getElectionWithCandidates(@PathVariable Long electionId) {
        ElectionResponse response = electionService.getElectionWithCandidates(electionId);
        return ApiResponse.success("Election with candidates retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_BY_CLUB)
    @Operation(summary = "Get elections by club", description = "Retrieve all elections for a specific club")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> getElectionsByClub(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ElectionResponse> response = electionService.getElectionsByClub(clubId, pageable);
        return ApiResponse.success("Elections retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_BY_STATUS)
    @Operation(summary = "Get elections by status", description = "Retrieve elections filtered by status")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> getElectionsByStatus(
            @PathVariable ElectionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ElectionResponse> response = electionService.getElectionsByStatus(status, pageable);
        return ApiResponse.success("Elections retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_UPCOMING)
    @Operation(summary = "Get upcoming elections", description = "Retrieve all upcoming elections")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> getUpcomingElections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ElectionResponse> response = electionService.getUpcomingElections(pageable);
        return ApiResponse.success("Upcoming elections retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_VOTABLE)
    @Operation(summary = "Get votable elections", description = "Get elections where current user can vote")
    @PreAuthorize("hasAuthority('VOTE:CAST')")
    public ApiResponse<PagedResponse<ElectionResponse>> getVotableElections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ElectionResponse> response = electionService.getVotableElections(pageable);
        return ApiResponse.success("Votable elections retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_SEARCH)
    @Operation(summary = "Search elections", description = "Search elections by keyword")
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<PagedResponse<ElectionResponse>> searchElections(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ElectionResponse> response = electionService.searchElections(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @DeleteMapping(ApiConstants.ELECTION_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete election", description = "Delete a draft election")
    @PreAuthorize("hasAuthority('ELECTION:DELETE')")
    public ApiResponse<Void> deleteElection(@PathVariable Long electionId) {
        log.info("Deleting election: {}", electionId);
        electionService.deleteElection(electionId);
        return ApiResponse.success("Election deleted successfully", null);
    }

    // ==================== ELECTION LIFECYCLE ENDPOINTS ====================

    @PostMapping(ApiConstants.ELECTION_OPEN_NOMINATIONS)
    @Operation(summary = "Open nominations", description = "Open the nomination period for an election")
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> openNominations(@PathVariable Long electionId) {
        log.info("Opening nominations for election: {}", electionId);
        ElectionResponse response = electionService.openNominations(electionId);
        return ApiResponse.success("Nominations opened successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_CLOSE_NOMINATIONS)
    @Operation(summary = "Close nominations", description = "Close the nomination period")
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> closeNominations(@PathVariable Long electionId) {
        log.info("Closing nominations for election: {}", electionId);
        ElectionResponse response = electionService.closeNominations(electionId);
        return ApiResponse.success("Nominations closed successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_OPEN_VOTING)
    @Operation(summary = "Open voting", description = "Open the voting period. Requires at least 2 approved candidates.")
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> openVoting(@PathVariable Long electionId) {
        log.info("Opening voting for election: {}", electionId);
        ElectionResponse response = electionService.openVoting(electionId);
        return ApiResponse.success("Voting opened successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_CLOSE_VOTING)
    @Operation(summary = "Close voting", description = "Close the voting period for an election")
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<ElectionResponse> closeVoting(@PathVariable Long electionId) {
        log.info("Closing voting for election: {}", electionId);
        ElectionResponse response = electionService.closeVoting(electionId);
        return ApiResponse.success("Voting closed successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_PUBLISH_RESULTS)
    @Operation(summary = "Publish results", description = "Publish election results to all members")
    @PreAuthorize("hasAuthority('ELECTION:PUBLISH_RESULTS')")
    public ApiResponse<ElectionResponse> publishResults(@PathVariable Long electionId) {
        log.info("Publishing results for election: {}", electionId);
        ElectionResponse response = electionService.publishResults(electionId);
        return ApiResponse.success("Results published successfully", response);
    }

    @PostMapping(ApiConstants.ELECTION_CANCEL)
    @Operation(summary = "Cancel election", description = "Cancel an election with a reason")
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<Void> cancelElection(
            @PathVariable Long electionId,
            @Valid @RequestBody CancelElectionRequest request) {
        log.info("Cancelling election: {} with reason: {}", electionId, request.getReason());
        electionService.cancelElection(electionId, request.getReason());
        return ApiResponse.success("Election cancelled successfully", null);
    }

    // ==================== CANDIDATE ENDPOINTS ====================

    @PostMapping(value = ApiConstants.CANDIDATE_NOMINATE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Nominate self with photo", description = "Submit nomination with photo upload for an election")
    @PreAuthorize("hasAuthority('CANDIDATE:NOMINATE')")
    public ApiResponse<CandidateResponse> nominateSelf(
            @RequestParam("electionId") Long electionId,
            @RequestParam(value = "manifesto", required = false) String manifesto,
            @RequestParam(value = "slogan", required = false) String slogan,
            @RequestParam(value = "qualifications", required = false) String qualifications,
            @RequestParam(value = "previousExperience", required = false) String previousExperience,
            @RequestPart("photo") MultipartFile photo) {

        log.info("Self-nomination with photo for election: {}", electionId);

        // Build the nomination request from parameters
        NominateCandidateRequest request = NominateCandidateRequest.builder()
                .electionId(electionId)
                .manifesto(manifesto)
                .slogan(slogan)
                .qualifications(qualifications)
                .previousExperience(previousExperience)
                .build();

        CandidateResponse response = electionService.nominateSelf(request, photo);
        return ApiResponse.success("Nomination submitted successfully. Pending approval.", response);
    }

    @PutMapping(value = ApiConstants.CANDIDATE_UPDATE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update nominationSelf", description = "Update your nomination details with optional photo")
    @PreAuthorize("hasAuthority('CANDIDATE:NOMINATE')")
    public ApiResponse<CandidateResponse> updateNominationSelf(
            @PathVariable Long candidateId,
            @RequestParam(value = "manifesto", required = false) String manifesto,
            @RequestParam(value = "slogan", required = false) String slogan,
            @RequestParam(value = "qualifications", required = false) String qualifications,
            @RequestParam(value = "previousExperience", required = false) String previousExperience,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        log.info("Updating nominationSelf {} with photo", candidateId);

        UpdateCandidateRequest request = UpdateCandidateRequest.builder()
                .manifesto(manifesto)
                .slogan(slogan)
                .qualifications(qualifications)
                .previousExperience(previousExperience)
                .build();

        CandidateResponse response;
        response = electionService.updateNominationSelf(candidateId, request, photo);
        return ApiResponse.success("NominationSelf updated successfully", response);
    }

    @DeleteMapping(ApiConstants.CANDIDATE_DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete nomination self", description = "Delete your nomination before voting starts")
    @PreAuthorize("hasAuthority('CANDIDATE:NOMINATE')")
    public ApiResponse<Void> deleteNomination(@PathVariable Long candidateId) {
        log.info("Deleting nominationSelf: {}", candidateId);
        electionService.deleteNominationSelf(candidateId);
        return ApiResponse.success("Nomination deleted successfully", null);
    }

    @PostMapping(ApiConstants.CANDIDATE_REVIEW)
    @Operation(summary = "Review candidate nomination", description = "Approve or reject a candidate")
    @PreAuthorize("hasAuthority('CANDIDATE:APPROVE')")
    public ApiResponse<CandidateResponse> reviewCandidate(@Valid @RequestBody ReviewCandidateRequest request) {
        log.info("Reviewing candidate: {}, approved: {}", request.getCandidateId(), request.getApproved());
        CandidateResponse response = electionService.reviewCandidate(request);
        String message = request.getApproved() ? "Candidate approved successfully" : "Candidate rejected";
        return ApiResponse.success(message, response);
    }

    @PostMapping(ApiConstants.CANDIDATE_WITHDRAW)
    @Operation(summary = "Withdraw candidacy", description = "Withdraw your candidacy before voting starts")
    @PreAuthorize("hasAuthority('CANDIDATE:NOMINATE')")
    public ApiResponse<Void> withdrawCandidacy(@PathVariable Long candidateId) {
        log.info("Withdrawing candidacy: {}", candidateId);
        electionService.withdrawCandidacy(candidateId);
        return ApiResponse.success("Candidacy withdrawn successfully", null);
    }

    @GetMapping(ApiConstants.ELECTION_CANDIDATES)
    @Operation(summary = "Get candidates", description = "Get all candidates for an election")
    @PreAuthorize("hasAuthority('CANDIDATE:VIEW')")
    public ApiResponse<PagedResponse<CandidateResponse>> getCandidates(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getCandidates(electionId, pageable);
        return ApiResponse.success("Candidates retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CANDIDATES_APPROVED)
    @Operation(summary = "Get approved candidates", description = "Get only approved candidates for voting")
    @PreAuthorize("hasAuthority('CANDIDATE:VIEW')")
    public ApiResponse<PagedResponse<CandidateResponse>> getApprovedCandidates(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getApprovedCandidates(electionId, pageable);
        return ApiResponse.success("Approved candidates retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CANDIDATES_PENDING)
    @Operation(summary = "Get pending candidates", description = "Get pending candidates awaiting approval")
    @PreAuthorize("hasAuthority('CANDIDATE:APPROVE')")
    public ApiResponse<PagedResponse<CandidateResponse>> getPendingCandidates(
            @PathVariable Long electionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getPendingCandidates(electionId, pageable);
        return ApiResponse.success("Pending candidates retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CANDIDATE_BY_ID)
    @Operation(summary = "Get candidate by ID", description = "Get candidate details")
    @PreAuthorize("hasAuthority('CANDIDATE:VIEW')")
    public ApiResponse<CandidateResponse> getCandidateById(@PathVariable Long candidateId) {
        CandidateResponse response = electionService.getCandidateById(candidateId);
        return ApiResponse.success("Candidate retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MY_CANDIDACIES)
    @Operation(summary = "Get my candidacies", description = "Get all candidacies of the current user")
    @PreAuthorize("hasAuthority('CANDIDATE:NOMINATE')")
    public ApiResponse<PagedResponse<CandidateResponse>> getMyCandidacies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CandidateResponse> response = electionService.getMyCandidacies(pageable);
        return ApiResponse.success("Candidacies retrieved successfully", response);
    }

    // ==================== VOTING ENDPOINTS ====================

    @PostMapping(ApiConstants.VOTE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cast vote", description = "Cast your vote for a candidate")
    @PreAuthorize("hasAuthority('VOTE:CAST')")
    public ApiResponse<VoteResponse> castVote(
            @Valid @RequestBody CastVoteRequest request,
            HttpServletRequest httpRequest) {
        log.info("Vote being cast for election: {}", request.getElectionId());
        String ipAddress = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        VoteResponse response = electionService.castVote(request, ipAddress, userAgent);
        return ApiResponse.success("Vote cast successfully. Keep your verification token safe.", response);
    }

    @GetMapping(ApiConstants.HAS_VOTED)
    @Operation(summary = "Check if voted", description = "Check if the current user has voted")
    @PreAuthorize("hasAuthority('VOTE:CAST')")
    public ApiResponse<Boolean> hasVoted(@PathVariable Long electionId) {
        boolean voted = electionService.hasVoted(electionId);
        return ApiResponse.success("Vote status retrieved", voted);
    }

    @GetMapping(ApiConstants.VERIFY_VOTE)
    @Operation(summary = "Verify vote", description = "Verify your vote using the verification token")
    @PreAuthorize("hasAuthority('VOTE:CAST')")
    public ApiResponse<Boolean> verifyVote(
            @PathVariable Long electionId,
            @RequestParam String token) {
        boolean verified = electionService.verifyVote(electionId, token);
        String message = verified ? "Vote verified successfully" : "Vote verification failed";
        return ApiResponse.success(message, verified);
    }

    // ==================== RESULTS ENDPOINTS ====================

    @GetMapping(ApiConstants.ELECTION_RESULTS)
    @Operation(summary = "Get election results", description = "Get results after they are published")
    @PreAuthorize("hasAuthority('VOTE:VIEW_RESULTS')")
    public ApiResponse<ElectionResultsResponse> getElectionResults(@PathVariable Long electionId) {
        ElectionResultsResponse response = electionService.getElectionResults(electionId);
        return ApiResponse.success("Results retrieved successfully", response);
    }

    @GetMapping(ApiConstants.ELECTION_LIVE_COUNT)
    @Operation(summary = "Get live vote count", description = "Get real-time vote counts (Admin only)")
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<ElectionResultsResponse> getLiveVoteCount(@PathVariable Long electionId) {
        ElectionResultsResponse response = electionService.getLiveVoteCount(electionId);
        return ApiResponse.success("Live vote count retrieved successfully", response);
    }

    // ==================== HELPER METHODS ====================

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
