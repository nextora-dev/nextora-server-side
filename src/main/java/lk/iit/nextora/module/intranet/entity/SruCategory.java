package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Students Relations Unit category (root info or sub-categories like video series).
 * Contains a bidirectional one-to-many relationship with {@link SruVideo}.
 */
@Entity
@Table(name = "intranet_sru_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SruCategory extends BaseEntity {

    @Column(name = "category_slug", nullable = false, unique = true, length = 200)
    private String categorySlug;

    @Column(name = "category_name", length = 200)
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Root-level SRU info fields
    @Column(name = "unit_name", length = 200)
    private String unitName;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "office_hours", length = 200)
    private String officeHours;

    @OneToMany(mappedBy = "sruCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @Builder.Default
    private List<SruVideo> videos = new ArrayList<>();

    public void addVideo(SruVideo video) {
        videos.add(video);
        video.setSruCategory(this);
    }
}

