package lk.iit.nextora.module.boardinghouse.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouseResponse {

    private Long id;
    private String name;
    private String description;
    private Double monthlyRent;
    private Boolean keyMoneyRequired;
    private String genderType;
    private Boolean withFood;
    private Boolean withFurniture;

    // Location
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;

    // Contact
    private String contactNumber1;
    private String contactNumber2;

    // Bills
    private Boolean waterBillIncluded;
    private Boolean electricityBillIncluded;
}
