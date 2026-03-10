package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a general info category (course-details, houses, students-union, clubs-and-societies).
 * Complex nested data stored in contentJson.
 */
@Entity
@Table(name = "intranet_info_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfoCategory extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * JSON-stored nested data: programmeCategories, houses, officeBearers, clubs, etc.
     * Deserialized by the mapper/service layer into the appropriate DTO sub-types.
     */
    @Column(name = "content_json", columnDefinition = "TEXT")
    private String contentJson;
}

