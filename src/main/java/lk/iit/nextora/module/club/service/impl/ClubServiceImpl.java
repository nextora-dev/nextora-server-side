package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.*;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.DuplicateResourceException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.AcademicStaffRepository;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.request.JoinClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.dto.response.ClubStatisticsResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.entity.ClubMembership;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubAnnouncementRepository;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.module.club.service.ClubService;
import lk.iit.nextora.module.election.repository.ElectionRepository;
import lk.iit.nextora.infrastructure.notification.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static lk.iit.nextora.common.util.FileUtils.MAX_IMAGE_SIZE;

/**
 * Service implementation for Club operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubServiceImpl implements ClubService {

    private static final String CLUB_LOGO_FOLDER = "clubs/logos";

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ClubAnnouncementRepository announcementRepository;
    private final ElectionRepository electionRepository;
    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final SecurityService securityService;
    private final ClubMapper clubMapper;
    private final ClubActivityLogService activityLogService;
    private final lk.iit.nextora.config.S3.S3Service s3Service;
    private final PushNotificationService pushNotificationService;

    // ==================== Club Management ====================

    @Override
    @Transactional
    public ClubResponse createClub(CreateClubRequest request, MultipartFile logo) {
        log.info("Creating new club: {}", request.getClubCode());

        // Validate unique club code
        if (clubRepository.existsByClubCodeAndIsDeletedFalse(request.getClubCode())) {
            throw new DuplicateResourceException("Club", "clubCode", request.getClubCode());
        }

        // Validate unique name
        if (clubRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new DuplicateResourceException("Club", "name", request.getName());
        }

        Club club = clubMapper.toEntity(request);

        // Upload logo to S3 if provided
        if (logo != null && !logo.isEmpty()) {
            validateLogoFile(logo);
            String logoUrl = s3Service.uploadFilePublic(logo, CLUB_LOGO_FOLDER);
            log.info("Club logo uploaded to S3: {}", logoUrl);
            club.setLogoUrl(logoUrl);
        }

        // Set president if provided
        if (request.getPresidentId() != null) {
            Student president = findStudentById(request.getPresidentId());
            validatePresidentEligibility(president);
            club.setPresident(president);
        }

        // Set advisor if provided
        if (request.getAdvisorId() != null) {
            AcademicStaff advisor = findAcademicStaffById(request.getAdvisorId());
            club.setAdvisor(advisor);
        }

        club = clubRepository.save(club);
        log.info("Club created successfully: {} (ID: {})", club.getName(), club.getId());

        return enrichClubResponse(clubMapper.toResponse(club), club.getId(), club);
    }

    @Override
    @Transactional
    public void deleteClub(Long clubId) {
        log.info("Deleting club: {}", clubId);

        Club club = findClubById(clubId);

        // Validate permissions (must be admin or club president)
        validateClubAdminAccess(club);

        // Delete logo from S3 if exists
        if (club.getLogoUrl() != null && !club.getLogoUrl().isEmpty()) {
            try {
                String key = extractS3KeyFromUrl(club.getLogoUrl());

                if (key != null) {
                    s3Service.deleteFile(key);
                    log.info("Deleted club logo from S3: {}", key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete logo from S3: {}", e.getMessage());
            }
        }

        // Soft delete the club
        club.softDelete();
        clubRepository.save(club);
        log.info("Club deleted successfully: {}", clubId);
    }

    @Override
    @Transactional
    public void permanentlyDeleteClub(Long clubId) {
        log.info("Super Admin permanently deleting club: {}", clubId);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));

        // 1. Delete all announcement attachments from S3
        var announcements = announcementRepository.findByClubId(clubId);
        for (var announcement : announcements) {
            if (announcement.getAttachmentUrl() != null && !announcement.getAttachmentUrl().isEmpty()) {
                try {
                    String key = extractS3KeyFromUrl(announcement.getAttachmentUrl());
                    if (key != null) {
                        s3Service.deleteFile(key);
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete announcement attachment from S3: {}", e.getMessage());
                }
            }
        }

        // 2. Delete club logo from S3
        if (club.getLogoUrl() != null && !club.getLogoUrl().isEmpty()) {
            try {
                String key = extractS3KeyFromUrl(club.getLogoUrl());
                if (key != null) {
                    s3Service.deleteFile(key);
                    log.info("Deleted club logo from S3: {}", key);
                }
            } catch (Exception e) {
                log.warn("Failed to delete club logo from S3: {}", e.getMessage());
            }
        }

        // 3. Delete all related records (order matters for FK constraints)
        announcementRepository.deleteByClubId(clubId);
        membershipRepository.deleteByClubId(clubId);
        activityLogService.deleteByClubId(clubId);
        // Elections cascade via Club entity (CascadeType.ALL + orphanRemoval)

        // 4. Permanently delete the club itself
        clubRepository.delete(club);

        log.info("Super Admin permanently deleted club: {} (name: {})", clubId, club.getName());
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
    public ClubResponse getClubById(Long clubId) {
        Club club = clubRepository.findByIdWithOfficers(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        return enrichClubResponse(clubMapper.toResponse(club), clubId, club);
    }

    @Override
    public ClubResponse getClubByCode(String clubCode) {
        Club club = clubRepository.findByClubCodeWithOfficers(clubCode)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "clubCode", clubCode));
        return enrichClubResponse(clubMapper.toResponse(club), club.getId(), club);
    }

    @Override
    public PagedResponse<ClubResponse> getAllClubs(Pageable pageable) {
        Page<Club> clubs = clubRepository.findByIsDeletedFalseAndIsActiveTrue(pageable);
        return toPagedResponse(clubs);
    }

    @Override
    public PagedResponse<ClubResponse> searchClubs(String keyword, Pageable pageable) {
        Page<Club> clubs = clubRepository.searchByKeyword(keyword, pageable);
        return toPagedResponse(clubs);
    }

    @Override
    public PagedResponse<ClubResponse> getClubsByFaculty(FacultyType faculty, Pageable pageable) {
        Page<Club> clubs = clubRepository.findByFaculty(faculty, pageable);
        return toPagedResponse(clubs);
    }

    @Override
    public PagedResponse<ClubResponse> getClubsOpenForRegistration(Pageable pageable) {
        Page<Club> clubs = clubRepository.findOpenForRegistration(pageable);
        return toPagedResponse(clubs);
    }

    @Override
    @Transactional
    public ClubResponse updateClub(Long clubId, CreateClubRequest request, MultipartFile logo) {
        log.info("Updating club with logo: {}", clubId);
        Club club = findClubById(clubId);

        // Validate permissions (must be president or admin)
        validateClubAdminAccess(club);

        // Update fields
        if (request.getName() != null) {
            club.setName(request.getName());
        }
        if (request.getDescription() != null) {
            club.setDescription(request.getDescription());
        }
        if (request.getEmail() != null) {
            club.setEmail(request.getEmail());
        }
        if (request.getContactNumber() != null) {
            club.setContactNumber(request.getContactNumber());
        }
        if (request.getSocialMediaLinks() != null) {
            club.setSocialMediaLinks(request.getSocialMediaLinks());
        }
        if (request.getMaxMembers() != null) {
            club.setMaxMembers(request.getMaxMembers());
        }
        if (request.getIsRegistrationOpen() != null) {
            club.setIsRegistrationOpen(request.getIsRegistrationOpen());
        }
        if (request.getFaculty() != null) {
            club.setFaculty(request.getFaculty());
        }

        // Handle logo upload if provided
        if (logo != null && !logo.isEmpty()) {
            validateLogoFile(logo);

            // Delete old logo if exists
            if (club.getLogoUrl() != null && !club.getLogoUrl().isEmpty()) {
                try {
                    String oldKey = extractS3KeyFromUrl(club.getLogoUrl());
                    if (oldKey != null) {
                        s3Service.deleteFile(oldKey);
                        log.info("Deleted old logo: {}", oldKey);
                    }
                } catch (Exception e) {
                    log.warn("Failed to delete old logo: {}", e.getMessage());
                }
            }

            // Upload new logo
            String logoUrl = s3Service.uploadFilePublic(logo, CLUB_LOGO_FOLDER);
            club.setLogoUrl(logoUrl);
            log.info("New club logo uploaded to S3: {}", logoUrl);
        }

        club = clubRepository.save(club);
        log.info("Club updated successfully with logo: {}", clubId);

        return enrichClubResponse(clubMapper.toResponse(club), clubId, club);
    }

    // ==================== Membership Management ====================

    @Override
    @Transactional
    public ClubMembershipResponse joinClub(JoinClubRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} joining club {}", currentUserId, request.getClubId());

        Club club = findClubById(request.getClubId());

        // Validate club accepts members
        if (!club.canAcceptMembers()) {
            throw new BadRequestException("Club is not accepting new members");
        }

        // Check for ANY existing membership (including soft-deleted records) to avoid unique constraint violation
        Optional<ClubMembership> existingMembership = membershipRepository.findByClubIdAndMemberId(request.getClubId(), currentUserId);

        if (existingMembership.isPresent()) {
            ClubMembership membership = existingMembership.get();

            // If the record was soft-deleted, reuse it for re-application
            if (membership.getIsDeleted()) {
                log.info("User {} has a soft-deleted membership, allowing re-application", currentUserId);

                // Check max members before re-application
                long currentMembers = membershipRepository.countActiveMembers(request.getClubId(), LocalDate.now());
                if (currentMembers >= club.getMaxMembers()) {
                    throw new BadRequestException("Club has reached maximum member capacity");
                }

                // Reuse the existing record (unique constraint on club_id, member_id)
                membership.setIsDeleted(false);
                membership.setIsActive(true);
                membership.setDeletedAt(null);
                membership.setDeletedBy(null);
                membership.setStatus(ClubMembershipStatus.PENDING);
                membership.setJoinDate(LocalDate.now());
                membership.setRemarks(request.getRemarks());
                membership.setPosition(null);
                membership.setApprovedAt(null);
                membership.setApprovedBy(null);
                membership.setExpiryDate(null);

                membership = membershipRepository.save(membership);
                log.info("Membership re-application submitted (reactivated): {}", membership.getMembershipNumber());

                return clubMapper.toResponse(membership);
            }

            // Record is not deleted — check its status
            ClubMembershipStatus status = membership.getStatus();

            if (status == ClubMembershipStatus.PENDING) {
                throw new BadRequestException("You already have a pending membership application for this club");
            }
            if (status == ClubMembershipStatus.ACTIVE) {
                throw new BadRequestException("You are already an active member of this club");
            }
            if (status == ClubMembershipStatus.SUSPENDED) {
                throw new BadRequestException("Your membership is suspended. Please contact the club administrator");
            }
            // If status is REVOKED, EXPIRED, or REJECTED, allow re-application by reusing the existing record
            if (status == ClubMembershipStatus.REVOKED || status == ClubMembershipStatus.EXPIRED || status == ClubMembershipStatus.REJECTED) {
                log.info("User {} has a {} membership, allowing re-application", currentUserId, status);

                // Check max members before re-application
                long currentMembers = membershipRepository.countActiveMembers(request.getClubId(), LocalDate.now());
                if (currentMembers >= club.getMaxMembers()) {
                    throw new BadRequestException("Club has reached maximum member capacity");
                }

                // Reuse the existing record (unique constraint on club_id, member_id)
                membership.setStatus(ClubMembershipStatus.PENDING);
                membership.setJoinDate(LocalDate.now());
                membership.setRemarks(request.getRemarks());
                membership.setPosition(null);
                membership.setApprovedAt(null);
                membership.setApprovedBy(null);
                membership.setExpiryDate(null);

                membership = membershipRepository.save(membership);
                log.info("Membership re-application submitted: {}", membership.getMembershipNumber());

                return clubMapper.toResponse(membership);
            }
        }

        // No existing membership at all — create a new one
        long currentMembers = membershipRepository.countActiveMembers(request.getClubId(), LocalDate.now());
        if (currentMembers >= club.getMaxMembers()) {
            throw new BadRequestException("Club has reached maximum member capacity");
        }

        Student member = findStudentById(currentUserId);

        ClubMembership membership = ClubMembership.builder()
                .club(club)
                .member(member)
                .membershipNumber(ClubMembership.generateMembershipNumber(club.getClubCode(), currentUserId))
                .status(ClubMembershipStatus.PENDING)
                .joinDate(LocalDate.now())
                .remarks(request.getRemarks())
                .build();

        membership = membershipRepository.save(membership);
        log.info("Membership application created: {}", membership.getMembershipNumber());

        return clubMapper.toResponse(membership);
    }

    @Override
    @Transactional
    public ClubMembershipResponse approveMembership(Long membershipId) {
        log.info("Approving membership: {}", membershipId);

        ClubMembership membership = findMembershipById(membershipId);
        Club club = membership.getClub();

        // Validate permissions - must be club officer, admin, or super admin
        validateMembershipApprovalAccess(club);

        if (membership.getStatus() != ClubMembershipStatus.PENDING) {
            throw new BadRequestException("Membership is not pending approval");
        }

        // Get approver - can be student (club officer) or admin/staff
        Long currentUserId = securityService.getCurrentUserId();
        Student approver = null;

        // Try to find as student (for club officers)
        try {
            approver = findStudentById(currentUserId);
        } catch (ResourceNotFoundException e) {
            // Not a student - could be admin or staff, which is fine
            log.info("Approver {} is not a student, proceeding with admin/staff approval", currentUserId);
        }

        membership.approve(approver);

        // Set default position to GENERAL_MEMBER if not already set
        if (membership.getPosition() == null) {
            membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
        }

        membership = membershipRepository.save(membership);

        // Add CLUB_MEMBER role type to the student if they don't have it
        Student member = membership.getMember();
        if (!member.hasRoleType(StudentRoleType.CLUB_MEMBER)) {
            member.addRoleType(StudentRoleType.CLUB_MEMBER);
            // Also set the student's club position if not set
            if (member.getClubPosition() == null) {
                member.setClubPosition(ClubPositionsType.GENERAL_MEMBER);
            }
            studentRepository.save(member);
            log.info("Added CLUB_MEMBER role type to student {} with position GENERAL_MEMBER", member.getId());
        }

        // Send push notification to the approved member
        try {
            String clubName = membership.getClub().getName();
            pushNotificationService.sendToUser(
                    member.getId(),
                    "Membership Approved - " + clubName,
                    "Congratulations! Your membership application for " + clubName + " has been approved. Welcome to the club!",
                    java.util.Map.of(
                            "type", "CLUB_MEMBERSHIP_APPROVED",
                            "clubId", String.valueOf(membership.getClub().getId()),
                            "membershipId", String.valueOf(membershipId)
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send approval push notification for membership {}: {}", membershipId, e.getMessage());
        }

        log.info("Membership approved: {}", membershipId);
        return clubMapper.toResponse(membership);
    }

    /**
     * Validates that the current user can approve/reject memberships.
     * Only club officers (PRESIDENT, VICE_PRESIDENT, SECRETARY, TREASURER, COMMITTEE_MEMBER),
     * admins, super admins, or non-academic staff can approve memberships.
     */
    private void validateMembershipApprovalAccess(Club club) {
        Long currentUserId = securityService.getCurrentUserId();

        // Check if current user is admin or super admin or non-academic staff
        if (securityService.isAdmin() || securityService.isSuperAdmin() || securityService.isNonAcademicstaff()) {
            return; // Admins and staff can approve
        }

        // Check if current user is club president
        if (club.getPresident() != null && club.getPresident().getId().equals(currentUserId)) {
            return; // Club president can approve
        }

        // Check if current user is an active club member with officer position
        Optional<ClubMembership> currentUserMembership = membershipRepository
                .findByClubIdAndMemberIdAndIsDeletedFalse(club.getId(), currentUserId);

        if (currentUserMembership.isPresent()) {
            ClubMembership userMembership = currentUserMembership.get();

            // Must be active member
            if (userMembership.getStatus() != ClubMembershipStatus.ACTIVE) {
                throw new UnauthorizedException("You must be an active member to approve memberships");
            }

            // Must have officer position (not GENERAL_MEMBER)
            ClubPositionsType position = userMembership.getPosition();
            if (position != null && isClubOfficer(position)) {
                return; // Club officer can approve
            }
        }

        throw new UnauthorizedException("Only club officers (President, Vice President, Secretary, Treasurer, Committee Member), admins, or staff can approve memberships");
    }

    /**
     * Check if the position is a club officer (can manage memberships)
     */
    private boolean isClubOfficer(ClubPositionsType position) {
        return position == ClubPositionsType.PRESIDENT ||
               position == ClubPositionsType.VICE_PRESIDENT ||
               position == ClubPositionsType.SECRETARY ||
               position == ClubPositionsType.TREASURER ||
               position == ClubPositionsType.COMMITTEE_MEMBER;
    }

    @Override
    @Transactional
    public void rejectMembership(Long membershipId, String reason) {
        log.info("Rejecting membership: {}", membershipId);

        ClubMembership membership = findMembershipById(membershipId);
        validateMembershipApprovalAccess(membership.getClub());

        if (membership.getStatus() != ClubMembershipStatus.PENDING) {
            throw new BadRequestException("Membership is not pending approval");
        }

        membership.setStatus(ClubMembershipStatus.REJECTED);
        membership.setRemarks(reason);
        membershipRepository.save(membership);

        // Send push notification to the rejected member
        try {
            String clubName = membership.getClub().getName();
            pushNotificationService.sendToUser(
                    membership.getMember().getId(),
                    "Membership Rejected - " + clubName,
                    "Your membership application for " + clubName + " has been rejected. Reason: " + reason,
                    java.util.Map.of(
                            "type", "CLUB_MEMBERSHIP_REJECTED",
                            "clubId", String.valueOf(membership.getClub().getId()),
                            "membershipId", String.valueOf(membershipId),
                            "reason", reason != null ? reason : ""
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send rejection push notification for membership {}: {}", membershipId, e.getMessage());
        }

        log.info("Membership rejected: {}", membershipId);
    }

    @Override
    public PagedResponse<ClubMembershipResponse> getClubMembers(Long clubId, Pageable pageable) {
        Page<ClubMembership> memberships = membershipRepository.findByClubIdAndIsDeletedFalse(clubId, pageable);
        return toMembershipPagedResponse(memberships);
    }

    @Override
    public PagedResponse<ClubMembershipResponse> getActiveClubMembers(Long clubId, Pageable pageable) {
        Page<ClubMembership> memberships = membershipRepository.findActiveMembers(clubId, LocalDate.now(), pageable);
        return toMembershipPagedResponse(memberships);
    }

    @Override
    public PagedResponse<ClubMembershipResponse> getPendingApplications(Long clubId, Pageable pageable) {
        validateClubAdminAccess(findClubById(clubId));
        Page<ClubMembership> memberships = membershipRepository.findPendingApplications(clubId, pageable);
        return toMembershipPagedResponse(memberships);
    }

    @Override
    public PagedResponse<ClubMembershipResponse> getMyMemberships(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Page<ClubMembership> memberships = membershipRepository.findByMemberIdAndIsDeletedFalse(currentUserId, pageable);
        return toMembershipPagedResponse(memberships);
    }

    @Override
    public ClubMembershipResponse getMembershipById(Long membershipId) {
        ClubMembership membership = findMembershipById(membershipId);
        return clubMapper.toResponse(membership);
    }

    @Override
    @Transactional
    public void leaveClub(Long clubId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} leaving club {}", currentUserId, clubId);

        ClubMembership membership = membershipRepository.findByClubIdAndMemberIdAndIsDeletedFalse(clubId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ClubMembership", "clubId and memberId", clubId + ", " + currentUserId));

        membership.softDelete();
        membershipRepository.save(membership);

        log.info("User {} left club {}", currentUserId, clubId);
    }

    @Override
    @Transactional
    public void suspendMembership(Long membershipId, String reason) {
        log.info("Suspending membership: {}", membershipId);

        ClubMembership membership = findMembershipById(membershipId);
        validateMembershipApprovalAccess(membership.getClub());

        membership.suspend(reason);
        membershipRepository.save(membership);

        log.info("Membership suspended: {}", membershipId);
    }

    @Override
    public boolean isActiveMember(Long clubId, Long memberId) {
        return membershipRepository.isActiveMember(clubId, memberId, LocalDate.now());
    }

    @Override
    public boolean canVoteInClub(Long clubId, Long memberId) {
        return membershipRepository.findByClubIdAndMemberIdAndIsDeletedFalse(clubId, memberId)
                .map(ClubMembership::canVote)
                .orElse(false);
    }

    @Override
    public boolean canNominateInClub(Long clubId, Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate eligibilityDate = today.minusMonths(3);
        return membershipRepository.canNominateInClub(clubId, memberId, today, eligibilityDate);
    }

    // ==================== New Extended Features ====================

    @Override
    @Transactional
    public ClubResponse toggleRegistration(Long clubId) {
        Club club = findClubById(clubId);
        validateClubAdminAccess(club);

        boolean newState = !club.getIsRegistrationOpen();
        club.setIsRegistrationOpen(newState);
        club = clubRepository.save(club);

        Long currentUserId = securityService.getCurrentUserId();
        activityLogService.log(club,
                newState ? ClubActivityLog.ActivityType.CLUB_REGISTRATION_OPENED : ClubActivityLog.ActivityType.CLUB_REGISTRATION_CLOSED,
                "Registration " + (newState ? "opened" : "closed") + " for club " + club.getName(),
                currentUserId, securityService.getCurrentUserEmail());

        log.info("Club {} registration toggled to: {}", clubId, newState);
        return enrichClubResponse(clubMapper.toResponse(club), clubId, club);
    }

    @Override
    public ClubStatisticsResponse getClubStatistics(Long clubId) {
        Club club = findClubById(clubId);
        LocalDate today = LocalDate.now();

        long totalMembers = membershipRepository.countActiveMembers(clubId, today);
        long pendingApps = membershipRepository.findPendingApplications(clubId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalAnnouncements = announcementRepository.countByClubId(clubId);

        // Real election statistics
        long totalElections = electionRepository.countByClubIdAndIsDeletedFalse(clubId);
        long activeElections = electionRepository.countByClubIdAndStatusAndIsDeletedFalse(clubId, ElectionStatus.VOTING_OPEN)
                + electionRepository.countByClubIdAndStatusAndIsDeletedFalse(clubId, ElectionStatus.NOMINATION_OPEN);
        long completedElections = electionRepository.countByClubIdAndStatusAndIsDeletedFalse(clubId, ElectionStatus.RESULTS_PUBLISHED);
        long cancelledElections = electionRepository.countByClubIdAndStatusAndIsDeletedFalse(clubId, ElectionStatus.CANCELLED);

        return ClubStatisticsResponse.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .clubCode(club.getClubCode())
                .totalMembers(totalMembers)
                .activeMembers(totalMembers)
                .pendingApplications(pendingApps)
                .suspendedMembers(0L)
                .expiredMemberships(0L)
                .totalElections(totalElections)
                .activeElections(activeElections)
                .completedElections(completedElections)
                .cancelledElections(cancelledElections)
                .totalCandidatesAllTime(0L)
                .totalVotesCastAllTime(0L)
                .generatedAt(LocalDateTime.now())
                .message("Statistics generated successfully for club: " + club.getName())
                .build();
    }

    @Override
    @Transactional
    public List<ClubMembershipResponse> bulkApproveMemberships(Long clubId, List<Long> membershipIds) {
        log.info("Bulk approving {} memberships for club {}", membershipIds.size(), clubId);
        Club club = findClubById(clubId);
        validateMembershipApprovalAccess(club);

        Long currentUserId = securityService.getCurrentUserId();
        Student approver = null;
        try {
            approver = findStudentById(currentUserId);
        } catch (ResourceNotFoundException e) {
            log.info("Approver {} is not a student, proceeding with admin/staff approval", currentUserId);
        }

        List<ClubMembershipResponse> results = new ArrayList<>();
        for (Long membershipId : membershipIds) {
            try {
                ClubMembership membership = findMembershipById(membershipId);
                if (membership.getStatus() != ClubMembershipStatus.PENDING) {
                    continue;
                }
                if (!membership.getClub().getId().equals(clubId)) {
                    continue;
                }

                membership.approve(approver);
                if (membership.getPosition() == null) {
                    membership.setPosition(ClubPositionsType.GENERAL_MEMBER);
                }
                membership = membershipRepository.save(membership);

                Student member = membership.getMember();
                if (!member.hasRoleType(StudentRoleType.CLUB_MEMBER)) {
                    member.addRoleType(StudentRoleType.CLUB_MEMBER);
                    if (member.getClubPosition() == null) {
                        member.setClubPosition(ClubPositionsType.GENERAL_MEMBER);
                    }
                    studentRepository.save(member);
                }

                results.add(clubMapper.toResponse(membership));
            } catch (Exception e) {
                log.warn("Failed to approve membership {}: {}", membershipId, e.getMessage());
            }
        }

        activityLogService.log(club, ClubActivityLog.ActivityType.BULK_MEMBER_APPROVED,
                "Bulk approved " + results.size() + " memberships",
                currentUserId, securityService.getCurrentUserEmail());

        log.info("Bulk approved {} memberships for club {}", results.size(), clubId);
        return results;
    }

    @Override
    @Transactional
    public ClubMembershipResponse changeMemberPosition(Long membershipId, ClubPositionsType newPosition, String reason) {
        log.info("Changing position of membership {} to {}", membershipId, newPosition);

        ClubMembership membership = findMembershipById(membershipId);
        Club club = membership.getClub();

        // Only president or admin can change positions
        validateClubAdminAccess(club);

        if (membership.getStatus() != ClubMembershipStatus.ACTIVE) {
            throw new BadRequestException("Only active members can have their position changed");
        }

        ClubPositionsType oldPosition = membership.getPosition();
        membership.setPosition(newPosition);
        membership.setRemarks(reason != null ? reason : "Position changed from " + oldPosition + " to " + newPosition);
        membership = membershipRepository.save(membership);

        // Update student's club position
        Student member = membership.getMember();
        member.setClubPosition(newPosition);
        studentRepository.save(member);

        Long currentUserId = securityService.getCurrentUserId();
        activityLogService.log(club, ClubActivityLog.ActivityType.MEMBER_POSITION_CHANGED,
                "Position changed from " + oldPosition + " to " + newPosition + " for " + member.getFullName(),
                currentUserId, securityService.getCurrentUserEmail(),
                member.getId(), member.getFullName(),
                membershipId, "ClubMembership", null);

        log.info("Position changed for membership {}: {} -> {}", membershipId, oldPosition, newPosition);
        return clubMapper.toResponse(membership);
    }

    // ==================== Helper Methods ====================

    private Club findClubById(Long clubId) {
        return clubRepository.findByIdAndIsDeletedFalse(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
    }

    private ClubMembership findMembershipById(Long membershipId) {
        return membershipRepository.findById(membershipId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("ClubMembership", "id", membershipId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private AcademicStaff findAcademicStaffById(Long academicStaffId) {
        return academicStaffRepository.findById(academicStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicStaff", "id", academicStaffId));
    }

    /**
     * Validates that a student is eligible to be a club president.
     * Requirements:
     * 1. Student must have StudentRoleType.CLUB_MEMBER
     * 2. Student must have clubPosition as PRESIDENT
     */
    private void validatePresidentEligibility(Student student) {
        // Check if student is a CLUB_MEMBER
        if (!student.hasRoleType(StudentRoleType.CLUB_MEMBER)) {
            throw new BadRequestException(
                    "Student must be a CLUB_MEMBER to be assigned as president. Current roles: "
                    + student.getStudentRoleDisplayName()
            );
        }

        // Check if student has PRESIDENT position
        ClubPositionsType clubPosition = student.getClubPosition();
        if (clubPosition == null || clubPosition != ClubPositionsType.PRESIDENT) {
            throw new BadRequestException(
                    "Student must have 'PRESIDENT' as club position to be assigned as club president. Current position: "
                    + (clubPosition != null ? clubPosition : "None")
            );
        }
    }

    private void validateClubAdminAccess(Club club) {
        Long currentUserId = securityService.getCurrentUserId();

        // Check if current user is president
        boolean isPresident = club.getPresident() != null && club.getPresident().getId().equals(currentUserId);

        // Check if current user is admin
        boolean isAdmin = securityService.isAdmin();

        // Check if current user is super admin
        boolean isSuperAdmin = securityService.isSuperAdmin();

        // Check if current user is super admin
        boolean isNON_ACADEMIC_STAFF = securityService.isNonAcademicstaff();

        if (!isPresident && !isAdmin && !isSuperAdmin && !isNON_ACADEMIC_STAFF) {
            throw new UnauthorizedException("You don't have permission to manage this club");
        }
    }

    private ClubResponse enrichClubResponse(ClubResponse response, Long clubId, Club club) {
        response.setTotalMembers((int) membershipRepository.countActiveMembers(clubId, LocalDate.now()));
        response.setActiveMembers((int) membershipRepository.countActiveMembers(clubId, LocalDate.now()));

        // Real election statistics from Election module
        response.setTotalElections((int) electionRepository.countByClubIdAndIsDeletedFalse(clubId));
        long activeElections = electionRepository.countByClubIdAndStatusAndIsDeletedFalse(
                clubId, ElectionStatus.VOTING_OPEN)
                + electionRepository.countByClubIdAndStatusAndIsDeletedFalse(
                clubId, ElectionStatus.NOMINATION_OPEN);
        response.setActiveElections((int) activeElections);

        // Fetch club officers (President, Vice President, Secretary, Treasurer)
        if (club.getPresident() != null) {
            Student pres = club.getPresident();
            response.setPresident(ClubResponse.ClubOfficerResponse.builder()
                    .id(pres.getId())
                    .name(pres.getFullName())
                    .email(pres.getEmail())
                    .profilePictureUrl(pres.getProfilePictureUrl())
                    .build());
        }

        // Advisor details
        if (club.getAdvisor() != null) {
            AcademicStaff adv = club.getAdvisor();
            response.setAdvisor(ClubResponse.ClubOfficerResponse.builder()
                    .id(adv.getId())
                    .name(adv.getFullName())
                    .email(adv.getEmail())
                    .profilePictureUrl(adv.getProfilePictureUrl())
                    .build());
        }

        membershipRepository.findByClubIdAndPositionAndStatusActive(clubId, ClubPositionsType.VICE_PRESIDENT)
                .ifPresent(m -> response.setVicePresident(toOfficerResponse(m)));

        membershipRepository.findByClubIdAndPositionAndStatusActive(clubId, ClubPositionsType.SECRETARY)
                .ifPresent(m -> response.setSecretary(toOfficerResponse(m)));

        membershipRepository.findByClubIdAndPositionAndStatusActive(clubId, ClubPositionsType.TREASURER)
                .ifPresent(m -> response.setTreasurer(toOfficerResponse(m)));

        return response;
    }

    /**
     * Convert a ClubMembership to a ClubOfficerResponse
     */
    private ClubResponse.ClubOfficerResponse toOfficerResponse(ClubMembership membership) {
        Student member = membership.getMember();
        return ClubResponse.ClubOfficerResponse.builder()
                .id(member.getId())
                .name(member.getFullName())
                .email(member.getEmail())
                .profilePictureUrl(member.getProfilePictureUrl())
                .build();
    }

    private PagedResponse<ClubResponse> toPagedResponse(Page<Club> page) {
        return PagedResponse.<ClubResponse>builder()
                .content(page.getContent().stream()
                        .map(club -> enrichClubResponse(clubMapper.toResponse(club), club.getId(), club))
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

    private PagedResponse<ClubMembershipResponse> toMembershipPagedResponse(Page<ClubMembership> page) {
        return PagedResponse.<ClubMembershipResponse>builder()
                .content(clubMapper.toMembershipResponseList(page.getContent()))
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
