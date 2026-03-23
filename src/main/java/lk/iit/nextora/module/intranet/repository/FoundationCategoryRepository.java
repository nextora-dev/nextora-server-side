package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.FoundationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoundationCategoryRepository extends JpaRepository<FoundationCategory, Long> {

    List<FoundationCategory> findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();

    Optional<FoundationCategory> findByCategorySlugAndIsDeletedFalse(String categorySlug);

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}

