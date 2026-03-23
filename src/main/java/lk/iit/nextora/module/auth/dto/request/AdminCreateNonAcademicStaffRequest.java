package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminCreateNonAcademicStaffRequest extends AdminCreateUserRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20)
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @NotBlank(message = "Position is required")
    @Size(max = 50)
    private String position;

    @Size(max = 100)
    private String officeLocation;

    @PastOrPresent
    private LocalDate joinDate;
}
