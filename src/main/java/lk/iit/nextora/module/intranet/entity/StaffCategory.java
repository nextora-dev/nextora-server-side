package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a staff category (SOC, common-info, mail-groups, doc-arch, contacts).
 * Complex nested data (staffMembers, departments, mailGroups, documents) stored in contentJson.
 */
@Entity
@Table(name = "intranet_staff_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCategory extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "department_full_name", length = 300)
    private String departmentFullName;

    /**
     * JSON-stored nested data: staffMembers, generalInfo, departments, mailGroups, documents, emergencyContacts.
     * Deserialized by the mapper/service layer into the appropriate DTO sub-types.
     */
    @Column(name = "content_json", columnDefinition = "TEXT")
    private String contentJson;
}

