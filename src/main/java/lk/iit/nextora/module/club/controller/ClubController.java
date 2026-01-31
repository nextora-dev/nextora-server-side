package lk.iit.nextora.module.club.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.request.JoinClubRequest;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for Club management operations
 */
@RestController
@RequestMapping(ApiConstants.CLUBS)
@RequiredArgsConstructor
@Tag(name = "Club Management", description = "Club and membership management endpoints")
public class ClubController {

    private final ClubService clubService;

    // ==================== Club Endpoints ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create club", description = "Create a new club with optional logo upload (Admin only)")
    @PreAuthorize("hasAuthority('CLUB:CREATE')")
    public ApiResponse<ClubResponse> createClubWithLogo(
            @RequestParam("name") String name,
            @RequestParam("clubCode") String clubCode,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contactNumber", required = false) String contactNumber,
            @RequestParam(value = "faculty", required = false) String faculty,
            @RequestParam(value = "establishedDate", required = false) String establishedDate,
            @RequestParam(value = "maxMembers", defaultValue = "100") Integer maxMembers,
            @RequestParam(value = "socialMediaLinks", required = false) String socialMediaLinks,
            @RequestParam(value = "presidentId", required = false) Long presidentId,
            @RequestParam(value = "advisorId", required = false) Long advisorId,
            @RequestParam(value = "isRegistrationOpen", defaultValue = "true") Boolean isRegistrationOpen,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        CreateClubRequest request = CreateClubRequest.builder()
                .name(name)
                .clubCode(clubCode)
                .description(description)
                .email(email)
                .contactNumber(contactNumber)
                .faculty(faculty != null ? FacultyType.valueOf(faculty) : null)
                .establishedDate(establishedDate != null ? java.time.LocalDate.parse(establishedDate) : null)
                .maxMembers(maxMembers)
                .socialMediaLinks(socialMediaLinks)
                .presidentId(presidentId)
                .advisorId(advisorId)
                .isRegistrationOpen(isRegistrationOpen)
                .build();

        ClubResponse response = clubService.createClub(request, logo);
        return ApiResponse.success("Club created successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_BY_ID)
    @Operation(summary = "Get club by ID", description = "Get club details by ID")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<ClubResponse> getClubById(@PathVariable Long clubId) {
        ClubResponse response = clubService.getClubById(clubId);
        return ApiResponse.success("Club retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_BY_CODE)
    @Operation(summary = "Get club by code", description = "Get club details by club code")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<ClubResponse> getClubByCode(@PathVariable String clubCode) {
        ClubResponse response = clubService.getClubByCode(clubCode);
        return ApiResponse.success("Club retrieved successfully", response);
    }

    @GetMapping
    @Operation(summary = "Get all clubs", description = "Get all active clubs with pagination")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<PagedResponse<ClubResponse>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<ClubResponse> response = clubService.getAllClubs(pageable);
        return ApiResponse.success("Clubs retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_SEARCH)
    @Operation(summary = "Search clubs", description = "Search clubs by keyword")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<PagedResponse<ClubResponse>> searchClubs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubResponse> response = clubService.searchClubs(keyword, pageable);
        return ApiResponse.success("Search completed successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_FACULTY)
    @Operation(summary = "Get clubs by faculty", description = "Get clubs filtered by faculty")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<PagedResponse<ClubResponse>> getClubsByFaculty(
            @PathVariable FacultyType faculty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubResponse> response = clubService.getClubsByFaculty(faculty, pageable);
        return ApiResponse.success("Clubs retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_OPEN_REGISTRATION)
    @Operation(summary = "Get clubs open for registration", description = "Get clubs accepting new members")
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<PagedResponse<ClubResponse>> getClubsOpenForRegistration(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubResponse> response = clubService.getClubsOpenForRegistration(pageable);
        return ApiResponse.success("Clubs retrieved successfully", response);
    }

    @PutMapping(value = ApiConstants.CLUB_BY_ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update club", description = "Update club details with new logo upload (Club Admin only)")
    @PreAuthorize("hasAuthority('CLUB:UPDATE')")
    public ApiResponse<ClubResponse> updateClub(
            @PathVariable Long clubId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "contactNumber", required = false) String contactNumber,
            @RequestParam(value = "faculty", required = false) String faculty,
            @RequestParam(value = "maxMembers", required = false) Integer maxMembers,
            @RequestParam(value = "socialMediaLinks", required = false) String socialMediaLinks,
            @RequestParam(value = "isRegistrationOpen", required = false) Boolean isRegistrationOpen,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {

        CreateClubRequest request = CreateClubRequest.builder()
                .name(name)
                .description(description)
                .email(email)
                .contactNumber(contactNumber)
                .faculty(faculty != null ? FacultyType.valueOf(faculty) : null)
                .maxMembers(maxMembers)
                .socialMediaLinks(socialMediaLinks)
                .isRegistrationOpen(isRegistrationOpen)
                .build();

        ClubResponse response = clubService.updateClub(clubId, request, logo);
        return ApiResponse.success("Club updated successfully", response);
    }

    @DeleteMapping(ApiConstants.CLUB_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete club", description = "Delete a club (Admin only)")
    @PreAuthorize("hasAuthority('CLUB:DELETE')")
    public ApiResponse<Void> deleteClub(@PathVariable Long clubId) {
        clubService.deleteClub(clubId);
        return ApiResponse.success("Club deleted successfully", null);
    }

    // ==================== Membership Endpoints ====================

    @PostMapping(ApiConstants.CLUB_JOIN)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Join club", description = "Apply to join a club")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<ClubMembershipResponse> joinClub(@Valid @RequestBody JoinClubRequest request) {
        ClubMembershipResponse response = clubService.joinClub(request);
        return ApiResponse.success("Membership application submitted successfully", response);
    }

    @PostMapping(ApiConstants.MEMBERSHIP_APPROVE)
    @Operation(summary = "Approve membership", description = "Approve a membership application (Club Admin)")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<ClubMembershipResponse> approveMembership(@PathVariable Long membershipId) {
        ClubMembershipResponse response = clubService.approveMembership(membershipId);
        return ApiResponse.success("Membership approved successfully", response);
    }

    @PostMapping(ApiConstants.MEMBERSHIP_REJECT)
    @Operation(summary = "Reject membership", description = "Reject a membership application (Club Admin)")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<Void> rejectMembership(
            @PathVariable Long membershipId,
            @RequestParam String reason) {
        clubService.rejectMembership(membershipId, reason);
        return ApiResponse.success("Membership rejected successfully", null);
    }

    @GetMapping(ApiConstants.CLUB_MEMBERS)
    @Operation(summary = "Get club members", description = "Get all members of a club")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<PagedResponse<ClubMembershipResponse>> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubMembershipResponse> response = clubService.getClubMembers(clubId, pageable);
        return ApiResponse.success("Members retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_MEMBERS_ACTIVE)
    @Operation(summary = "Get active club members", description = "Get active members of a club")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<PagedResponse<ClubMembershipResponse>> getActiveClubMembers(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubMembershipResponse> response = clubService.getActiveClubMembers(clubId, pageable);
        return ApiResponse.success("Active members retrieved successfully", response);
    }

    @GetMapping(ApiConstants.CLUB_MEMBERS_PENDING)
    @Operation(summary = "Get pending applications", description = "Get pending membership applications (Club Admin)")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<PagedResponse<ClubMembershipResponse>> getPendingApplications(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubMembershipResponse> response = clubService.getPendingApplications(clubId, pageable);
        return ApiResponse.success("Pending applications retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MY_MEMBERSHIPS)
    @Operation(summary = "Get my memberships", description = "Get current user's club memberships")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<PagedResponse<ClubMembershipResponse>> getMyMemberships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ClubMembershipResponse> response = clubService.getMyMemberships(pageable);
        return ApiResponse.success("Memberships retrieved successfully", response);
    }

    @GetMapping(ApiConstants.MEMBERSHIP_BY_ID)
    @Operation(summary = "Get membership by ID", description = "Get membership details")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<ClubMembershipResponse> getMembershipById(@PathVariable Long membershipId) {
        ClubMembershipResponse response = clubService.getMembershipById(membershipId);
        return ApiResponse.success("Membership retrieved successfully", response);
    }

    @DeleteMapping(ApiConstants.CLUB_LEAVE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Leave club", description = "Leave a club")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:VIEW')")
    public ApiResponse<Void> leaveClub(@PathVariable Long clubId) {
        clubService.leaveClub(clubId);
        return ApiResponse.success("Left club successfully", null);
    }

    @PostMapping(ApiConstants.MEMBERSHIP_SUSPEND)
    @Operation(summary = "Suspend membership", description = "Suspend a club membership (Club Admin)")
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<Void> suspendMembership(
            @PathVariable Long membershipId,
            @RequestParam String reason) {
        clubService.suspendMembership(membershipId, reason);
        return ApiResponse.success("Membership suspended successfully", null);
    }
}
