package lk.iit.nextora.module.club.dto.response;

import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for club announcement details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubAnnouncementResponse {

    private Long id;

    // Club info
    private Long clubId;
    private String clubCode;
    private String clubName;

    // Announcement details
    private String title;
    private String content;
    private ClubAnnouncement.AnnouncementPriority priority;
    private Boolean isPinned;
    private Boolean isMembersOnly;

    // Author details
    private Long authorId;
    private String authorName;
    private String authorEmail;

    // Attachments
    private String attachmentUrl;
    private String attachmentName;

    // Stats
    private Long viewCount;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}

