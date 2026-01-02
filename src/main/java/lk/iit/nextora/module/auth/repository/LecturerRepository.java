package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.Lecturer;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends BaseUserRepository<Lecturer> {
    Optional<Lecturer> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
