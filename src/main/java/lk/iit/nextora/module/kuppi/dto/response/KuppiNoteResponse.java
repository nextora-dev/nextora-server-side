package lk.iit.nextora.module.kuppi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Kuppi note details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiNoteResponse {

    private Long id;
    private String title;
    private String description;
    private String fileType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String formattedFileSize; // Human-readable file size (e.g., "2.50 MB")
    private Boolean allowDownload;
    private Long downloadCount;
    private Long viewCount;

    // Session details
    private Long sessionId;
    private String sessionTitle;

    // Uploader details
    private Long uploadedById;
    private String uploaderName;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}

