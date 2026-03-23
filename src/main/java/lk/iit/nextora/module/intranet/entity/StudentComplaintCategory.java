package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a student complaint category.
 */
@Entity
@Table(name = "intranet_student_complaint_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StudentComplaintCategory extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "form_url", length = 500)
    private String formUrl;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "response_time_business_days")
    private Integer responseTimeBusinessDays;
}

