package lk.iit.nextora.module.club.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.request.JoinClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.dto.response.ClubStatisticsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Club operations
 */
public interface ClubService {

    // ==================== Club Management ====================

    ClubResponse createClub(CreateClubRequest request, MultipartFile logo);

    void deleteClub(Long clubId);

    /**
     * Permanently delete a club and all associated data from the database.
     * Removes: memberships, announcements (+ S3 files), activity logs, elections, and the club itself.
     * Super Admin only — this action is irreversible.
     */
    void permanentlyDeleteClub(Long clubId);

    ClubResponse getClubById(Long clubId);

    ClubResponse getClubByCode(String clubCode);

    PagedResponse<ClubResponse> getAllClubs(Pageable pageable);

    PagedResponse<ClubResponse> searchClubs(String keyword, Pageable pageable);

    PagedResponse<ClubResponse> getClubsByFaculty(FacultyType faculty, Pageable pageable);

    PagedResponse<ClubResponse> getClubsOpenForRegistration(Pageable pageable);

    ClubResponse updateClub(Long clubId, CreateClubRequest request, MultipartFile logo);

    /**
     * Toggle club registration open/closed
     */
    ClubResponse toggleRegistration(Long clubId);

    /**
     * Get club statistics (membership, events, announcements)
     */
    ClubStatisticsResponse getClubStatistics(Long clubId);

    // ==================== Membership Management ====================

    ClubMembershipResponse joinClub(JoinClubRequest request);

    ClubMembershipResponse approveMembership(Long membershipId);

    void rejectMembership(Long membershipId, String reason);

    /**
     * Bulk approve pending memberships for a club
     */
    List<ClubMembershipResponse> bulkApproveMemberships(Long clubId, List<Long> membershipIds);

    /**
     * Change a member's position within a club (role-based: president/admin only)
     */
    ClubMembershipResponse changeMemberPosition(Long membershipId, ClubPositionsType newPosition, String reason);

    PagedResponse<ClubMembershipResponse> getClubMembers(Long clubId, Pageable pageable);

    PagedResponse<ClubMembershipResponse> getActiveClubMembers(Long clubId, Pageable pageable);

    PagedResponse<ClubMembershipResponse> getPendingApplications(Long clubId, Pageable pageable);

    PagedResponse<ClubMembershipResponse> getMyMemberships(Pageable pageable);

    ClubMembershipResponse getMembershipById(Long membershipId);

    void leaveClub(Long clubId);

    void suspendMembership(Long membershipId, String reason);

    boolean isActiveMember(Long clubId, Long memberId);

    boolean canVoteInClub(Long clubId, Long memberId);

    default boolean canNominateInClub(Long clubId, Long memberId) {
        return false;
    }
}
