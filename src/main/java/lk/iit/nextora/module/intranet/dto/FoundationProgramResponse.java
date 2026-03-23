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
public class FoundationProgramResponse {
    private Long id;
    private String categoryName;
    private String categorySlug;
    private String description;
    private String academicYear;
    private String calendarFileUrl;
    private List<AcademicCalendarResponse.CalendarEventResponse> events;
    private String programName;
    private String duration;
    private String specificationFileUrl;
    private List<FoundationModuleResponse> modules;
    private Integer totalCredits;
    private List<ContactResponse> contacts;
    private String semester;
    private String effectiveFrom;
    private String timetableFileUrl;
    private List<ScheduleDayResponse> schedule;
    private String scheduleFileUrl;
    private List<AssessmentResponse> assessments;
    private String lmsName;
    private String lmsUrl;
    private String loginInstructions;
    private String usernameFormat;
    private String defaultPasswordInfo;
    private String passwordResetUrl;
    private ContactResponse supportContact;
    private List<String> browserRequirements;
    private String additionalNotes;
    private String formName;
    private String formFileUrl;
    private String submissionEmail;
    private String submissionDeadline;
    private List<String> eligibleCircumstances;
    private List<String> requiredEvidence;
    private ContactResponse contactPerson;
    private String lastUpdated;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FoundationModuleResponse {
        private String moduleCode;
        private String moduleName;
        private Integer credits;
        private Integer semester;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ContactResponse {
        private String role;
        private String name;
        private String email;
        private String phone;
        private String officeHours;
        private String office;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScheduleDayResponse {
        private String day;
        private List<TimeSlotResponse> slots;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TimeSlotResponse {
        private String startTime;
        private String endTime;
        private String moduleCode;
        private String moduleName;
        private String lecturer;
        private String venue;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AssessmentResponse {
        private String moduleCode;
        private String moduleName;
        private String assessmentType;
        private String description;
        private Integer weightPercentage;
        private String releaseDate;
        private String submissionDeadline;
        private String feedbackDate;
    }
}

