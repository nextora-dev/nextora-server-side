package lk.iit.nextora.module.lostandfound.repository;

import lk.iit.nextora.module.lostandfound.entity.LostItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    // Find all active (not deleted)
    Page<LostItem> findByActiveTrue(Pageable pageable);

    // Find by category
    Page<LostItem> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Search by title
    @Query("SELECT l FROM LostItem l WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND l.active = true")
    Page<LostItem> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // Search by location
    @Query("SELECT l FROM LostItem l WHERE LOWER(l.location) LIKE LOWER(CONCAT('%', :keyword, '%')) AND l.active = true")
    Page<LostItem> searchByLocation(@Param("keyword") String keyword, Pageable pageable);

    // Find with category (eager fetch)
    @Query("SELECT l FROM LostItem l LEFT JOIN FETCH l.category WHERE l.id = :id AND l.active = true")
    Optional<LostItem> findByIdWithCategory(@Param("id") Long id);

    // Count active lost items
    long countByActiveTrue();

    // Analytics - count by category
    long countByCategoryIdAndActiveTrue(Long categoryId);
}
