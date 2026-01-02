package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Student ID is required")
    @Size(max = 20, message = "Student ID must not exceed 20 characters")
    private String studentId;

    @NotBlank(message = "Batch is required")
    @Size(max = 50, message = "Batch must not exceed 50 characters")
    private String batch;

    @NotBlank(message = "Program is required")
    @Size(max = 100, message = "Program must not exceed 100 characters")
    private String program;

    @NotBlank(message = "Faculty is required")
    @Size(max = 50, message = "Faculty must not exceed 50 characters")
    private String faculty;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @Size(max = 50, message = "Guardian name must not exceed 50 characters")
    private String guardianName;

    @Size(max = 15, message = "Guardian phone must not exceed 15 characters")
    private String guardianPhone;
}