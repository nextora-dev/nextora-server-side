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
public class ScheduleCategoryResponse {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private String description;
    private List<ScheduleEventResponse> events;
    private Boolean isActive;
    private String lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleEventResponse {
        private Long id;
        private String eventName;
        private String description;
        private String startDate;
        private String endDate;
        private String venue;
        private String eventType;
        private Boolean isActive;
    }
}

