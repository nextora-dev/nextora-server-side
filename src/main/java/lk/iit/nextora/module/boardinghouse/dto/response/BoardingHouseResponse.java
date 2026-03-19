package lk.iit.nextora.module.boardinghouse.dto.response;

import lk.iit.nextora.common.enums.GenderPreference;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for Boarding House listing details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouseResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String formattedPrice; // e.g., "Rs. 15,000/month"

    // Location
    private String address;
    private String city;
    private String district;

    // Preferences
    private GenderPreference genderPreference;
    private String genderPreferenceDisplay;

    // Room info
    private Integer totalRooms;
    private Integer availableRooms;
    private Boolean isAvailable;

    // Contact info
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    // Additional info
    private Set<String> amenities;
    private List<BoardingHouseImageResponse> images;
    private String primaryImageUrl;
    private Long viewCount;

    // Posted by (Admin details)
    private Long postedById;
    private String postedByName;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
