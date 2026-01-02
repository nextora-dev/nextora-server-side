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
@Table(name = "lecturers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecturer extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String faculty;

    @Column(length = 50)
    private String designation;

    @Column(length = 50)
    private String specialization;

    @ElementCollection
    @CollectionTable(name = "lecturer_qualifications",
            joinColumns = @JoinColumn(name = "lecturer_id"))
    @Column(name = "qualification")
    private Set<String> qualifications = new HashSet<>();

    private LocalDate joinDate;

    @Column(length = 100)
    private String officeLocation;

    @Column(length = 500)
    private String bio;

    private Boolean availableForMeetings = true;


    @Override
    public String getUserType() {
        return "LECTURER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return getFirstName()+" "+getLastName();
    }
}
