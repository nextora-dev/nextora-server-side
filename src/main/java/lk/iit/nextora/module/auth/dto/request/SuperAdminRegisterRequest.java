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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SuperAdminRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Super Admin ID is required")
    @Size(max = 20, message = "Super Admin ID must not exceed 20 characters")
    private String superAdminId;

    @PastOrPresent(message = "Assigned date cannot be in the future")
    private LocalDate assignedDate;

    @Size(max = 100, message = "Access level must not exceed 100 characters")
    private String accessLevel;
}
