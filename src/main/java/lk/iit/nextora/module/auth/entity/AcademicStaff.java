package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.enums.FacultyType;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "academic_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicStaff extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FacultyType faculty;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(length = 100)
    private String officeLocation;

    private LocalDate joinDate;

    @Column(length = 500)
    private String responsibilities;

    // Lecturer-specific fields (merged from Lecturer entity)
    @Column(length = 50)
    private String designation;

    @Column(length = 50)
    private String specialization;

    @ElementCollection
    @CollectionTable(name = "academic_staff_qualifications",
            joinColumns = @JoinColumn(name = "academic_staff_id"))
    @Column(name = "qualification")
    @Builder.Default
    private Set<String> qualifications = new HashSet<>();

    @Column(length = 500)
    private String bio;

    @Builder.Default
    private Boolean availableForMeetings = true;

    @Override
    public String getUserType() {
        return "ACADEMIC_STAFF";
    }
}