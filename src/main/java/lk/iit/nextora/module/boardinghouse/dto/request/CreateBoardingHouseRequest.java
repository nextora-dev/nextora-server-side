package lk.iit.nextora.module.boardinghouse.dto.request;

import jakarta.validation.constraints.*;
import lk.iit.nextora.common.enums.GenderPreference;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Request DTO for creating a new Boarding House listing.
 * Used by: Admin, Super Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoardingHouseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @NotBlank(message = "Address is required")
    @Size(max = 300, message = "Address must not exceed 300 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    private GenderPreference genderPreference = GenderPreference.ANY;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @Min(value = 0, message = "Available rooms cannot be negative")
    private Integer availableRooms;

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Builder.Default
    private Set<String> amenities = new HashSet<>();

    private Boolean isAvailable = true;
}
