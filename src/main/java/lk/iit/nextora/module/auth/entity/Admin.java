package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
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
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    private LocalDate assignedDate;

    @Override
    public String getUserType() {
        return "ADMIN";
    }
}