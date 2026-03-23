package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.StaffCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffCategoryRepository extends JpaRepository<StaffCategory, Long> {

    List<StaffCategory> findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();

    Optional<StaffCategory> findByCategorySlugAndIsDeletedFalse(String categorySlug);

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}

