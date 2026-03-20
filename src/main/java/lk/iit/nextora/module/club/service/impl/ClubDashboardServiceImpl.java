package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.club.dto.response.ClubActivityLogResponse;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.dto.response.dashboard.*;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lk.iit.nextora.module.club.entity.ClubMembership;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubActivityLogRepository;
import lk.iit.nextora.module.club.repository.ClubAnnouncementRepository;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubDashboardService;
import lk.iit.nextora.module.election.dto.response.ElectionResponse;
import lk.iit.nextora.module.election.entity.Election;
import lk.iit.nextora.module.election.mapper.VotingMapper;
import lk.iit.nextora.module.election.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for role-based club dashboard aggregation.
 * Each method composes data from multiple repositories to provide
 * a complete dashboard view tailored to the caller's role.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubDashboardServiceImpl implements ClubDashboardService {

    private static final int DASHBOARD_ITEM_LIMIT = 5;
    private static final int ACTIVITY_LIMIT = 10;

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ClubAnnouncementRepository announcementRepository;
    private final ClubActivityLogRepository activityLogRepository;
    private final ElectionRepository electionRepository;
    private final SecurityService securityService;
    private final ClubMapper clubMapper;
    private final VotingMapper votingMapper;

    // ==================== Student Dashboard ====================

    @Override
    public StudentClubDashboardResponse getStudentDashboard() {
        Long currentUserId = securityService.getCurrentUserId();
        log.debug("Generating student club dashboard for user: {}", currentUserId);

        long totalActive = clubRepository.countActiveClubs();
        long openReg = clubRepository.countOpenForRegistration();
        long myMemberships = membershipRepository.countByMemberId(currentUserId);
        long myPending = membershipRepository.countPendingByMemberId(currentUserId);

        // Featured clubs (most recent, open for registration)
        Pageable limit5 = PageRequest.of(0, DASHBOARD_ITEM_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Club> featuredClubs = clubRepository.findRecentClubs(limit5);
        List<ClubResponse> featuredClubResponses = clubMapper.toClubResponseList(featuredClubs);

        // My memberships summary
        List<ClubMembership> activeMemberships = membershipRepository
                .findActiveMembershipsByStudent(currentUserId, LocalDate.now());
        List<StudentClubDashboardResponse.MembershipSummaryItem> membershipSummary =
                activeMemberships.stream()
                        .limit(DASHBOARD_ITEM_LIMIT)
                        .map(cm -> StudentClubDashboardResponse.MembershipSummaryItem.builder()
                                .clubId(cm.getClub().getId())
                                .clubName(cm.getClub().getName())
                                .clubCode(cm.getClub().getClubCode())
                                .position(cm.getPosition() != null ? cm.getPosition().name() : "GENERAL_MEMBER")
                                .status(cm.getStatus().name())
                                .logoUrl(cm.getClub().getLogoUrl())
                                .build())
                        .toList();

        return StudentClubDashboardResponse.builder()
                .totalActiveClubs(totalActive)
                .openForRegistrationCount(openReg)
                .myMembershipsCount(myMemberships)
                .myPendingApplicationsCount(myPending)
                .featuredClubs(featuredClubResponses)
                .latestPublicAnnouncements(Collections.emptyList())
                .myMemberships(membershipSummary)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Club Member Dashboard ====================

    @Override
    public ClubMemberDashboardResponse getClubMemberDashboard(Long clubId) {
        Long currentUserId = securityService.getCurrentUserId();
        log.debug("Generating club member dashboard for user: {} in club: {}", currentUserId, clubId);

        Club club = clubRepository.findByIdAndIsDeletedFalse(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        ClubResponse clubResponse = clubMapper.toResponse(club);

        ClubMembership membership = membershipRepository
                .findByClubIdAndMemberIdAndIsDeletedFalse(clubId, currentUserId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this club"));

        ClubPositionsType myPosition = membership.getPosition();
        boolean isOfficer = isOfficerPosition(myPosition);

        LocalDate today = LocalDate.now();
        long totalMembers = membershipRepository.countActiveMembers(clubId, today);
        long pendingCount = membershipRepository.countPendingByClubId(clubId);

        // Latest announcements
        Pageable limit5 = PageRequest.of(0, DASHBOARD_ITEM_LIMIT);
        Page<ClubAnnouncement> announcementPage = announcementRepository
                .findByClubIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(clubId, limit5);
        List<ClubAnnouncementResponse> latestAnnouncements =
                clubMapper.toAnnouncementResponseList(announcementPage.getContent());

        // Active elections
        List<Election> activeElectionsList = electionRepository
                .findByClubIdAndStatusAndIsDeletedFalse(clubId, ElectionStatus.VOTING_OPEN);
        List<ElectionResponse> activeElections = activeElectionsList.stream()
                .map(votingMapper::toResponse).toList();

        // Upcoming elections
        Pageable electionLimit = PageRequest.of(0, DASHBOARD_ITEM_LIMIT);
        Page<Election> upcomingPage = electionRepository
                .findUpcomingByClub(clubId, LocalDateTime.now(), electionLimit);
        List<ElectionResponse> upcomingElections = upcomingPage.getContent().stream()
                .map(votingMapper::toResponse).toList();

        // Recent activity (officers only)
        List<ClubActivityLogResponse> recentActivity = Collections.emptyList();
        if (isOfficer) {
            Pageable activityLimit = PageRequest.of(0, ACTIVITY_LIMIT);
            Page<ClubActivityLog> activityPage = activityLogRepository
                    .findByClubIdOrderByCreatedAtDesc(clubId, activityLimit);
            recentActivity = clubMapper.toActivityLogResponseList(activityPage.getContent());
        }

        boolean canManageMembers = isOfficer;
        boolean canCreateAnnouncements = membership.getStatus() == ClubMembershipStatus.ACTIVE;
        boolean canUpdateClub = myPosition == ClubPositionsType.PRESIDENT;

        return ClubMemberDashboardResponse.builder()
                .club(clubResponse)
                .myMembershipId(membership.getId())
                .myPosition(myPosition)
                .myStatus(membership.getStatus())
                .totalMembers(totalMembers)
                .activeMembers(totalMembers)
                .pendingApplicationsCount(pendingCount)
                .latestAnnouncements(latestAnnouncements)
                .activeElections(activeElections)
                .upcomingElections(upcomingElections)
                .recentActivity(recentActivity)
                .canManageMembers(canManageMembers)
                .canCreateAnnouncements(canCreateAnnouncements)
                .canManageElections(false)
                .canViewStats(isOfficer)
                .canUpdateClub(canUpdateClub)
                .isOfficer(isOfficer)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Staff Dashboard ====================

    @Override
    public StaffClubDashboardResponse getStaffDashboard() {
        log.debug("Generating staff club dashboard");

        LocalDate today = LocalDate.now();
        long totalClubs = clubRepository.count();
        long activeClubs = clubRepository.countActiveClubs();
        long openReg = clubRepository.countOpenForRegistration();
        long totalMembers = membershipRepository.countAllActiveMembers(today);
        long pendingAll = membershipRepository.countAllPending();
        long activeElections = electionRepository.countActiveElections();
        long totalElections = electionRepository.countAllActive();

        Pageable limit5 = PageRequest.of(0, DASHBOARD_ITEM_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ClubResponse> recentClubs = clubMapper.toClubResponseList(clubRepository.findRecentClubs(limit5));

        List<StaffClubDashboardResponse.PendingClubItem> pendingClubs = buildPendingClubsList();

        Pageable activityLimit = PageRequest.of(0, ACTIVITY_LIMIT);
        List<ClubActivityLog> recentLogs = activityLogRepository.findRecentActivity(activityLimit);
        List<ClubActivityLogResponse> recentActivity = clubMapper.toActivityLogResponseList(recentLogs);

        return StaffClubDashboardResponse.builder()
                .totalClubs(totalClubs)
                .activeClubs(activeClubs)
                .openForRegistrationCount(openReg)
                .totalMembersAcrossClubs(totalMembers)
                .pendingRequestsAcrossClubs(pendingAll)
                .activeElectionsCount(activeElections)
                .totalElections(totalElections)
                .recentlyCreatedClubs(recentClubs)
                .clubsWithPendingApplications(pendingClubs)
                .recentActivity(recentActivity)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Admin Dashboard ====================

    @Override
    public AdminClubDashboardResponse getAdminDashboard() {
        log.debug("Generating admin club dashboard");

        LocalDate today = LocalDate.now();
        long totalClubs = clubRepository.count();
        long activeClubs = clubRepository.countActiveClubs();
        long openReg = clubRepository.countOpenForRegistration();
        long totalMembers = membershipRepository.countAllActiveMembers(today);
        long pendingAll = membershipRepository.countAllPending();
        long totalElections = electionRepository.countAllActive();
        long activeElections = electionRepository.countActiveElections();
        long completedElections = electionRepository.countByStatus(ElectionStatus.RESULTS_PUBLISHED);
        long cancelledElections = electionRepository.countByStatus(ElectionStatus.CANCELLED);

        Pageable limit5 = PageRequest.of(0, DASHBOARD_ITEM_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ClubResponse> recentClubs = clubMapper.toClubResponseList(clubRepository.findRecentClubs(limit5));

        List<StaffClubDashboardResponse.PendingClubItem> pendingClubs = buildPendingClubsList();

        Pageable activityLimit = PageRequest.of(0, ACTIVITY_LIMIT);
        List<ClubActivityLog> recentLogs = activityLogRepository.findRecentActivity(activityLimit);
        List<ClubActivityLogResponse> recentActivity = clubMapper.toActivityLogResponseList(recentLogs);

        return AdminClubDashboardResponse.builder()
                .totalClubs(totalClubs)
                .activeClubs(activeClubs)
                .openForRegistrationCount(openReg)
                .totalMembersAcrossClubs(totalMembers)
                .pendingRequestsAcrossClubs(pendingAll)
                .totalElections(totalElections)
                .activeElectionsCount(activeElections)
                .completedElections(completedElections)
                .cancelledElections(cancelledElections)
                .recentlyCreatedClubs(recentClubs)
                .clubsWithPendingApplications(pendingClubs)
                .recentActivity(recentActivity)
                .clubsCreatedByMonth(null)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Academic Staff Dashboard ====================

    @Override
    public AcademicStaffClubDashboardResponse getAcademicStaffDashboard() {
        Long currentUserId = securityService.getCurrentUserId();
        log.debug("Generating academic staff club dashboard for user: {}", currentUserId);

        long totalActive = clubRepository.countActiveClubs();
        long openReg = clubRepository.countOpenForRegistration();

        Pageable limit10 = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ClubResponse> clubs = clubMapper.toClubResponseList(clubRepository.findRecentClubs(limit10));

        List<Club> advisingClubEntities = clubRepository.findByAdvisorId(currentUserId);
        List<ClubResponse> advisingClubs = clubMapper.toClubResponseList(advisingClubEntities);

        return AcademicStaffClubDashboardResponse.builder()
                .totalActiveClubs(totalActive)
                .openForRegistrationCount(openReg)
                .clubs(clubs)
                .advisingClubs(advisingClubs)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Helper Methods ====================

    private boolean isOfficerPosition(ClubPositionsType position) {
        if (position == null) return false;
        return Set.of(
                ClubPositionsType.PRESIDENT,
                ClubPositionsType.VICE_PRESIDENT,
                ClubPositionsType.SECRETARY,
                ClubPositionsType.TREASURER,
                ClubPositionsType.COMMITTEE_MEMBER
        ).contains(position);
    }

    private List<StaffClubDashboardResponse.PendingClubItem> buildPendingClubsList() {
        List<StaffClubDashboardResponse.PendingClubItem> pendingClubs = new ArrayList<>();
        Pageable allClubs = PageRequest.of(0, 100);
        Page<Club> clubPage = clubRepository.findByIsDeletedFalseAndIsActiveTrue(allClubs);

        for (Club club : clubPage.getContent()) {
            long pending = membershipRepository.countPendingByClubId(club.getId());
            if (pending > 0) {
                pendingClubs.add(StaffClubDashboardResponse.PendingClubItem.builder()
                        .clubId(club.getId())
                        .clubName(club.getName())
                        .clubCode(club.getClubCode())
                        .pendingCount(pending)
                        .build());
            }
        }

        pendingClubs.sort((a, b) -> Long.compare(b.getPendingCount(), a.getPendingCount()));
        return pendingClubs.stream().limit(DASHBOARD_ITEM_LIMIT).collect(Collectors.toList());
    }
}

