package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.StudentPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPolicyRepository extends JpaRepository<StudentPolicy, Long> {

    List<StudentPolicy> findAllByIsDeletedFalseAndIsActiveTrueOrderByPolicyNameAsc();

    Optional<StudentPolicy> findByPolicySlugAndIsDeletedFalse(String policySlug);

    boolean existsByPolicySlugAndIsDeletedFalse(String policySlug);
}

