package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.MitigationForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MitigationFormRepository extends JpaRepository<MitigationForm, Long> {

    List<MitigationForm> findAllByIsDeletedFalseAndIsActiveTrueOrderByFormNameAsc();

    Optional<MitigationForm> findByFormSlugAndIsDeletedFalse(String formSlug);

    boolean existsByFormSlugAndIsDeletedFalse(String formSlug);
}

