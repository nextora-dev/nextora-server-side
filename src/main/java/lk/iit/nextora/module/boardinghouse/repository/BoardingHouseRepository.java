package lk.iit.nextora.module.boardinghouse.repository;

import lk.iit.nextora.module.boardinghouse.entity.BoardingHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardingHouseRepository extends JpaRepository<BoardingHouse, Long> {

    Optional<BoardingHouse> findByIdAndIsDeletedFalse(Long id);
}