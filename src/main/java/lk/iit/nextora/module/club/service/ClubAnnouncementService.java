package lk.iit.nextora.module.club.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.club.dto.request.CreateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.request.UpdateAnnouncementRequest;
import lk.iit.nextora.module.club.dto.response.ClubAnnouncementResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Club Announcement operations.
 * Handles CRUD, pinning, and visibility control for club announcements.
 */
public interface ClubAnnouncementService {

    ClubAnnouncementResponse createAnnouncement(CreateAnnouncementRequest request, MultipartFile attachment);

    ClubAnnouncementResponse updateAnnouncement(Long announcementId, UpdateAnnouncementRequest request, MultipartFile attachment);

    ClubAnnouncementResponse getAnnouncementById(Long announcementId);

    void deleteAnnouncement(Long announcementId);

    PagedResponse<ClubAnnouncementResponse> getAnnouncementsByClub(Long clubId, Pageable pageable);

    PagedResponse<ClubAnnouncementResponse> getPublicAnnouncementsByClub(Long clubId, Pageable pageable);

    PagedResponse<ClubAnnouncementResponse> getPinnedAnnouncementsByClub(Long clubId, Pageable pageable);

    PagedResponse<ClubAnnouncementResponse> searchAnnouncements(String keyword, Pageable pageable);

    ClubAnnouncementResponse pinAnnouncement(Long announcementId);

    ClubAnnouncementResponse unpinAnnouncement(Long announcementId);

    /**
     * Permanently delete an announcement from the database and remove associated S3 files.
     * Super Admin only — this action is irreversible.
     */
    void permanentlyDeleteAnnouncement(Long announcementId);
}

