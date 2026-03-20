package lk.iit.nextora.module.lostandfound.repository;

import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    Page<FoundItem> findByActiveTrue(Pageable pageable);

    Page<FoundItem> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    @Query("SELECT f FROM FoundItem f " +
            "WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND f.active = true")
    Page<FoundItem> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT f FROM FoundItem f " +
            "LEFT JOIN FETCH f.category " +
            "WHERE f.id = :id AND f.active = true")
    Optional<FoundItem> findByIdWithCategory(@Param("id") Long id);

    Page<FoundItem> findByReportedByAndActiveTrue(Long reportedBy, Pageable pageable);

    long countByActiveTrue();
}
