package lk.iit.nextora.module.event.entity;

import jakarta.persistence.*;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_reg_seq")
    @SequenceGenerator(name = "event_reg_seq", sequenceName = "event_registration_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private BaseUser user;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCancelled = false;

    @Column
    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    public void cancel() {
        this.isCancelled = true;
        this.isActive = false;
        this.cancelledAt = LocalDateTime.now();
    }

    public void reRegister() {
        this.isCancelled = false;
        this.isActive = true;
        this.cancelledAt = null;
        this.registeredAt = LocalDateTime.now();
    }
}
