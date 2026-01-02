package lk.iit.nextora.module.auth.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LecturerRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Faculty is required")
    @Size(max = 50, message = "Faculty must not exceed 50 characters")
    private String faculty;

    @Size(max = 50, message = "Designation must not exceed 50 characters")
    private String designation;

    @Size(max = 50, message = "Specialization must not exceed 50 characters")
    private String specialization;

    private Set<String> qualifications;

    @Size(max = 100, message = "Office location must not exceed 100 characters")
    private String officeLocation;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}