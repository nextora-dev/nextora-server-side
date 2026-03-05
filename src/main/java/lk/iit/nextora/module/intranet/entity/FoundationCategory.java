package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a foundation program information category.
 * Each category (academic-calendar, time-table, etc.) stores its
 * polymorphic nested JSON data in the {@code contentJson} column.
 */
@Entity
@Table(name = "intranet_foundation_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FoundationCategory extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "semester", length = 50)
    private String semester;

    @Column(name = "effective_from", length = 30)
    private String effectiveFrom;

    @Column(name = "program_name", length = 300)
    private String programName;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "total_credits")
    private Integer totalCredits;

    // ===== URL fields =====
    @Column(name = "calendar_file_url", length = 500)
    private String calendarFileUrl;

    @Column(name = "specification_file_url", length = 500)
    private String specificationFileUrl;

    @Column(name = "timetable_file_url", length = 500)
    private String timetableFileUrl;

    @Column(name = "schedule_file_url", length = 500)
    private String scheduleFileUrl;

    @Column(name = "form_file_url", length = 500)
    private String formFileUrl;

    // ===== LMS fields =====
    @Column(name = "lms_name", length = 100)
    private String lmsName;

    @Column(name = "lms_url", length = 500)
    private String lmsUrl;

    @Column(name = "login_instructions", columnDefinition = "TEXT")
    private String loginInstructions;

    @Column(name = "username_format", length = 200)
    private String usernameFormat;

    @Column(name = "default_password_info", columnDefinition = "TEXT")
    private String defaultPasswordInfo;

    @Column(name = "password_reset_url", length = 500)
    private String passwordResetUrl;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    // ===== Mitigation form fields =====
    @Column(name = "form_name", length = 300)
    private String formName;

    @Column(name = "submission_email", length = 200)
    private String submissionEmail;

    @Column(name = "submission_deadline", length = 300)
    private String submissionDeadline;

    // ===== JSON-stored nested data (schedule, assessments, contacts, modules, etc.) =====
    @Column(name = "content_json", columnDefinition = "TEXT")
    private String contentJson;

    @ElementCollection
    @CollectionTable(name = "intranet_foundation_browser_requirements", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "requirement", length = 300)
    @Builder.Default
    private List<String> browserRequirements = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_foundation_eligible_circumstances", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "circumstance", length = 500)
    @Builder.Default
    private List<String> eligibleCircumstances = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_foundation_required_evidence", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "evidence", length = 500)
    @Builder.Default
    private List<String> requiredEvidence = new ArrayList<>();
}

