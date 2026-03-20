package lk.iit.nextora.module.boardinghouse.dto.response;

import lombok.*;

/**
 * Response DTO for Boarding House platform statistics.
 * Used by: Admin, Super Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouseStatsResponse {

    private Long totalListings;
    private Long availableListings;
    private Long unavailableListings;
    private Long totalViews;
    private Long maleOnlyListings;
    private Long femaleOnlyListings;
    private Long anyGenderListings;
}
