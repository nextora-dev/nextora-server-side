package lk.iit.nextora.module.boardinghouse.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.GenderPreference;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a Boarding House listing.
 * Admin/Super Admin can create and manage listings.
 * Students can browse and filter listings.
 */
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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Monthly rent

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GenderPreference genderPreference = GenderPreference.ANY;

    @Column
    private Integer totalRooms;

    @Column
    @Builder.Default
    private Integer availableRooms = 0;

    @Column(nullable = false, length = 100)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(length = 100)
    private String contactEmail;

    // Amenities: WiFi, Parking, AC, Meals, etc.
    @ElementCollection
    @CollectionTable(name = "boarding_house_amenities",
            joinColumns = @JoinColumn(name = "boarding_house_id"))
    @Column(name = "amenity")
    @Builder.Default
    private Set<String> amenities = new HashSet<>();

    @OneToMany(mappedBy = "boardingHouse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<BoardingHouseImage> images = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by", nullable = false)
    private BaseUser postedBy;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void addImage(BoardingHouseImage image) {
        images.add(image);
        image.setBoardingHouse(this);
    }

    public void removeImage(BoardingHouseImage image) {
        images.remove(image);
        image.setBoardingHouse(null);
    }

    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(BoardingHouseImage::getIsPrimary)
                .findFirst()
                .map(BoardingHouseImage::getImageUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
    }
}
