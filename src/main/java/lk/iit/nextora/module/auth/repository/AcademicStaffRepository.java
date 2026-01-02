package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.AcademicStaff;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicStaffRepository extends BaseUserRepository<AcademicStaff> {
    Optional<AcademicStaff> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
