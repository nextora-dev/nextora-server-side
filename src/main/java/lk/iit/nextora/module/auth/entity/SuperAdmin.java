package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.UserRole;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "super_admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdmin extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String superAdminId;

    private LocalDate assignedDate;

    @Column(columnDefinition = "TEXT")
    private String accessLevel;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (getRole() == null) {
            setRole(UserRole.ROLE_SUPER_ADMIN);
        }
    }

    @Override
    public String getUserType() {
        return "SUPER_ADMIN";
    }
}