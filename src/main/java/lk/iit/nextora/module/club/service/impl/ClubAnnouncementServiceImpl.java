package lk.iit.nextora.module.club.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.common.enums.ClubPositionsType;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.club.dto.request.CreateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.request.UpdateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import lk.iit.nextora.module.club.entity.Club;
import lk.iit.nextora.module.club.entity.ClubActivityLog;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lk.iit.nextora.module.club.mapper.ClubMapper;
import lk.iit.nextora.module.club.repository.ClubAnnouncementRepository;
import lk.iit.nextora.module.club.repository.ClubMembershipRepository;
import lk.iit.nextora.module.club.repository.ClubRepository;
import lk.iit.nextora.module.club.service.ClubActivityLogService;
import lk.iit.nextora.module.club.service.ClubAnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubAnnouncementServiceImpl implements ClubAnnouncementService {

    private static final String ANNOUNCEMENT_FOLDER = "clubs/announcements";

    private final ClubAnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;
    private final ClubMapper clubMapper;
    private final ClubActivityLogService activityLogService;
    private final lk.iit.nextora.config.S3.S3Service s3Service;

    @Override
    @Transactional
    public ClubAnnouncementResponse createAnnouncement(CreateAnnouncementRequest request, MultipartFile attachment) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("User {} creating announcement for club {}", currentUserId, request.getClubId());

        Club club = findClubById(request.getClubId());
        validateClubOfficerAccess(club);

        Student author = findStudentById(currentUserId);

        ClubAnnouncement announcement = ClubAnnouncement.builder()
                .club(club)
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .priority(request.getPriority())
                .isPinned(request.getIsPinned())
                .isMembersOnly(request.getIsMembersOnly())
                .build();

        if (attachment != null && !attachment.isEmpty()) {
            validateAttachment(attachment);
            String url = s3Service.uploadFilePublic(attachment, ANNOUNCEMENT_FOLDER);
            announcement.setAttachmentUrl(url);
            announcement.setAttachmentName(attachment.getOriginalFilename());
        }

        announcement = announcementRepository.save(announcement);

        activityLogService.log(club, ClubActivityLog.ActivityType.ANNOUNCEMENT_POSTED,
                "Announcement posted: " + announcement.getTitle(),
                currentUserId, author.getFullName(),
                null, null, announcement.getId(), "ClubAnnouncement", null);

        log.info("Announcement created: {} (ID: {})", announcement.getTitle(), announcement.getId());
        return clubMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public ClubAnnouncementResponse updateAnnouncement(Long announcementId, UpdateAnnouncementRequest request, MultipartFile attachment) {
        ClubAnnouncement announcement = findAnnouncementById(announcementId);
        validateClubOfficerAccess(announcement.getClub());

        if (request.getTitle() != null) announcement.setTitle(request.getTitle());
        if (request.getContent() != null) announcement.setContent(request.getContent());
        if (request.getPriority() != null) announcement.setPriority(request.getPriority());
        if (request.getIsPinned() != null) announcement.setIsPinned(request.getIsPinned());
        if (request.getIsMembersOnly() != null) announcement.setIsMembersOnly(request.getIsMembersOnly());

        if (attachment != null && !attachment.isEmpty()) {
            validateAttachment(attachment);
            deleteS3File(announcement.getAttachmentUrl());
            String url = s3Service.uploadFilePublic(attachment, ANNOUNCEMENT_FOLDER);
            announcement.setAttachmentUrl(url);
            announcement.setAttachmentName(attachment.getOriginalFilename());
        }

        announcement = announcementRepository.save(announcement);

        Long currentUserId = securityService.getCurrentUserId();
        activityLogService.log(announcement.getClub(), ClubActivityLog.ActivityType.ANNOUNCEMENT_UPDATED,
                "Announcement updated: " + announcement.getTitle(),
                currentUserId, securityService.getCurrentUserEmail(),
                null, null, announcement.getId(), "ClubAnnouncement", null);

        log.info("Announcement updated: {}", announcementId);
        return clubMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public ClubAnnouncementResponse getAnnouncementById(Long announcementId) {
        ClubAnnouncement announcement = findAnnouncementById(announcementId);
        announcement.incrementViewCount();
        announcementRepository.save(announcement);
        return clubMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        ClubAnnouncement announcement = findAnnouncementById(announcementId);
        validateClubOfficerAccess(announcement.getClub());

        announcement.softDelete();
        announcementRepository.save(announcement);

        Long currentUserId = securityService.getCurrentUserId();
        activityLogService.log(announcement.getClub(), ClubActivityLog.ActivityType.ANNOUNCEMENT_DELETED,
                "Announcement deleted: " + announcement.getTitle(),
                currentUserId, securityService.getCurrentUserEmail());

        log.info("Announcement soft-deleted: {}", announcementId);
    }

    @Override
    public PagedResponse<ClubAnnouncementResponse> getAnnouncementsByClub(Long clubId, Pageable pageable) {
        Page<ClubAnnouncement> announcements = announcementRepository.findByClubIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(clubId, pageable);
        return toPagedResponse(announcements);
    }

    @Override
    public PagedResponse<ClubAnnouncementResponse> getPublicAnnouncementsByClub(Long clubId, Pageable pageable) {
        Page<ClubAnnouncement> announcements = announcementRepository.findPublicByClubId(clubId, pageable);
        return toPagedResponse(announcements);
    }

    @Override
    public PagedResponse<ClubAnnouncementResponse> getPinnedAnnouncementsByClub(Long clubId, Pageable pageable) {
        Page<ClubAnnouncement> announcements = announcementRepository.findPinnedByClubId(clubId, pageable);
        return toPagedResponse(announcements);
    }

    @Override
    public PagedResponse<ClubAnnouncementResponse> searchAnnouncements(String keyword, Pageable pageable) {
        Page<ClubAnnouncement> announcements = announcementRepository.searchByKeyword(keyword, pageable);
        return toPagedResponse(announcements);
    }

    @Override
    @Transactional
    public ClubAnnouncementResponse pinAnnouncement(Long announcementId) {
        ClubAnnouncement announcement = findAnnouncementById(announcementId);
        validateClubOfficerAccess(announcement.getClub());

        announcement.setIsPinned(true);
        announcement = announcementRepository.save(announcement);

        Long currentUserId = securityService.getCurrentUserId();
        activityLogService.log(announcement.getClub(), ClubActivityLog.ActivityType.ANNOUNCEMENT_PINNED,
                "Announcement pinned: " + announcement.getTitle(),
                currentUserId, securityService.getCurrentUserEmail());

        return clubMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public ClubAnnouncementResponse unpinAnnouncement(Long announcementId) {
        ClubAnnouncement announcement = findAnnouncementById(announcementId);
        validateClubOfficerAccess(announcement.getClub());

        announcement.setIsPinned(false);
        announcement = announcementRepository.save(announcement);
        return clubMapper.toResponse(announcement);
    }

    @Override
    @Transactional
    public void permanentlyDeleteAnnouncement(Long announcementId) {
        log.info("Super Admin permanently deleting announcement: {}", announcementId);

        ClubAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("ClubAnnouncement", "id", announcementId));

        // Remove attachment from S3 if exists
        deleteS3File(announcement.getAttachmentUrl());

        // Permanently delete from database
        announcementRepository.delete(announcement);

        log.info("Super Admin permanently deleted announcement: {} (title: {})", announcementId, announcement.getTitle());
    }

    // ==================== Helpers ====================

    private Club findClubById(Long clubId) {
        return clubRepository.findByIdAndIsDeletedFalse(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
    }

    private ClubAnnouncement findAnnouncementById(Long id) {
        return announcementRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClubAnnouncement", "id", id));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }

    private void validateClubOfficerAccess(Club club) {
        Long currentUserId = securityService.getCurrentUserId();

        if (securityService.isAdmin() || securityService.isSuperAdmin() || securityService.isNonAcademicstaff()) {
            return;
        }

        if (club.getPresident() != null && club.getPresident().getId().equals(currentUserId)) {
            return;
        }

        var membership = membershipRepository.findByClubIdAndMemberIdAndIsDeletedFalse(club.getId(), currentUserId);
        if (membership.isPresent() && membership.get().getStatus() == ClubMembershipStatus.ACTIVE) {
            var position = membership.get().getPosition();
            if (position != null && isAnnouncementOfficer(position)) {
                return;
            }
        }

        throw new UnauthorizedException("You don't have permission to manage announcements for this club");
    }

    /**
     * Check if the position is allowed to manage announcements.
     * Only PRESIDENT, VICE_PRESIDENT, SECRETARY, TREASURER, and Top_Board_MEMBER can manage announcements.
     */
    private boolean isAnnouncementOfficer(ClubPositionsType position) {
        return position == ClubPositionsType.PRESIDENT ||
               position == ClubPositionsType.VICE_PRESIDENT ||
               position == ClubPositionsType.SECRETARY ||
               position == ClubPositionsType.TREASURER ||
               position == ClubPositionsType.Top_Board_MEMBER;
    }

    private void validateAttachment(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new BadRequestException("Attachment file size must not exceed 10MB");
        }
    }

    private void deleteS3File(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                String[] parts = url.split(".amazonaws.com/");
                if (parts.length > 1) {
                    s3Service.deleteFile(parts[1]);
                }
            } catch (Exception e) {
                log.warn("Failed to delete S3 file: {}", e.getMessage());
            }
        }
    }

    private PagedResponse<ClubAnnouncementResponse> toPagedResponse(Page<ClubAnnouncement> page) {
        return PagedResponse.<ClubAnnouncementResponse>builder()
                .content(clubMapper.toAnnouncementResponseList(page.getContent()))
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

