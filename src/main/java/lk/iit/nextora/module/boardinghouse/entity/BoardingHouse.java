package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
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
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private Double monthlyRent;

    @Column(nullable = false)
    private Integer roomsAvailable;

    @Column(nullable = false)
    private Boolean isAvailable;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Long ownerId; // Later can be FK, now simple
}
