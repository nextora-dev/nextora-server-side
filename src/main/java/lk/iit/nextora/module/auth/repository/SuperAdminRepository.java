package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.SuperAdmin;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuperAdminRepository extends BaseUserRepository<SuperAdmin> {
    Optional<SuperAdmin> findBySuperAdminId(String superAdminId);
    boolean existsBySuperAdminId(String superAdminId);
}
