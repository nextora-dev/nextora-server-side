package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.StudentComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentComplaintCategoryRepository extends JpaRepository<StudentComplaintCategory, Long> {

    List<StudentComplaintCategory> findAllByIsDeletedFalseAndIsActiveTrueOrderByCategoryNameAsc();

    Optional<StudentComplaintCategory> findByCategorySlugAndIsDeletedFalse(String categorySlug);

    boolean existsByCategorySlugAndIsDeletedFalse(String categorySlug);
}

