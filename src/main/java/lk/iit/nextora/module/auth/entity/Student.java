package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.UserRole;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(nullable = false, length = 50)
    private String batch;

    @Column(nullable = false, length = 100)
    private String program;

    @Column(nullable = false, length = 50)
    private String faculty;

    private LocalDate enrollmentDate;

    private LocalDate dateOfBirth;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String guardianName;

    @Column(length = 15)
    private String guardianPhone;


    @Override
    public String getUserType() {
        return "STUDENT";
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

