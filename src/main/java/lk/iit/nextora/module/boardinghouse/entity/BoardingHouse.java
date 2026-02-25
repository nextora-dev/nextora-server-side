package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.Gender;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "boarding_houses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouse extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, length = 20)
    private String contactNumber1;

    @Column(length = 20)
    private String contactNumber2;

    @Column(nullable = false)
    private Boolean keyMoneyRequired;

    @Column(nullable = false)
    private Boolean waterBillIncluded;

    @Column(nullable = false)
    private Boolean electricityBillIncluded;

    @Column(nullable = false)
    private Boolean foodIncluded;

    @Column(nullable = false)
    private Boolean furnitureIncluded;

    private Double latitude;

    private Double longitude;
}