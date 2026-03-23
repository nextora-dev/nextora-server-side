package lk.iit.nextora.module.lostandfound.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String category;
    private String location;
    private String contactNumber;
    private Long reportedBy;
    private String reporterName;
    private boolean active;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
