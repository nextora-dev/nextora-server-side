package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.InfoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfoCategoryRepository extends JpaRepository<InfoCategory, Long> {

    List<InfoCategory> findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();

    Optional<InfoCategory> findByCategorySlugAndIsDeletedFalse(String categorySlug);

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}

