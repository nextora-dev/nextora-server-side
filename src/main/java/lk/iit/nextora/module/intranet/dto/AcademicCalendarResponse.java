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
public class AcademicCalendarResponse {
    private Long id;
    private String universityName;
    private String universitySlug;
    private String academicYear;
    private String calendarFileUrl;
    private List<CalendarEventResponse> events;
    private String lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEventResponse {
        private String eventName;
        private String startDate;
        private String endDate;
        private String eventType;
    }
}
