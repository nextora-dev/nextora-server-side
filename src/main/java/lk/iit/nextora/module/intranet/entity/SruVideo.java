package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
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

    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    private String duration;

    private String publishedDate;
}

