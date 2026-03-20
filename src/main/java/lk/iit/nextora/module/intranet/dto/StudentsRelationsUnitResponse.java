package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentsRelationsUnitResponse {
    private String unitName;
    private String description;
    private String location;
    private String email;
    private String phone;
    private String officeHours;
    private List<CategorySummaryResponse> categories;
    private String categoryName;
    private List<VideoResponse> videos;
    private String lastUpdated;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategorySummaryResponse {
        private String categoryName;
        private String categorySlug;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VideoResponse {
        private Long id;
        private String title;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;
        private String duration;
        private String publishedDate;
    }
}

