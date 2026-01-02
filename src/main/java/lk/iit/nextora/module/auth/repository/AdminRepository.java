package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.Admin;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends BaseUserRepository<Admin> {

    Optional<Admin> findByAdminId(String adminId);

    boolean existsByAdminId(String adminId);
}


