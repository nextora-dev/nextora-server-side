package lk.iit.nextora.module.kuppi.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing notes uploaded for a Kuppi session
 */
@Entity
@Table(name = "kuppi_notes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiNote extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(length = 500)
    private String fileUrl;

    @Column(length = 100)
    private String fileName;

    @Column
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private KuppiSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Student uploadedBy;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowDownload = true;

    @Column(nullable = false)
    @Builder.Default
    private Long downloadCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean isDownloadable() {
        return this.allowDownload;
    }
}

