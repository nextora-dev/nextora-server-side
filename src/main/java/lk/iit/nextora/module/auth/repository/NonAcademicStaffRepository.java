package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.NonAcademicStaff;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NonAcademicStaffRepository extends BaseUserRepository<NonAcademicStaff> {
    Optional<NonAcademicStaff> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
}
