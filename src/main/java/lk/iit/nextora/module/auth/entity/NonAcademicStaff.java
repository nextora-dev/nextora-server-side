package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "non_academic_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonAcademicStaff extends BaseUser {

    @Column(nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(length = 100)
    private String workLocation;

    private LocalDate joinDate;

    @Column(length = 50)
    private String shift;

    @Override
    public String getUserType() {
        return "NON_ACADEMIC_STAFF";
    }
}