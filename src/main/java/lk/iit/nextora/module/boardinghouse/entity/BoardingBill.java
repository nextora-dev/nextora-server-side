package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boarding_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardingBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardingBillType billType;

    @Column(nullable = false)
    private Boolean included; // true = included in rent, false = pay separately
}
