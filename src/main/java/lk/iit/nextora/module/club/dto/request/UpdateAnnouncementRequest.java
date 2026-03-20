package lk.iit.nextora.module.club.dto.request;

import jakarta.validation.constraints.Size;
import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing club announcement.
 * All fields are optional — only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnnouncementRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    private ClubAnnouncement.AnnouncementPriority priority;

    private Boolean isPinned;

    private Boolean isMembersOnly;
}

