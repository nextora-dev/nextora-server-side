package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a student policy document.
 */
@Entity
@Table(name = "intranet_student_policies", uniqueConstraints = {
        @UniqueConstraint(columnNames = "policy_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPolicy extends BaseEntity {

    @Column(name = "policy_name", nullable = false, length = 300)
    private String policyName;

    @Column(name = "policy_slug", nullable = false, unique = true, length = 200)
    private String policySlug;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "effective_date", length = 20)
    private String effectiveDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "policy_content", columnDefinition = "TEXT")
    private String policyContent;

    @Column(name = "policy_file_url", length = 500)
    private String policyFileUrl;

    @ElementCollection
    @CollectionTable(name = "intranet_policy_key_points", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "key_point", length = 500)
    @Builder.Default
    private List<String> keyPoints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "intranet_policy_disciplinary_process", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "process_step", length = 500)
    @Builder.Default
    private List<String> disciplinaryProcess = new ArrayList<>();

    // Contact person stored as embedded fields
    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_role", length = 200)
    private String contactRole;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;
}

