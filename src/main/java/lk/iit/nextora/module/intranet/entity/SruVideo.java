package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a help desk video in the Students Relations Unit.
 * Belongs to a {@link SruCategory} in a bidirectional one-to-many relationship.
 */
@Entity
@Table(name = "intranet_sru_videos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SruVideo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sru_category_id", nullable = false)
    private SruCategory sruCategory;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration", length = 20)
    private String duration;

    @Column(name = "published_date", length = 20)
    private String publishedDate;
}

