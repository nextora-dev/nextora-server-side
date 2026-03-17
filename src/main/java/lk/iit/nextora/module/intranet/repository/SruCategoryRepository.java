package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.SruCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SruCategoryRepository extends JpaRepository<SruCategory, Long> {

    @Query("SELECT s FROM SruCategory s WHERE s.categorySlug = '_root' AND s.isDeleted = false")
    Optional<SruCategory> findRootCategory();

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}

