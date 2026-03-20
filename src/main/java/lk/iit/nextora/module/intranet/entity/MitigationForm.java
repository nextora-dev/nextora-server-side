package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a mitigating circumstances form (UoW / RGU).
 */
@Entity
@Table(name = "intranet_mitigation_forms", uniqueConstraints = {
        @UniqueConstraint(columnNames = "form_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MitigationForm extends BaseEntity {

    @Column(name = "form_name", nullable = false, length = 300)
    private String formName;

    @Column(name = "form_slug", nullable = false, unique = true, length = 200)
    private String formSlug;

    @Column(name = "university", length = 300)
    private String university;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "form_file_url", length = 500)
    private String formFileUrl;

    @Column(name = "submission_email", length = 200)
    private String submissionEmail;

    @Column(name = "submission_deadline", length = 300)
    private String submissionDeadline;

    @Column(name = "processing_time_business_days")
    private Integer processingTimeBusinessDays;

    @Column(name = "extension_duration", length = 200)
    private String extensionDuration;

    @Column(name = "deferral_details", columnDefinition = "TEXT")
    private String deferralDetails;

    // Contact person
    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @ElementCollection
    @CollectionTable(name = "intranet_mitigation_eligible_circumstances", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "circumstance", length = 500)
    @Builder.Default
    private List<String> eligibleCircumstances = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_mitigation_required_documents", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "document", length = 500)
    @Builder.Default
    private List<String> requiredDocuments = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_mitigation_limitations", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "limitation", length = 500)
    @Builder.Default
    private List<String> limitations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_mitigation_possible_outcomes", joinColumns = @JoinColumn(name = "form_id"))
    @Column(name = "outcome", length = 500)
    @Builder.Default
    private List<String> possibleOutcomes = new ArrayList<>();
}

