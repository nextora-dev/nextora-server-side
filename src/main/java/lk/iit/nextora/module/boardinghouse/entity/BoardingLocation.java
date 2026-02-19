package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boarding_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardingLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 255)
    private String address;

    private Double latitude;
    private Double longitude;
}
