package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.ArrayList;

/**
 * Entity representing a student policy document.
 * Stores policy information including content, version, and contact details.
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Column(name = "policy_name", nullable = false, length = 300)
    private String policyName;

    @Column(name = "policy_slug", nullable = false, unique = true, length = 300)
    private String policySlug;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "effective_date", length = 50)
    private String effectiveDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "policy_content", columnDefinition = "TEXT")
    private String policyContent;

    @Column(name = "policy_file_url", length = 500)
    private String policyFileUrl;

    @Column(name = "key_points_json", columnDefinition = "TEXT")
    private String keyPointsJson;

    @Column(name = "disciplinary_process_json", columnDefinition = "TEXT")
    private String disciplinaryProcessJson;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_role", length = 200)
    private String contactRole;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Helper method to convert List to JSON and set keyPointsJson
     */
    public StudentPolicy keyPoints(List<String> keyPoints) {
        try {
            this.keyPointsJson = keyPoints != null ? objectMapper.writeValueAsString(keyPoints) : null;
        } catch (Exception e) {
            this.keyPointsJson = null;
        }
        return this;
    }

    /**
     * Helper method to convert List to JSON and set disciplinaryProcessJson
     */
    public StudentPolicy disciplinaryProcess(List<String> disciplinaryProcess) {
        try {
            this.disciplinaryProcessJson = disciplinaryProcess != null ? objectMapper.writeValueAsString(disciplinaryProcess) : null;
        } catch (Exception e) {
            this.disciplinaryProcessJson = null;
        }
        return this;
    }
}

