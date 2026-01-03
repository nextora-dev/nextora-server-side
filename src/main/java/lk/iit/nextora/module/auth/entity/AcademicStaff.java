package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(nullable = false, length = 50)
    private String position;

    @Column(length = 100)
    private String officeLocation;

    private LocalDate joinDate;

    @Column(length = 500)
    private String responsibilities;

    @Override
    public String getUserType() {
        return "ACADEMIC_STAFF";
    }
}