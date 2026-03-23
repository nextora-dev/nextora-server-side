package lk.iit.nextora.infrastructure.notification.push.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.NotificationType;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Notification Entity
 *
 * Stores notification history for users.
 * Allows users to view past notifications even if they missed them.
 */
@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_read", columnList = "read"),
        @Index(name = "idx_notifications_type", columnList = "type"),
        @Index(name = "idx_notifications_sent_at", columnList = "sent_at")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private BaseUser user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private NotificationType type = NotificationType.GENERAL;

    @Builder.Default
    @Column(nullable = false)
    private Boolean read = false;

    @Column(name = "click_action", length = 500)
    private String clickAction;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> data;

    @Column(name = "sent_at")
    private ZonedDateTime sentAt;

    @Column(name = "read_at")
    private ZonedDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    /**
     * Mark this notification as read.
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = ZonedDateTime.now();
    }
}
