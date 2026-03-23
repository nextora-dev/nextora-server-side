package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import lk.iit.nextora.common.enums.FacultyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AcademicStaffRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotNull(message = "Faculty is required")
    private FacultyType faculty;

    @NotBlank(message = "Position is required")
    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    @Size(max = 100, message = "Office location must not exceed 100 characters")
    private String officeLocation;

    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate joinDate;

    @Size(max = 500, message = "Responsibilities must not exceed 500 characters")
    private String responsibilities;

    // Lecturer-specific fields (optional)
    @Size(max = 50, message = "Designation must not exceed 50 characters")
    private String designation;

    @Size(max = 50, message = "Specialization must not exceed 50 characters")
    private String specialization;

    private Set<String> qualifications;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
