package lk.iit.nextora.module.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String contactNumber;
    private boolean active = true;

    @ManyToOne
    private ItemCategory category;
}
