package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
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
    @Builder.Default
    private Set<String> qualifications = new HashSet<>();

    private LocalDate joinDate;

    @Column(length = 100)
    private String officeLocation;

    @Column(length = 500)
    private String bio;

    @Builder.Default
    private Boolean availableForMeetings = true;

    @Override
    public String getUserType() {
        return "LECTURER";
    }
}
