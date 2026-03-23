package lk.iit.nextora.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
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
public class AdminRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Admin ID is required")
    @Size(max = 20, message = "Admin ID must not exceed 20 characters")
    private String adminId;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    private Set<String> permissions;

    @PastOrPresent(message = "Assigned date cannot be in the future")
    private LocalDate assignedDate;
}
