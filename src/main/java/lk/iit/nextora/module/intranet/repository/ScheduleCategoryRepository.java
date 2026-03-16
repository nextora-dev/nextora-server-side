package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.ScheduleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleCategoryRepository extends JpaRepository<ScheduleCategory, Long> {

    List<ScheduleCategory> findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();

    @Query("SELECT DISTINCT sc FROM ScheduleCategory sc LEFT JOIN FETCH sc.events WHERE sc.categorySlug = :slug AND sc.isDeleted = false")
    Optional<ScheduleCategory> findByCategorySlugWithEvents(String slug);

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}
