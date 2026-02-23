package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "boarding_house")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouse extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double monthlyRent;

    @Column(nullable = false)
    private Boolean keyMoneyRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardingGenderType genderType;

    @Column(nullable = false)
    private Boolean withFood;

    @Column(nullable = false)
    private Boolean withFurniture;

    //  Location entity
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private BoardingLocation location;

    //  Contact entity
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_id")
    private BoardingContact contact;

    //  Water Bill entity
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "water_bill_id")
    private BoardingBill waterBill;

    //  Electricity Bill entity
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "electricity_bill_id")
    private BoardingBill electricityBill;

}