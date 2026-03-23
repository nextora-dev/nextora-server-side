package lk.iit.nextora.module.kuppi.dto.response;

import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.enums.KuppiSessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Kuppi session details
 * Users click on liveLink to join the session on external platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiSessionResponse {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private KuppiSessionType sessionType;
    private KuppiSessionStatus status;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String liveLink;
    private String meetingPlatform;
    private Long viewCount;

    // Full host object (for richer responses)
    private KuppiStudentResponse host;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;

    // Whether session is joinable (time-based)
    private Boolean canJoin;

    // Session notes
    private List<KuppiNoteResponse> notes;
}
