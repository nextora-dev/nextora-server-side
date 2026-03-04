package lk.iit.nextora.module.club.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a club announcement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementRequest {

    @NotNull(message = "Club ID is required")
    private Long clubId;

    @NotBlank(message = "Announcement title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Announcement content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Builder.Default
    private ClubAnnouncement.AnnouncementPriority priority = ClubAnnouncement.AnnouncementPriority.NORMAL;

    @Builder.Default
    private Boolean isPinned = false;

    @Builder.Default
    private Boolean isMembersOnly = false;
}

