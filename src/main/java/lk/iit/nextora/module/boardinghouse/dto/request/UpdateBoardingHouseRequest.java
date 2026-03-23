package lk.iit.nextora.module.boardinghouse.dto.request;

import jakarta.validation.constraints.*;
import lk.iit.nextora.common.enums.GenderPreference;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Request DTO for updating a Boarding House listing.
 * All fields are optional - only provided fields will be updated.
 * Used by: Admin, Super Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoardingHouseRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @Size(max = 300, message = "Address must not exceed 300 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    private GenderPreference genderPreference;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @Min(value = 0, message = "Available rooms cannot be negative")
    private Integer availableRooms;

    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    private Set<String> amenities;

    private Boolean isAvailable;
}
