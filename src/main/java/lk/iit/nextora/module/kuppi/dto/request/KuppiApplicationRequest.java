package lk.iit.nextora.module.kuppi.dto.request;

import jakarta.validation.constraints.*;
import lk.iit.nextora.common.enums.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for submitting a Kuppi Student application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KuppiApplicationRequest {

    @NotBlank(message = "Motivation is required")
    @Size(min = 50, max = 1000, message = "Motivation must be between 50 and 1000 characters")
    private String motivation;

    @Size(max = 500, message = "Relevant experience cannot exceed 500 characters")
    private String relevantExperience;

    @NotEmpty(message = "At least one subject to teach is required")
    @Size(min = 1, max = 10, message = "You can select between 1 and 10 subjects")
    private Set<String> subjectsToTeach;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel preferredExperienceLevel;

    @Size(max = 500, message = "Availability cannot exceed 500 characters")
    private String availability;

    @NotNull(message = "Current GPA is required")
    @DecimalMin(value = "0.0", message = "GPA cannot be negative")
    @DecimalMax(value = "4.0", message = "GPA cannot exceed 4.0")
    private Double currentGpa;

    @NotBlank(message = "Current semester is required")
    @Size(max = 50, message = "Current semester cannot exceed 50 characters")
    private String currentSemester;
}

