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
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubMembership;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    private final StudentRepository studentRepository;
    private final AcademicStaffRepository academicStaffRepository;
    private final SecurityService securityService;
    private final ClubMapper clubMapper;
    private final lk.iit.nextora.config.S3.S3Service s3Service;

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

        return enrichClubResponse(clubMapper.toResponse(club), club.getId());
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
        Club club = findClubById(clubId);
        return enrichClubResponse(clubMapper.toResponse(club), clubId);
    }

    @Override
    public ClubResponse getClubByCode(String clubCode) {
        Club club = clubRepository.findByClubCodeAndIsDeletedFalse(clubCode)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "clubCode", clubCode));
        return enrichClubResponse(clubMapper.toResponse(club), club.getId());
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

        return enrichClubResponse(clubMapper.toResponse(club), clubId);
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

        // Check if already a member (PENDING or ACTIVE status)
        Optional<ClubMembership> existingMembership = membershipRepository.findByClubIdAndMemberIdAndIsDeletedFalse(request.getClubId(), currentUserId);

        if (existingMembership.isPresent()) {
            ClubMembership membership = existingMembership.get();
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
            // If status is REVOKED or EXPIRED, allow re-application by soft-deleting old record
            if (status == ClubMembershipStatus.REVOKED || status == ClubMembershipStatus.EXPIRED) {
                log.info("User {} has a {} membership, allowing re-application", currentUserId, status);
                membership.softDelete();
                membershipRepository.save(membership);
            }
        }

        // Check max members
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

        membership.setStatus(ClubMembershipStatus.REVOKED);
        membership.setRemarks(reason);
        membershipRepository.save(membership);

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
        LocalDate eligibilityDate = today.minusMonths(3); // Member must have joined at least 3 months ago
        return membershipRepository.canNominateInClub(clubId, memberId, today, eligibilityDate);
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

    private ClubResponse enrichClubResponse(ClubResponse response, Long clubId) {
        response.setTotalMembers((int) membershipRepository.countActiveMembers(clubId, LocalDate.now()));
        response.setActiveMembers((int) membershipRepository.countActiveMembers(clubId, LocalDate.now()));
        // Elections are managed in voting module, so we don't count them here
        response.setTotalElections(0);
        return response;
    }

    private PagedResponse<ClubResponse> toPagedResponse(Page<Club> page) {
        return PagedResponse.<ClubResponse>builder()
                .content(page.getContent().stream()
                        .map(club -> enrichClubResponse(clubMapper.toResponse(club), club.getId()))
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
