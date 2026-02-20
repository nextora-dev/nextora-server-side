package lk.iit.nextora.module.kuppi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Detailed Response DTO for Kuppi Student.
 * Contains comprehensive information including hosted sessions and notes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KuppiStudentDetailResponse {

    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String profilePictureUrl;
    private String batch;
    private String program;
    private FacultyType faculty;

    private Set<String> kuppiSubjects;
    private String kuppiExperienceLevel;
    private Integer kuppiSessionsCompleted;
    private Double kuppiRating;
    private String kuppiAvailability;

    private Long totalSessionsHosted;
    private Long completedSessions;
    private Long liveSessions;
    private Long scheduledSessions;
    private Long cancelledSessions;
    private Long totalViews;
    private Long totalNotesUploaded;

    private List<SessionSummary> recentSessions;
    private List<SessionSummary> upcomingSessions;

    private LocalDateTime kuppiApprovedAt;
    private LocalDateTime memberSince;
    private Boolean isActive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private Long id;
        private String title;
        private String subject;
        private KuppiSessionStatus status;
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private Long viewCount;
    }
}

