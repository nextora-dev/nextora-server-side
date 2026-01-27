package lk.iit.nextora.module.club.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.request.JoinClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Club operations
 */
public interface ClubService {

    // ==================== Club Management ====================

    /**
     * Create a new club with logo upload (Admin only)
     */
    ClubResponse createClub(CreateClubRequest request, MultipartFile logo);

    /**
     * Delete club (soft delete club and delete logo from S3 - Admin only)
     */
    void deleteClub(Long clubId);

    /**
     * Get club by ID
     */
    ClubResponse getClubById(Long clubId);

    /**
     * Get club by code
     */
    ClubResponse getClubByCode(String clubCode);

    /**
     * Get all active clubs
     */
    PagedResponse<ClubResponse> getAllClubs(Pageable pageable);

    /**
     * Search clubs by keyword
     */
    PagedResponse<ClubResponse> searchClubs(String keyword, Pageable pageable);

    /**
     * Get clubs by faculty
     */
    PagedResponse<ClubResponse> getClubsByFaculty(FacultyType faculty, Pageable pageable);

    /**
     * Get clubs open for registration
     */
    PagedResponse<ClubResponse> getClubsOpenForRegistration(Pageable pageable);

    /**
     * Update club with logo (Club Admin only)
     */
    ClubResponse updateClub(Long clubId, CreateClubRequest request, MultipartFile logo);

    // ==================== Membership Management ====================

    /**
     * Join a club (Student)
     */
    ClubMembershipResponse joinClub(JoinClubRequest request);

    /**
     * Approve membership application (Club Admin)
     */
    ClubMembershipResponse approveMembership(Long membershipId);

    /**
     * Reject membership application (Club Admin)
     */
    void rejectMembership(Long membershipId, String reason);

    /**
     * Get club members
     */
    PagedResponse<ClubMembershipResponse> getClubMembers(Long clubId, Pageable pageable);

    /**
     * Get active club members
     */
    PagedResponse<ClubMembershipResponse> getActiveClubMembers(Long clubId, Pageable pageable);

    /**
     * Get pending membership applications
     */
    PagedResponse<ClubMembershipResponse> getPendingApplications(Long clubId, Pageable pageable);

    /**
     * Get my memberships
     */
    PagedResponse<ClubMembershipResponse> getMyMemberships(Pageable pageable);

    /**
     * Get membership by ID
     */
    ClubMembershipResponse getMembershipById(Long membershipId);

    /**
     * Leave club
     */
    void leaveClub(Long clubId);

    /**
     * Suspend membership (Club Admin)
     */
    void suspendMembership(Long membershipId, String reason);

    /**
     * Check if user is active member of club
     */
    boolean isActiveMember(Long clubId, Long memberId);

    /**
     * Check if user can vote in club elections
     */
    boolean canVoteInClub(Long clubId, Long memberId);

    /**
     * Check if user can nominate in club elections
     */
    default boolean canNominateInClub(Long clubId, Long memberId) {
        return false;
    }
}
