package lk.iit.nextora.module.boardinghouse.repository;

import lk.iit.nextora.module.boardinghouse.entity.BoardingHouseImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardingHouseImageRepository extends JpaRepository<BoardingHouseImage, Long> {

    List<BoardingHouseImage> findByBoardingHouseIdOrderByDisplayOrderAsc(Long boardingHouseId);

    Optional<BoardingHouseImage> findByIdAndBoardingHouseId(Long id, Long boardingHouseId);

    @Query("SELECT COUNT(i) FROM BoardingHouseImage i WHERE i.boardingHouse.id = :boardingHouseId")
    int countByBoardingHouseId(@Param("boardingHouseId") Long boardingHouseId);

    @Query("SELECT MAX(i.displayOrder) FROM BoardingHouseImage i WHERE i.boardingHouse.id = :boardingHouseId")
    Integer findMaxDisplayOrder(@Param("boardingHouseId") Long boardingHouseId);
}
