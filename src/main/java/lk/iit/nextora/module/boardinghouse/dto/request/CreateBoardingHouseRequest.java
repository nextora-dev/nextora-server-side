package lk.iit.nextora.module.boardinghouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoardingHouseRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Double monthlyRent;

    @NotNull
    private Boolean keyMoneyRequired;

    @NotNull
    private String genderType; // MALE / FEMALE / MIXED

    @NotNull
    private Boolean withFood;

    @NotNull
    private Boolean withFurniture;

    // -------- Location --------
    @NotBlank
    private String city;

    @NotBlank
    private String address;

    private Double latitude;
    private Double longitude;

    // -------- Contact --------
    @NotBlank
    private String contactNumber1;

    private String contactNumber2;

    // -------- Bills --------
    @NotNull
    private Boolean waterBillIncluded;

    @NotNull
    private Boolean electricityBillIncluded;
}
