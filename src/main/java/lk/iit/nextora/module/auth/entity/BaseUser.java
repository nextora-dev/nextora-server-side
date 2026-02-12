package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.Permission;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseUser extends BaseEntity implements UserDetails {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 20)
    private String phoneNumber;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "profile_picture_key", length = 255)
    private String profilePictureKey;

    /**
     * Number of consecutive failed login attempts.
     * Reset to 0 on successful login.
     * Account is suspended after 5 failed attempts per day (for non-admin users).
     */
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    /**
     * Timestamp of the last failed login attempt.
     * Used to reset attempts for a new day.
     */
    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Version
    private Long version;

    /**
     * Returns the user's role and permissions as Spring Security GrantedAuthorities.
     * This enables role-based and permission-based authorization (@PreAuthorize, hasRole, hasAuthority, etc.)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role as authority
        authorities.add(new SimpleGrantedAuthority(role.name()));

        // Add all permissions from the role
        Set<Permission> permissions = role.getPermissions();
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermission()));
        }

        // Add additional permissions from student sub-role if applicable
        Set<Permission> additionalPermissions = getAdditionalPermissions();
        for (Permission permission : additionalPermissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermission()));
        }

        return authorities;
    }

    /**
     * Override in Student entity to return sub-role permissions
     */
    protected Set<Permission> getAdditionalPermissions() {
        return Set.of();
    }

    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return role.hasPermission(permission) || getAdditionalPermissions().contains(permission);
    }

    /**
     * Check if user has a specific permission by string
     */
    public boolean hasPermission(String permissionString) {
        return getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(permissionString));
    }

    /**
     * Returns the email as the username for Spring Security.
     * This ensures JWT tokens use email as the subject.
     */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != null && status != UserStatus.SUSPENDED && status != UserStatus.DELETED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Allow users with PASSWORD_CHANGE_REQUIRED status to log in
        // so they can change their password on first login
        return UserStatus.ACTIVE.equals(status) || UserStatus.PASSWORD_CHANGE_REQUIRED.equals(status);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public abstract String getUserType();
}
