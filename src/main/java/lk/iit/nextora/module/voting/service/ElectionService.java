package lk.iit.nextora.module.voting.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.module.voting.dto.request.*;
import lk.iit.nextora.module.voting.dto.response.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Election operations
 */
public interface ElectionService {

    // ==================== Election Management ====================

    /**
     * Create a new election (Club Admin/President)
     */
    ElectionResponse createElection(CreateElectionRequest request);

    /**
     * Update election (Club Admin/President)
     */
    ElectionResponse updateElection(Long electionId, UpdateElectionRequest request);

    /**
     * Get election by ID
     */
    ElectionResponse getElectionById(Long electionId);

    /**
     * Get election with candidates
     */
    ElectionResponse getElectionWithCandidates(Long electionId);

    /**
     * Get elections by club
     */
    PagedResponse<ElectionResponse> getElectionsByClub(Long clubId, Pageable pageable);

    /**
     * Get elections by status
     */
    PagedResponse<ElectionResponse> getElectionsByStatus(ElectionStatus status, Pageable pageable);

    /**
     * Get upcoming elections
     */
    PagedResponse<ElectionResponse> getUpcomingElections(Pageable pageable);

    /**
     * Get upcoming elections for a club
     */
    PagedResponse<ElectionResponse> getUpcomingElectionsByClub(Long clubId, Pageable pageable);

    /**
     * Get elections where current user can vote
     */
    PagedResponse<ElectionResponse> getVotableElections(Pageable pageable);

    /**
     * Search elections
     */
    PagedResponse<ElectionResponse> searchElections(String keyword, Pageable pageable);

    /**
     * Delete election (Club Admin/President - only DRAFT status)
     */
    void deleteElection(Long electionId);

    // ==================== Election Lifecycle Management ====================

    /**
     * Open nominations for election
     */
    ElectionResponse openNominations(Long electionId);

    /**
     * Close nominations
     */
    ElectionResponse closeNominations(Long electionId);

    /**
     * Open voting
     */
    ElectionResponse openVoting(Long electionId);

    /**
     * Close voting
     */
    ElectionResponse closeVoting(Long electionId);

    /**
     * Publish results
     */
    ElectionResponse publishResults(Long electionId);

    /**
     * Cancel election
     */
    void cancelElection(Long electionId, String reason);

    // ==================== Candidate Operations ====================

    /**
     * Nominate self as candidate
     */
    CandidateResponse nominateSelf(NominateCandidateRequest request, MultipartFile photo);

    /**
     * Update own nomination details
     */
    CandidateResponse updateNominationSelf(Long candidateId, UpdateCandidateRequest request, MultipartFile photo);

    /**
     * Delete own nomination
     */
    void deleteNominationSelf(Long candidateId);

    /**
     * Upload or update candidate photo
     */
    CandidateResponse uploadCandidatePhoto(Long candidateId, MultipartFile photo);

    /**
     * Delete candidate photo
     */
    CandidateResponse deleteCandidatePhoto(Long candidateId);

    /**
     * Review candidate nomination (approve/reject)
     */
    CandidateResponse reviewCandidate(ReviewCandidateRequest request);

    /**
     * Withdraw candidacy
     */
    void withdrawCandidacy(Long candidateId);

    /**
     * Get candidates for election
     */
    PagedResponse<CandidateResponse> getCandidates(Long electionId, Pageable pageable);

    /**
     * Get approved candidates for election
     */
    PagedResponse<CandidateResponse> getApprovedCandidates(Long electionId, Pageable pageable);

    /**
     * Get pending candidates for review
     */
    PagedResponse<CandidateResponse> getPendingCandidates(Long electionId, Pageable pageable);

    /**
     * Get candidate by ID
     */
    CandidateResponse getCandidateById(Long candidateId);

    /**
     * Get my candidacies
     */
    PagedResponse<CandidateResponse> getMyCandidacies(Pageable pageable);

    // ==================== Voting Operations ====================

    /**
     * Cast vote
     */
    VoteResponse castVote(CastVoteRequest request, String ipAddress, String userAgent);

    /**
     * Check if user has voted in election
     */
    boolean hasVoted(Long electionId);

    /**
     * Verify vote by token
     */
    boolean verifyVote(Long electionId, String verificationToken);

    // ==================== Results Operations ====================

    /**
     * Get election results
     */
    ElectionResultsResponse getElectionResults(Long electionId);

    /**
     * Get live vote count (for admins during voting)
     */
    ElectionResultsResponse getLiveVoteCount(Long electionId);

    // ==================== Scheduled Tasks ====================

    /**
     * Process elections needing status updates (scheduled task)
     */
    void processElectionStatusUpdates();
}
