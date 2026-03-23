package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.*;
import lk.iit.nextora.common.enums.FacultyType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCreateAcademicStaffRequest extends AdminCreateUserRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20)
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @NotNull(message = "Faculty is required")
    private FacultyType faculty;

    @NotBlank(message = "Position is required")
    @Size(max = 50)
    private String position;

    @Size(max = 100)
    private String officeLocation;

    @PastOrPresent
    private LocalDate joinDate;

    @Size(max = 500)
    private String responsibilities;

    @Size(max = 50)
    private String designation;

    @Size(max = 50)
    private String specialization;

    private Set<String> qualifications;

    @Size(max = 500)
    private String bio;
}
