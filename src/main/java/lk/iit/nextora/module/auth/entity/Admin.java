package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.UserRole;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String adminId;

    @Column(nullable = false, length = 100)
    private String department;

    @ElementCollection
    @CollectionTable(name = "admin_permissions",
            joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>();

    private LocalDate assignedDate;


    @Override
    public String getUserType() {
        return "ADMIN";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return getFirstName() + " " + getLastName();
    }
}