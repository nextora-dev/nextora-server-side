package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.Student;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends BaseUserRepository<Student> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
}
