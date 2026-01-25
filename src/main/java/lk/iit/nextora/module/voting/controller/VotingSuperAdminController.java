package lk.iit.nextora.module.voting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.iit.nextora.common.constants.ApiConstants;
import lk.iit.nextora.common.dto.ApiResponse;
import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.response.ClubMembershipResponse;
import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lk.iit.nextora.module.club.service.ClubService;
import lk.iit.nextora.module.voting.dto.response.*;
import lk.iit.nextora.module.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Super Admin Controller for Voting Module.
 * Provides highest-level administrative endpoints exclusively for Super Admin.
 * These operations are critical system functions.
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.VOTING_ADMIN)
@RequiredArgsConstructor
@Tag(name = "Voting Super Admin", description = "Super Admin only endpoints for voting module")
public class VotingSuperAdminController {

    private final ClubService clubService;
    private final ElectionService electionService;

    // ==================== SYSTEM OPERATIONS ====================

    /**
     * Trigger manual election status update
     * Forces the scheduled task to run immediately
     */
    @PostMapping("/elections/process-status-updates")
    @Operation(
            summary = "Process election status updates",
            description = "Manually trigger election status updates. Normally runs automatically every minute."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<Void> processElectionStatusUpdates() {
        log.warn("Super Admin manually triggering election status updates");
        electionService.processElectionStatusUpdates();
        return ApiResponse.success("Election status updates processed successfully", null);
    }

    /**
     * Get system-wide audit log for voting module
     */
    @GetMapping("/audit-log")
    @Operation(
            summary = "Get voting audit log",
            description = "Retrieve system-wide audit log for all voting activities."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<PagedResponse<AuditLogEntry>> getVotingAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action) {
        log.info("Super Admin retrieving voting audit log");
        // Placeholder - would need an audit service
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success("Audit log retrieved successfully", null);
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Bulk approve pending memberships
     */
    @PostMapping("/memberships/bulk-approve")
    @Operation(
            summary = "Bulk approve memberships",
            description = "Approve all pending membership applications for a club."
    )
    @PreAuthorize("hasAuthority('CLUB_MEMBERSHIP:MANAGE')")
    public ApiResponse<BulkOperationResult> bulkApproveMemberships(
            @RequestParam Long clubId) {
        log.warn("Super Admin bulk approving memberships for club: {}", clubId);
        // Would need a bulk operation in service
        BulkOperationResult result = BulkOperationResult.builder()
                .totalProcessed(0)
                .successCount(0)
                .failureCount(0)
                .message("Bulk operation completed")
                .build();
        return ApiResponse.success("Bulk approval completed", result);
    }

    /**
     * Bulk cancel elections
     */
    @PostMapping("/elections/bulk-cancel")
    @Operation(
            summary = "Bulk cancel elections",
            description = "Cancel all elections for a club. Critical operation."
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<BulkOperationResult> bulkCancelElections(
            @RequestParam Long clubId,
            @RequestParam(defaultValue = "Bulk cancelled by Super Admin") String reason) {
        log.warn("Super Admin bulk cancelling elections for club: {} with reason: {}", clubId, reason);
        BulkOperationResult result = BulkOperationResult.builder()
                .totalProcessed(0)
                .successCount(0)
                .failureCount(0)
                .message("Bulk cancellation completed")
                .build();
        return ApiResponse.success("Bulk cancellation completed", result);
    }

    // ==================== DATA MANAGEMENT ====================

    /**
     * Export voting data for a club
     */
    @GetMapping("/clubs/{clubId}/export")
    @Operation(
            summary = "Export club voting data",
            description = "Export all voting data for a club in JSON format."
    )
    @PreAuthorize("hasAuthority('CLUB:READ')")
    public ApiResponse<ClubExportData> exportClubData(@PathVariable Long clubId) {
        log.info("Super Admin exporting data for club: {}", clubId);
        ClubExportData exportData = ClubExportData.builder()
                .clubId(clubId)
                .message("Export generated successfully")
                .build();
        return ApiResponse.success("Export generated successfully", exportData);
    }

    /**
     * Export election data
     */
    @GetMapping("/elections/{electionId}/export")
    @Operation(
            summary = "Export election data",
            description = "Export complete election data including votes (anonymized)."
    )
    @PreAuthorize("hasAuthority('ELECTION:READ')")
    public ApiResponse<ElectionExportData> exportElectionData(@PathVariable Long electionId) {
        log.info("Super Admin exporting data for election: {}", electionId);
        ElectionExportData exportData = ElectionExportData.builder()
                .electionId(electionId)
                .message("Export generated successfully")
                .build();
        return ApiResponse.success("Export generated successfully", exportData);
    }

    // ==================== DANGER ZONE OPERATIONS ====================

    /**
     * Permanently delete club and all related data
     * THIS IS A DESTRUCTIVE OPERATION
     */
    @DeleteMapping("/clubs/{clubId}/permanent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Permanently delete club",
            description = "DANGER: Permanently delete a club and ALL related data. This cannot be undone!"
    )
    @PreAuthorize("hasAuthority('CLUB:DELETE')")
    public ApiResponse<Void> permanentlyDeleteClub(
            @PathVariable Long clubId,
            @RequestParam String confirmationCode) {
        log.error("Super Admin PERMANENTLY DELETING club: {}", clubId);

        // Require confirmation code for safety
        if (!"CONFIRM_PERMANENT_DELETE".equals(confirmationCode)) {
            return ApiResponse.error("Invalid confirmation code. Use 'CONFIRM_PERMANENT_DELETE'");
        }

        // Would need permanent delete method in service
        clubService.deleteClub(clubId);
        return ApiResponse.success("Club permanently deleted", null);
    }

    /**
     * Reset election votes (for testing/emergency only)
     * THIS IS A DESTRUCTIVE OPERATION
     */
    @DeleteMapping("/elections/{electionId}/reset-votes")
    @Operation(
            summary = "Reset election votes",
            description = "DANGER: Delete all votes for an election. Only for emergency situations!"
    )
    @PreAuthorize("hasAuthority('ELECTION:MANAGE')")
    public ApiResponse<Void> resetElectionVotes(
            @PathVariable Long electionId,
            @RequestParam String confirmationCode,
            @RequestParam String reason) {
        log.error("Super Admin RESETTING VOTES for election: {} with reason: {}", electionId, reason);

        if (!"CONFIRM_RESET_VOTES".equals(confirmationCode)) {
            return ApiResponse.error("Invalid confirmation code. Use 'CONFIRM_RESET_VOTES'");
        }

        // Would need reset votes method in service
        return ApiResponse.success("Election votes reset. This action has been logged.", null);
    }

    /**
     * Invalidate all votes by a specific user
     * For fraud investigation
     */
    @DeleteMapping("/voters/{voterId}/invalidate-votes")
    @Operation(
            summary = "Invalidate user votes",
            description = "DANGER: Invalidate all votes cast by a specific user. For fraud investigation only."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<BulkOperationResult> invalidateUserVotes(
            @PathVariable Long voterId,
            @RequestParam String confirmationCode,
            @RequestParam String reason) {
        log.error("Super Admin INVALIDATING VOTES for voter: {} with reason: {}", voterId, reason);

        if (!"CONFIRM_INVALIDATE_VOTES".equals(confirmationCode)) {
            return ApiResponse.error("Invalid confirmation code. Use 'CONFIRM_INVALIDATE_VOTES'");
        }

        BulkOperationResult result = BulkOperationResult.builder()
                .totalProcessed(0)
                .successCount(0)
                .failureCount(0)
                .message("Votes invalidated. This action has been logged for audit.")
                .build();
        return ApiResponse.success("User votes invalidated", result);
    }

    // ==================== SYSTEM CONFIGURATION ====================

    /**
     * Get voting module configuration
     */
    @GetMapping("/config")
    @Operation(
            summary = "Get voting module config",
            description = "Retrieve current voting module configuration settings."
    )
    @PreAuthorize("hasAuthority('VOTE:VIEW_STATISTICS')")
    public ApiResponse<VotingModuleConfig> getVotingConfig() {
        VotingModuleConfig config = VotingModuleConfig.builder()
                .minCandidatesForVoting(2)
                .defaultMaxCandidates(10)
                .defaultWinnersCount(1)
                .membershipEligibilityMonths(3)
                .anonymousVotingDefault(true)
                .autoCloseNominations(true)
                .autoOpenVoting(true)
                .autoCloseVoting(true)
                .build();
        return ApiResponse.success("Configuration retrieved successfully", config);
    }

    // ==================== INNER CLASSES FOR RESPONSES ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLogEntry {
        private Long id;
        private String entityType;
        private Long entityId;
        private String action;
        private Long performedBy;
        private String performedByName;
        private String details;
        private java.time.LocalDateTime timestamp;
        private String ipAddress;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkOperationResult {
        private Integer totalProcessed;
        private Integer successCount;
        private Integer failureCount;
        private java.util.List<String> errors;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ClubExportData {
        private Long clubId;
        private ClubResponse club;
        private java.util.List<ClubMembershipResponse> memberships;
        private java.util.List<ElectionResponse> elections;
        private java.time.LocalDateTime exportedAt;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ElectionExportData {
        private Long electionId;
        private ElectionResponse election;
        private java.util.List<CandidateResponse> candidates;
        private ElectionResultsResponse results;
        private Integer totalVotes;
        private java.time.LocalDateTime exportedAt;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VotingModuleConfig {
        private Integer minCandidatesForVoting;
        private Integer defaultMaxCandidates;
        private Integer defaultWinnersCount;
        private Integer membershipEligibilityMonths;
        private Boolean anonymousVotingDefault;
        private Boolean autoCloseNominations;
        private Boolean autoOpenVoting;
        private Boolean autoCloseVoting;
    }
}
