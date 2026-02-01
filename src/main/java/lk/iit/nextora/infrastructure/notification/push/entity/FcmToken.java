package lk.iit.nextora.infrastructure.notification.push.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FCM Token Entity - Stores Firebase Cloud Messaging tokens for push notifications.
 *
 * Design Decisions:
 * - Each user can have multiple tokens (multiple devices)
 * - Token is unique (not the user) to support multi-device scenarios
 * - Soft-delete not used here; invalid tokens are hard-deleted for cleanup
 * - Role stored with token enables role-based notification targeting
 * - lastUsedAt tracks token activity for cleanup of stale tokens
 * - deviceInfo helps identify which device a token belongs to (debugging)
 */
@Entity
@Table(name = "fcm_tokens", indexes = {
        @Index(name = "idx_fcm_token_user_id", columnList = "user_id"),
        @Index(name = "idx_fcm_token_role", columnList = "role"),
        @Index(name = "idx_fcm_token_active", columnList = "is_active"),
        @Index(name = "idx_fcm_token_value", columnList = "token", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fcm_token_seq")
    @SequenceGenerator(name = "fcm_token_seq", sequenceName = "fcm_token_sequence", allocationSize = 1)
    private Long id;

    /**
     * The actual FCM registration token from the client.
     * Unique constraint ensures no duplicate tokens.
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    /**
     * User ID this token belongs to.
     * Not a foreign key to avoid tight coupling with user table.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * User's role at the time of token registration.
     * Used for role-based notification targeting.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /**
     * Whether this token is active and should receive notifications.
     * Set to false when token becomes invalid or user disables notifications.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp of when this token was last used to send a notification.
     * Helps identify stale tokens for cleanup.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Optional device information for debugging and user device management.
     * Example: "Chrome on Windows", "Safari on iPhone"
     */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    /**
     * Timestamp when this token was registered.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this token was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
