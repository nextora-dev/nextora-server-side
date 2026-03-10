package lk.iit.nextora.module.intranet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoResponse {
    private String categoryName;
    private String categorySlug;
    private String description;
    private List<ProgrammeCategoryResponse> programmeCategories;
    private AdmissionsContactResponse admissionsContact;
    private List<HouseResponse> houses;
    private String currentAcademicYear;
    private String office;
    private String email;
    private String phone;
    private Map<String, String> socialMedia;
    private CurrentOfficeResponse currentOffice;
    private List<UpcomingEventResponse> upcomingEvents;
    private Integer totalClubs;
    private List<ClubInfoResponse> clubs;
    private String joinInstructions;
    private String lastUpdated;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProgrammeCategoryResponse {
        private String category;
        private List<ProgrammeSummaryResponse> programmes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProgrammeSummaryResponse {
        private String programName;
        private String duration;
        private String awardingBody;
        private String intake;
        private String fee;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AdmissionsContactResponse {
        private String email;
        private String phone;
        private String whatsapp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HouseResponse {
        private Long id;
        private String houseName;
        private String color;
        private String motto;
        private String description;
        private String housemaster;
        private String captainName;
        private String logoUrl;
        private Integer totalPoints;
        private Integer rank;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CurrentOfficeResponse {
        private String academicYear;
        private List<OfficeBearerResponse> officeBearers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OfficeBearerResponse {
        private String position;
        private String name;
        private String email;
        private String programme;
        private Integer year;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpcomingEventResponse {
        private String eventName;
        private String date;
        private String venue;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClubInfoResponse {
        private Long id;
        private String clubName;
        private String clubCode;
        private String category;
        private String description;
        private String logoUrl;
        private String president;
        private String email;
        private Integer memberCount;
        private Boolean isOpenForRegistration;
        private Map<String, String> socialMedia;
    }
}

