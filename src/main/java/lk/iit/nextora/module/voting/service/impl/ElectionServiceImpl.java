package lk.iit.nextora.module.voting.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.CandidateStatus;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.DuplicateResourceException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.NonAcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubService;
import lk.iit.nextora.module.voting.dto.request.*;
import lk.iit.nextora.module.voting.dto.response.*;
import lk.iit.nextora.module.voting.entity.*;
import lk.iit.nextora.module.voting.mapper.VotingMapper;
import lk.iit.nextora.module.voting.repository.*;
import lk.iit.nextora.module.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static lk.iit.nextora.common.util.FileUtils.MAX_IMAGE_SIZE;

/**
 * Service implementation for Election operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElectionServiceImpl implements ElectionService {

    private static final String CANDIDATE_PHOTO_FOLDER = "elections/candidates";

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final StudentRepository studentRepository;
    private final NonAcademicStaffRepository nonAcademicStaffRepository;
    private final SecurityService securityService;
    private final ClubService clubService;
    private final VotingMapper votingMapper;
    private final lk.iit.nextora.config.S3.S3Service s3Service;

    @Value("${app.voting.secret:nextora-voting-secret-2026}")
    private String votingSecret;

    // ==================== Election Management ====================

    @Override
    @Transactional
    public ElectionResponse createElection(CreateElectionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Creating election for club {}", request.getClubId());

        Club club = findClubById(request.getClubId());
        validateClubAdminAccess();
        validateElectionSchedule(request);

        NonAcademicStaff creator = findNonAcademicStaffById(currentUserId);

        Election election = votingMapper.toEntity(request);
        election.setClub(club);
        election.setCreatedBy(creator);
        election.setStatus(ElectionStatus.DRAFT);

        election = electionRepository.save(election);
        log.info("Election created: {} (ID: {})", election.getTitle(), election.getId());

        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public ElectionResponse updateElection(Long electionId, UpdateElectionRequest request) {
        log.info("Updating election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (!election.canModify()) {
            throw new BadRequestException("Election cannot be modified in current status: " + election.getStatus());
        }

        votingMapper.updateElectionFromRequest(request, election);
        election = electionRepository.save(election);

        log.info("Election updated: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    public ElectionResponse getElectionById(Long electionId) {
        Election election = findElectionById(electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    public ElectionResponse getElectionWithCandidates(Long electionId) {
        Election election = electionRepository.findByIdWithDetails(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election", "id", electionId));

        ElectionResponse response = enrichElectionResponse(votingMapper.toResponse(election), election);

        // Add approved candidates
        List<Candidate> approvedCandidates = candidateRepository
                .findByElectionIdAndStatusAndIsDeletedFalseOrderByDisplayOrderAsc(electionId, CandidateStatus.APPROVED);
        response.setCandidates(votingMapper.toCandidateResponseList(approvedCandidates));

        return response;
    }

    @Override
    public PagedResponse<ElectionResponse> getElectionsByClub(Long clubId, Pageable pageable) {
        Page<Election> elections = electionRepository.findByClubIdAndIsDeletedFalse(clubId, pageable);
        return toPagedResponse(elections);
    }

    @Override
    public PagedResponse<ElectionResponse> getElectionsByStatus(ElectionStatus status, Pageable pageable) {
        Page<Election> elections = electionRepository.findByStatusAndIsDeletedFalse(status, pageable);
        return toPagedResponse(elections);
    }

    @Override
    public PagedResponse<ElectionResponse> getUpcomingElections(Pageable pageable) {
        Page<Election> elections = electionRepository.findAllUpcoming(LocalDateTime.now(), pageable);
        return toPagedResponse(elections);
    }

    @Override
    public PagedResponse<ElectionResponse> getUpcomingElectionsByClub(Long clubId, Pageable pageable) {
        Page<Election> elections = electionRepository.findUpcomingByClub(clubId, LocalDateTime.now(), pageable);
        return toPagedResponse(elections);
    }

    @Override
    public PagedResponse<ElectionResponse> getVotableElections(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Election> elections = electionRepository.findVotableElectionsForMember(currentUserId, pageable);
        return toPagedResponse(elections);
    }

    @Override
    public PagedResponse<ElectionResponse> searchElections(String keyword, Pageable pageable) {
        Page<Election> elections = electionRepository.searchByKeyword(keyword, pageable);
        return toPagedResponse(elections);
    }

    @Override
    @Transactional
    public void deleteElection(Long electionId) {
        log.info("Deleting election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new BadRequestException("Only draft elections can be deleted");
        }

        election.softDelete();
        electionRepository.save(election);

        log.info("Election deleted: {}", electionId);
    }

    // ==================== Election Lifecycle ====================

    @Override
    @Transactional
    public ElectionResponse openNominations(Long electionId) {
        log.info("Opening nominations for election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new BadRequestException("Can only open nominations for draft elections");
        }

        election.openNominations();
        election = electionRepository.save(election);

        log.info("Nominations opened for election: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public ElectionResponse closeNominations(Long electionId) {
        log.info("Closing nominations for election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.NOMINATION_OPEN) {
            throw new BadRequestException("Nominations are not open");
        }

        election.closeNominations();
        election = electionRepository.save(election);

        log.info("Nominations closed for election: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public ElectionResponse openVoting(Long electionId) {
        log.info("Opening voting for election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.NOMINATION_CLOSED) {
            throw new BadRequestException("Nominations must be closed before opening voting");
        }

        // Ensure there are approved candidates
        long approvedCount = candidateRepository.countByElectionIdAndStatusAndIsDeletedFalse(
                electionId, CandidateStatus.APPROVED);
        if (approvedCount < 2) {
            throw new BadRequestException("At least 2 approved candidates required to start voting");
        }

        election.openVoting();
        election = electionRepository.save(election);

        log.info("Voting opened for election: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public ElectionResponse closeVoting(Long electionId) {
        log.info("Closing voting for election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.VOTING_OPEN) {
            throw new BadRequestException("Voting is not open");
        }

        election.closeVoting();
        election = electionRepository.save(election);

        log.info("Voting closed for election: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public ElectionResponse publishResults(Long electionId) {
        log.info("Publishing results for election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() != ElectionStatus.VOTING_CLOSED) {
            throw new BadRequestException("Voting must be closed before publishing results");
        }

        election.publishResults();
        election = electionRepository.save(election);

        log.info("Results published for election: {}", electionId);
        return enrichElectionResponse(votingMapper.toResponse(election), election);
    }

    @Override
    @Transactional
    public void cancelElection(Long electionId, String reason) {
        log.info("Cancelling election: {}", electionId);

        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        if (election.getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot cancel election after results are published");
        }

        election.cancel(reason);
        electionRepository.save(election);

        log.info("Election cancelled: {}", electionId);
    }

    // ==================== Candidate Operations ====================

    @Override
    @Transactional
    public CandidateResponse nominateSelf(NominateCandidateRequest request, MultipartFile photo) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} nominating self with photo for election {}", currentUserId, request.getElectionId());

        // Validate photo file
        validatePhotoFile(photo);

        Election election = findElectionById(request.getElectionId());

        // Validate election is accepting nominations
        if (!election.isNominationOpen()) {
            throw new BadRequestException("Nominations are not currently open for this election");
        }

        // Validate user is eligible (active club member)
        if (!clubService.canNominateInClub(election.getClub().getId(), currentUserId)) {
            throw new BadRequestException("You are not eligible to nominate for this election. " +
                    "Must be an active member for at least 3 months.");
        }

        // Check if already nominated
        if (candidateRepository.existsByElectionIdAndStudentIdAndIsDeletedFalse(request.getElectionId(), currentUserId)) {
            throw new DuplicateResourceException("Candidate", "election and student",
                    request.getElectionId() + ", " + currentUserId);
        }

        // Check max candidates
        long currentCandidates = candidateRepository.countByElectionIdAndIsDeletedFalse(request.getElectionId());
        if (currentCandidates >= election.getMaxCandidates()) {
            throw new BadRequestException("Maximum number of candidates reached");
        }

        // Validate manifesto if required
        if (election.getRequireManifesto() &&
            (request.getManifesto() == null || request.getManifesto().isBlank())) {
            throw new BadRequestException("Manifesto is required for this election");
        }

        Candidate candidate = votingMapper.toEntity(request);

        // Upload logo to S3 if provided
        if (!photo.isEmpty()) {
            validateLogoFile(photo);
            String photoUrl = s3Service.uploadFilePublic(photo, CANDIDATE_PHOTO_FOLDER);
            log.info("Candidate photo uploaded to S3: {}", photoUrl);
            candidate.setPhotoUrl(photoUrl);
        }

        Student student = findStudentById(currentUserId);

        candidate.setElection(election);
        candidate.setStudent(student);
        candidate.setNominatedBy(student);
        candidate.setNominatedAt(LocalDateTime.now());
        candidate.setStatus(CandidateStatus.PENDING);
        candidate.setDisplayOrder((int) currentCandidates + 1);

        candidate = candidateRepository.save(candidate);
        log.info("Candidate nomination with photo created: {} for election {}", candidate.getId(), request.getElectionId());

        return votingMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public CandidateResponse updateNominationSelf(Long candidateId, UpdateCandidateRequest request, MultipartFile photo) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} updating nomination {} with photo", currentUserId, candidateId);

        Candidate candidate = findCandidateById(candidateId);

        // Validate ownership - only the candidate can update their nomination
        if (!candidate.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only update your own nomination");
        }

        // Validate election status - cannot update after voting starts
        Election election = candidate.getElection();
        if (election.getStatus() == ElectionStatus.VOTING_OPEN ||
            election.getStatus() == ElectionStatus.VOTING_CLOSED ||
            election.getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot update nomination after voting has started");
        }

        // Validate candidate status
        if (candidate.getStatus() == CandidateStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot update a withdrawn nomination");
        }
        if (candidate.getStatus() == CandidateStatus.REJECTED) {
            throw new BadRequestException("Cannot update a rejected nomination");
        }

        // Update fields if provided
        if (request.getManifesto() != null) {
            candidate.setManifesto(request.getManifesto());
        }
        if (request.getSlogan() != null) {
            candidate.setSlogan(request.getSlogan());
        }
        if (request.getQualifications() != null) {
            candidate.setQualifications(request.getQualifications());
        }
        if (request.getPreviousExperience() != null) {
            candidate.setPreviousExperience(request.getPreviousExperience());
        }

        // Handle photo update
        if (photo != null && !photo.isEmpty()) {
            validatePhotoFile(photo);

            // Delete old photo if exists
            if (candidate.getPhotoUrl() != null && !candidate.getPhotoUrl().isEmpty()) {
                try {
                    String oldKey = extractS3KeyFromUrl(candidate.getPhotoUrl());
                    if (oldKey != null) {
                        s3Service.deleteFile(oldKey);
                        log.info("Deleted old candidate photo: {}", oldKey);
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete old candidate photo: {}", e.getMessage());
                }
            }

            // Upload new photo
            String photoUrl = s3Service.uploadFilePublic(photo, CANDIDATE_PHOTO_FOLDER);
            log.info("New candidate photo uploaded to S3: {}", photoUrl);
            candidate.setPhotoUrl(photoUrl);
        }

        // If candidate was approved and updates details, reset to pending for re-review
        if (candidate.getStatus() == CandidateStatus.APPROVED) {
            candidate.setStatus(CandidateStatus.PENDING);
            candidate.setReviewedAt(null);
            candidate.setReviewedBy(null);
            log.info("Candidate {} status reset to PENDING due to nomination update", candidateId);
        }

        candidate = candidateRepository.save(candidate);
        log.info("Nomination with photo updated successfully: {}", candidateId);

        return votingMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public void deleteNominationSelf(Long candidateId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} deleting nomination {}", currentUserId, candidateId);

        Candidate candidate = findCandidateById(candidateId);

        // Validate ownership - only the candidate can delete their nomination
        if (!candidate.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only delete your own nomination");
        }

        // Validate election status - cannot delete after voting starts
        Election election = candidate.getElection();
        if (election.getStatus() == ElectionStatus.VOTING_OPEN ||
            election.getStatus() == ElectionStatus.VOTING_CLOSED ||
            election.getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot delete nomination after voting has started");
        }

        // Delete photo from S3 if exists
        if (candidate.getPhotoUrl() != null && !candidate.getPhotoUrl().isEmpty()) {
            try {
                String key = extractS3KeyFromUrl(candidate.getPhotoUrl());
                if (key != null) {
                    s3Service.deleteFile(key);
                    log.info("Deleted candidate photo from S3: {}", key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete candidate photo from S3: {}", e.getMessage());
            }
        }

        // Soft delete the nomination
        candidate.setIsDeleted(true);
        candidate.setDeletedAt(LocalDateTime.now());
        candidate.setDeletedBy(currentUserId);
        candidateRepository.save(candidate);

        log.info("Nomination deleted successfully: {}", candidateId);
    }

    private void validateLogoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Logo file is required");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Logo file size must not exceed 5MB");
        }

        if (!lk.iit.nextora.common.util.FileUtils.isImageFile(file)) {
            throw new BadRequestException("Only image files (JPEG, PNG, GIF, WEBP) are allowed");
        }
    }

    @Override
    @Transactional
    public CandidateResponse uploadCandidatePhoto(Long candidateId, MultipartFile photo) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Uploading photo for candidate: {}", candidateId);

        // Validate photo file
        validatePhotoFile(photo);

        Candidate candidate = findCandidateById(candidateId);

        // Only the candidate or admin can upload photo
        if (!candidate.getStudent().getId().equals(currentUserId) && !securityService.isAdmin()) {
            throw new UnauthorizedException("You can only upload photo for your own candidacy");
        }

        // Cannot change photo after voting starts
        if (candidate.getElection().getStatus() == ElectionStatus.VOTING_OPEN ||
            candidate.getElection().getStatus() == ElectionStatus.VOTING_CLOSED ||
            candidate.getElection().getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot change photo after voting has started");
        }

        // Delete old photo if exists
        if (candidate.getPhotoUrl() != null && !candidate.getPhotoUrl().isEmpty()) {
            try {
                String oldKey = extractS3KeyFromUrl(candidate.getPhotoUrl());
                if (oldKey != null) {
                    s3Service.deleteFile(oldKey);
                    log.info("Deleted old candidate photo: {}", oldKey);
                }
            } catch (Exception e) {
                log.warn("Failed to delete old candidate photo: {}", e.getMessage());
            }
        }

        // Upload new photo to S3
        String photoUrl = s3Service.uploadFilePublic(photo, CANDIDATE_PHOTO_FOLDER);
        log.info("New candidate photo uploaded to S3: {}", photoUrl);

        candidate.setPhotoUrl(photoUrl);
        candidate = candidateRepository.save(candidate);

        return votingMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public CandidateResponse deleteCandidatePhoto(Long candidateId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Deleting photo for candidate: {}", candidateId);

        Candidate candidate = findCandidateById(candidateId);

        // Only the candidate or admin can delete photo
        if (!candidate.getStudent().getId().equals(currentUserId) && !securityService.isAdmin()) {
            throw new UnauthorizedException("You can only delete photo for your own candidacy");
        }

        // Cannot change photo after voting starts
        if (candidate.getElection().getStatus() == ElectionStatus.VOTING_OPEN ||
            candidate.getElection().getStatus() == ElectionStatus.VOTING_CLOSED ||
            candidate.getElection().getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot change photo after voting has started");
        }

        if (candidate.getPhotoUrl() != null && !candidate.getPhotoUrl().isEmpty()) {
            try {
                String key = extractS3KeyFromUrl(candidate.getPhotoUrl());
                if (key != null) {
                    s3Service.deleteFile(key);
                    log.info("Deleted candidate photo: {}", key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete photo from S3: {}", e.getMessage());
            }

            candidate.setPhotoUrl(null);
            candidate = candidateRepository.save(candidate);
        }

        return votingMapper.toResponse(candidate);
    }

    private void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Photo file is required");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Photo file size must not exceed 5MB");
        }

        if (!lk.iit.nextora.common.util.FileUtils.isImageFile(file)) {
            throw new BadRequestException("Only image files (JPEG, PNG, GIF, WEBP) are allowed");
        }
    }

    private String extractS3KeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // URL format: https://bucket-name.s3.region.amazonaws.com/folder/filename
        try {
            String[] parts = url.split(".amazonaws.com/");
            if (parts.length > 1) {
                return parts[1];
            }
        } catch (Exception e) {
            log.warn("Failed to extract S3 key from URL: {}", url);
        }
        return null;
    }

    @Override
    @Transactional
    public CandidateResponse reviewCandidate(ReviewCandidateRequest request) {
        log.info("Reviewing candidate: {}", request.getCandidateId());

        Candidate candidate = findCandidateById(request.getCandidateId());
        validateClubAdminAccess();

        if (candidate.getStatus() != CandidateStatus.PENDING) {
            throw new BadRequestException("Candidate is not pending review");
        }

        BaseUser reviewer = securityService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        if (request.getApproved()) {
            candidate.approve(reviewer);
            log.info("Candidate approved: {}", request.getCandidateId());
        } else {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            candidate.reject(reviewer, request.getRejectionReason());
            log.info("Candidate rejected: {}", request.getCandidateId());
        }

        candidate = candidateRepository.save(candidate);
        return votingMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public void withdrawCandidacy(Long candidateId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} withdrawing candidacy {}", currentUserId, candidateId);

        Candidate candidate = findCandidateById(candidateId);

        // Only the candidate can withdraw
        if (!candidate.getStudent().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only withdraw your own candidacy");
        }

        // Cannot withdraw after voting starts
        if (candidate.getElection().getStatus() == ElectionStatus.VOTING_OPEN ||
            candidate.getElection().getStatus() == ElectionStatus.VOTING_CLOSED ||
            candidate.getElection().getStatus() == ElectionStatus.RESULTS_PUBLISHED) {
            throw new BadRequestException("Cannot withdraw after voting has started");
        }

        candidate.withdraw();
        candidateRepository.save(candidate);

        log.info("Candidacy withdrawn: {}", candidateId);
    }

    @Override
    public PagedResponse<CandidateResponse> getCandidates(Long electionId, Pageable pageable) {
        Page<Candidate> candidates = candidateRepository.findByElectionIdAndIsDeletedFalse(electionId, pageable);
        return toCandidatePagedResponse(candidates);
    }

    @Override
    public PagedResponse<CandidateResponse> getApprovedCandidates(Long electionId, Pageable pageable) {
        Page<Candidate> candidates = candidateRepository
                .findByElectionIdAndStatusAndIsDeletedFalse(electionId, CandidateStatus.APPROVED, pageable);
        return toCandidatePagedResponse(candidates);
    }

    @Override
    public PagedResponse<CandidateResponse> getPendingCandidates(Long electionId, Pageable pageable) {
        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        Page<Candidate> candidates = candidateRepository.findPendingCandidates(electionId, pageable);
        return toCandidatePagedResponse(candidates);
    }

    @Override
    public CandidateResponse getCandidateById(Long candidateId) {
        Candidate candidate = findCandidateById(candidateId);
        return votingMapper.toResponse(candidate);
    }

    @Override
    public PagedResponse<CandidateResponse> getMyCandidacies(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<Candidate> candidates = candidateRepository.findByStudentIdAndIsDeletedFalse(currentUserId, pageable);
        return toCandidatePagedResponse(candidates);
    }

    // ==================== Voting Operations ====================

    @Override
    @Transactional
    public VoteResponse castVote(CastVoteRequest request, String ipAddress, String userAgent) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} casting vote in election {}", currentUserId, request.getElectionId());

        Election election = findElectionById(request.getElectionId());

        // Validate voting is open
        if (!election.isVotingOpen()) {
            throw new BadRequestException("Voting is not currently open for this election");
        }

        // Validate user is eligible to vote
        if (!clubService.canVoteInClub(election.getClub().getId(), currentUserId)) {
            throw new BadRequestException("You are not eligible to vote in this election");
        }

        // Check if already voted
        String voteHash = Vote.generateVoteHash(request.getElectionId(), currentUserId, votingSecret);
        if (voteRepository.existsByElectionIdAndVoteHashAndIsDeletedFalse(request.getElectionId(), voteHash)) {
            throw new BadRequestException("You have already voted in this election");
        }

        // Validate candidate
        Candidate candidate = findCandidateById(request.getCandidateId());
        if (!candidate.getElection().getId().equals(request.getElectionId())) {
            throw new BadRequestException("Candidate does not belong to this election");
        }
        if (!candidate.canReceiveVotes()) {
            throw new BadRequestException("Selected candidate cannot receive votes");
        }

        Student voter = findStudentById(currentUserId);

        // Create vote
        Vote vote = Vote.builder()
                .election(election)
                .candidate(candidate)
                .voter(election.getIsAnonymousVoting() ? null : voter)
                .voteHash(voteHash)
                .votedAt(LocalDateTime.now())
                .voterIpAddress(ipAddress)
                .userAgent(userAgent)
                .verificationToken(Vote.generateVerificationToken())
                .isVerified(false)
                .build();

        vote = voteRepository.save(vote);

        // Increment candidate vote count
        candidateRepository.incrementVoteCount(candidate.getId());

        log.info("Vote cast successfully for election {} by user {}", request.getElectionId(), currentUserId);

        VoteResponse response = votingMapper.toResponse(vote);
        if (election.getIsAnonymousVoting()) {
            response.setCandidateId(null);
            response.setCandidateName(null);
        }
        return response;
    }

    @Override
    public boolean hasVoted(Long electionId) {
        Long currentUserId = securityService.getCurrentUserId();
        String voteHash = Vote.generateVoteHash(electionId, currentUserId, votingSecret);
        return voteRepository.existsByElectionIdAndVoteHashAndIsDeletedFalse(electionId, voteHash);
    }

    @Override
    public boolean verifyVote(Long electionId, String verificationToken) {
        return voteRepository.verifyVote(electionId, verificationToken);
    }

    // ==================== Results Operations ====================

    @Override
    public ElectionResultsResponse getElectionResults(Long electionId) {
        Election election = findElectionById(electionId);

        // Only show results if published
        if (!election.canViewResults()) {
            throw new BadRequestException("Election results are not yet available");
        }

        return buildElectionResults(election, true);
    }

    @Override
    public ElectionResultsResponse getLiveVoteCount(Long electionId) {
        Election election = findElectionById(electionId);
        validateClubAdminAccess();

        return buildElectionResults(election, false);
    }

    private ElectionResultsResponse buildElectionResults(Election election, boolean includeStatistics) {
        List<Candidate> rankedCandidates = candidateRepository.findApprovedCandidatesOrderByVotes(election.getId());

        long totalVotes = voteRepository.countByElectionIdAndIsDeletedFalse(election.getId());
        long eligibleVoters = membershipRepository.countActiveMembers(election.getClub().getId(), LocalDate.now());

        // Calculate percentages and ranks
        List<CandidateResponse> rankedResponses = new ArrayList<>();
        int rank = 1;
        for (Candidate candidate : rankedCandidates) {
            CandidateResponse response = votingMapper.toResponse(candidate);
            response.setRank(rank++);
            response.setVotePercentage(totalVotes > 0 ?
                    (double) candidate.getVoteCount() / totalVotes * 100 : 0.0);
            response.setIsWinner(rank <= election.getWinnersCount() + 1);
            rankedResponses.add(response);
        }

        // Get winners
        List<CandidateResponse> winners = rankedResponses.stream()
                .limit(election.getWinnersCount())
                .toList();

        ElectionResultsResponse.ElectionResultsResponseBuilder builder = ElectionResultsResponse.builder()
                .electionId(election.getId())
                .electionTitle(election.getTitle())
                .clubName(election.getClub().getName())
                .resultsPublishedAt(election.getResultsPublishedAt())
                .totalVotes((int) totalVotes)
                .eligibleVoters((int) eligibleVoters)
                .participationRate(eligibleVoters > 0 ? (double) totalVotes / eligibleVoters * 100 : 0.0)
                .winnersCount(election.getWinnersCount())
                .winners(winners)
                .rankedCandidates(rankedResponses);

        if (includeStatistics) {
            builder.statistics(buildVotingStatistics(election.getId()));
        }

        return builder.build();
    }

    private ElectionResultsResponse.VotingStatistics buildVotingStatistics(Long electionId) {
        List<Object[]> votesByDate = voteRepository.getVotingStatsByDate(electionId);
        List<Object[]> votesByHour = voteRepository.getVotingStatsByHour(electionId);

        Map<String, Long> dateMap = votesByDate.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue()
                ));

        Map<Integer, Long> hourMap = votesByHour.stream()
                .collect(Collectors.toMap(
                        arr -> ((Number) arr[0]).intValue(),
                        arr -> ((Number) arr[1]).longValue()
                ));

        return ElectionResultsResponse.VotingStatistics.builder()
                .votesByDate(dateMap)
                .votesByHour(hourMap)
                .build();
    }

    // ==================== Scheduled Tasks ====================

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processElectionStatusUpdates() {
        LocalDateTime now = LocalDateTime.now();

        // Close nominations that have ended
        List<Election> nominationsToClose = electionRepository.findElectionsNeedingNominationClose(now);
        for (Election election : nominationsToClose) {
            election.closeNominations();
            electionRepository.save(election);
            log.info("Auto-closed nominations for election: {}", election.getId());
        }

        // Open voting for elections ready to start
        List<Election> votingToOpen = electionRepository.findElectionsNeedingVotingOpen(now);
        for (Election election : votingToOpen) {
            long approvedCount = candidateRepository.countByElectionIdAndStatusAndIsDeletedFalse(
                    election.getId(), CandidateStatus.APPROVED);
            if (approvedCount >= 2) {
                election.openVoting();
                electionRepository.save(election);
                log.info("Auto-opened voting for election: {}", election.getId());
            }
        }

        // Close voting that has ended
        List<Election> votingToClose = electionRepository.findElectionsNeedingVotingClose(now);
        for (Election election : votingToClose) {
            election.closeVoting();
            electionRepository.save(election);
            log.info("Auto-closed voting for election: {}", election.getId());
        }
    }

    // ==================== Helper Methods ====================

    private Election findElectionById(Long electionId) {
        return electionRepository.findByIdAndIsDeletedFalse(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election", "id", electionId));
    }

    private Candidate findCandidateById(Long candidateId) {
        return candidateRepository.findByIdAndIsDeletedFalse(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));
    }

    private Club findClubById(Long clubId) {
        return clubRepository.findByIdAndIsDeletedFalse(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private NonAcademicStaff findNonAcademicStaffById(Long nonAcademicStaffId) {
        return nonAcademicStaffRepository.findById(nonAcademicStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("Non Academic Staff", "id", nonAcademicStaffId));
    }

    private void validateClubAdminAccess() {
        boolean isAdmin = securityService.isAdmin();
        boolean isSuperAdmin = securityService.isSuperAdmin();
        boolean isNonAcademicStaff = securityService.isNonAcademicstaff();

        if (!isSuperAdmin && !isAdmin && !isNonAcademicStaff) {
            throw new UnauthorizedException("You don't have permission to manage elections for this club");
        }
    }

    private void validateElectionSchedule(CreateElectionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        if (request.getNominationStartTime().isBefore(now)) {
            throw new BadRequestException("Nomination start time must be in the future");
        }
        if (request.getNominationEndTime().isBefore(request.getNominationStartTime())) {
            throw new BadRequestException("Nomination end time must be after start time");
        }
        if (request.getVotingStartTime().isBefore(request.getNominationEndTime())) {
            throw new BadRequestException("Voting start time must be after nomination end time");
        }
        if (request.getVotingEndTime().isBefore(request.getVotingStartTime())) {
            throw new BadRequestException("Voting end time must be after start time");
        }
    }

    private ElectionResponse enrichElectionResponse(ElectionResponse response, Election election) {
        long eligibleVoters = membershipRepository.countActiveMembers(election.getClub().getId(), LocalDate.now());
        response.setEligibleVoters((int) eligibleVoters);

        if (response.getTotalVotes() != null && eligibleVoters > 0) {
            response.setParticipationRate((double) response.getTotalVotes() / eligibleVoters * 100);
        }

        return response;
    }

    private PagedResponse<ElectionResponse> toPagedResponse(Page<Election> page) {
        return PagedResponse.<ElectionResponse>builder()
                .content(page.getContent().stream()
                        .map(election -> enrichElectionResponse(votingMapper.toResponse(election), election))
                        .toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    private PagedResponse<CandidateResponse> toCandidatePagedResponse(Page<Candidate> page) {
        return PagedResponse.<CandidateResponse>builder()
                .content(votingMapper.toCandidateResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
