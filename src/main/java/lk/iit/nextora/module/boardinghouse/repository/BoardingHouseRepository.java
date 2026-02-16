package lk.iit.nextora.module.boardinghouse.repository;

import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardingHouseRepository extends JpaRepository<BoardingHouse, Long> {

    Page<BoardingHouse> findByIsDeletedFalse(Pageable pageable);

    Page<BoardingHouse> findByLocation_CityIgnoreCaseAndIsDeletedFalse(String city, Pageable pageable);

    Page<BoardingHouse> findByMonthlyRentBetweenAndIsDeletedFalse(Double min, Double max, Pageable pageable);

    Page<BoardingHouse> findByLocation_CityIgnoreCaseAndMonthlyRentBetweenAndIsDeletedFalse(
            String city, Double min, Double max, Pageable pageable);

    Page<BoardingHouse> findByGenderTypeAndIsDeletedFalse(
            lk.iit.nextora.module.boardinghouse.entity.BoardingGenderType genderType, Pageable pageable);

    Page<BoardingHouse> findByWithFoodAndWithFurnitureAndIsDeletedFalse(
            Boolean withFood, Boolean withFurniture, Pageable pageable);
}
