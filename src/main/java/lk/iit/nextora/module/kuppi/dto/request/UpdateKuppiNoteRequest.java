package lk.iit.nextora.module.kuppi.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a Kuppi note
 * Used by: Kuppi Students (own notes), Admin, Super Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKuppiNoteRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "File type must not exceed 50 characters")
    private String fileType;

    @Size(max = 500, message = "File URL must not exceed 500 characters")
    private String fileUrl;

    @Size(max = 100, message = "File name must not exceed 100 characters")
    private String fileName;

    private Long fileSize;

    private Boolean allowDownload;
}


