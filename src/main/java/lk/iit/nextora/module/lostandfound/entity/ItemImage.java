package lk.iit.nextora.module.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne
    private LostItem lostItem;

    @ManyToOne
    private FoundItem foundItem;
}
