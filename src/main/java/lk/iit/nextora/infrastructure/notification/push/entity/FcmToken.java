package lk.iit.nextora.infrastructure.notification.push.entity;

import lk.iit.nextora.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

/**
 * FCM Token Entity
 *
 * Design Decisions:
 * 1. Token is unique per user to prevent duplicates
 * 2. Role stored at registration time (may differ from current role)
 * 3. Active flag for soft-delete (enables analytics)
 * 4. lastUsedAt tracks token freshness
 *
 * Indexing Strategy:
 * - Composite unique constraint on (token, userId)
 * - Index on role for role-based notifications
 * - Index on active for filtering valid tokens
 */
@Entity
@Table(name = "fcm_tokens",
    indexes = {
        @Index(name = "idx_fcm_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_fcm_tokens_role", columnList = "role"),
        @Index(name = "idx_fcm_tokens_active", columnList = "is_active")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Firebase Cloud Messaging token.
     * Max 500 chars to accommodate FCM token length.
     */
    @Column(nullable = false, length = 500, unique = true)
    private String token;

    /**
     * Reference to the user who owns this token.
     * Stored as Long to allow flexible user table design.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * User role at the time of token registration.
     * Used for role-based notification targeting.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    /**
     * Device type for analytics and targeting.
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;

    /**
     * Device info for additional context.
     */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    /**
     * Active flag for soft-delete functionality.
     * Inactive tokens are not used for sending notifications.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Timestamp when the token was first registered.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    /**
     * Timestamp when the token was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    /**
     * Timestamp when a notification was last sent to this token.
     * Used for token freshness tracking and cleanup.
     */
    @Column(name = "last_used_at")
    private ZonedDateTime lastUsedAt;

    /**
     * Deactivates the token (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Updates the last used timestamp.
     */
    public void markAsUsed() {
        this.lastUsedAt = ZonedDateTime.now();
    }
}
