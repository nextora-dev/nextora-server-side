package lk.iit.nextora.module.boardinghouse.repository;

import lk.iit.nextora.common.enums.GenderPreference;
import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface BoardingHouseRepository extends JpaRepository<BoardingHouse, Long> {

    // Find all active (not deleted) listings
    Page<BoardingHouse> findByIsDeletedFalseAndIsAvailableTrue(Pageable pageable);

    // Find all listings including unavailable (for admin)
    Page<BoardingHouse> findByIsDeletedFalse(Pageable pageable);

    // Find by poster (admin's own listings)
    Page<BoardingHouse> findByPostedByIdAndIsDeletedFalse(Long postedById, Pageable pageable);

    // Find by ID (not deleted)
    @Query("SELECT b FROM BoardingHouse b LEFT JOIN FETCH b.postedBy WHERE b.id = :id AND b.isDeleted = false")
    Optional<BoardingHouse> findByIdWithDetails(@Param("id") Long id);

    // Search by keyword (title, description, city)
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.city) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Filter by city
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "LOWER(b.city) LIKE LOWER(CONCAT('%', :city, '%')) " +
            "AND b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> findByCity(@Param("city") String city, Pageable pageable);

    // Filter by district
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "LOWER(b.district) LIKE LOWER(CONCAT('%', :district, '%')) " +
            "AND b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> findByDistrict(@Param("district") String district, Pageable pageable);

    // Filter by gender preference
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "(b.genderPreference = :gender OR b.genderPreference = 'ANY') " +
            "AND b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> findByGenderPreference(@Param("gender") GenderPreference gender, Pageable pageable);

    // Filter by price range
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "b.price BETWEEN :minPrice AND :maxPrice " +
            "AND b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         Pageable pageable);

    // Advanced filter: city + gender + price range
    @Query("SELECT b FROM BoardingHouse b WHERE " +
            "(:city IS NULL OR LOWER(b.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:district IS NULL OR LOWER(b.district) LIKE LOWER(CONCAT('%', :district, '%'))) AND " +
            "(:gender IS NULL OR b.genderPreference = :gender OR b.genderPreference = 'ANY') AND " +
            "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.price <= :maxPrice) AND " +
            "b.isDeleted = false AND b.isAvailable = true")
    Page<BoardingHouse> filterBoardingHouses(@Param("city") String city,
                                             @Param("district") String district,
                                             @Param("gender") GenderPreference gender,
                                             @Param("minPrice") BigDecimal minPrice,
                                             @Param("maxPrice") BigDecimal maxPrice,
                                             Pageable pageable);

    // Count for stats
    long countByIsDeletedFalse();
    long countByIsDeletedFalseAndIsAvailableTrue();

    // Analytics
    @Query("SELECT SUM(b.viewCount) FROM BoardingHouse b WHERE b.postedBy.id = :userId AND b.isDeleted = false")
    Long getTotalViewsByPoster(@Param("userId") Long userId);

    // Stats by gender preference
    @Query("SELECT SUM(b.viewCount) FROM BoardingHouse b WHERE b.isDeleted = false")
    Long getTotalViews();

    @Query("SELECT COUNT(b) FROM BoardingHouse b WHERE b.genderPreference = 'MALE' AND b.isDeleted = false")
    long countMaleOnlyListings();

    @Query("SELECT COUNT(b) FROM BoardingHouse b WHERE b.genderPreference = 'FEMALE' AND b.isDeleted = false")
    long countFemaleOnlyListings();

    @Query("SELECT COUNT(b) FROM BoardingHouse b WHERE b.genderPreference = 'ANY' AND b.isDeleted = false")
    long countAnyGenderListings();
}
