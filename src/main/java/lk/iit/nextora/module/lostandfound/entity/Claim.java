package lk.iit.nextora.module.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LostItem lostItem;

    @ManyToOne
    private FoundItem foundItem;

    private String status;
}
