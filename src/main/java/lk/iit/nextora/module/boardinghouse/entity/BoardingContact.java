package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boarding_contact")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contactNumber1;

    private String contactNumber2;

}