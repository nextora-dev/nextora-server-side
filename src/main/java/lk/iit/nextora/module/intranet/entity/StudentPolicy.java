package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    private String policySlug;

    private String version;

    private String effectiveDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "policy_content", columnDefinition = "TEXT")
    private String policyContent;

    @Column(name = "policy_file_url", length = 500)
    private String policyFileUrl;



    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_role", length = 200)
    private String contactRole;

    private String contactEmail;
}

