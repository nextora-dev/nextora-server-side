package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 15)
    private String phoneNumber;

//    @Column(name = "account_locked", nullable = false)
//    private Boolean accountLocked = false;
//
//    @Column(name = "credentials_expired", nullable = false)
//    private Boolean credentialsExpired = false;
//
//    @Column(name = "account_expired", nullable = false)
//    private Boolean accountExpired = false;
//
//    @Column(name = "enabled", nullable = false)
//    private Boolean enabled = true;

    @Version
    private Long version;

//    @Override
//    public boolean isAccountNonExpired() {
//        return !accountExpired;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return !accountLocked;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return !credentialsExpired;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return enabled;
//    }

    /**
     * Returns the user's role as Spring Security GrantedAuthority.
     * This enables role-based authorization (@PreAuthorize, hasRole, etc.)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the email as the username for Spring Security.
     * This ensures JWT tokens use email as the subject.
     */
    @Override
    public String getUsername() {
        return email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public abstract String getUserType();
}
