package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boarding_location")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;

    private String address;

    private Double latitude;

    private Double longitude;

}