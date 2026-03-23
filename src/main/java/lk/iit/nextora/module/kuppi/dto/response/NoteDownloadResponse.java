package lk.iit.nextora.module.kuppi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for note file download
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDownloadResponse {

    private Long noteId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private byte[] fileContent;
    private String downloadUrl;
}
