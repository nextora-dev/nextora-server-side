package lk.iit.nextora.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
}

