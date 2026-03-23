package lk.iit.nextora.module.boardinghouse.dto.request;

import lk.iit.nextora.common.enums.GenderPreference;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for filtering Boarding Houses.
 * All fields are optional - used as query parameters.
 * Used by: All students (browsing)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouseFilterRequest {

    private String city;
    private String district;
    private GenderPreference genderPreference;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
