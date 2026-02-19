package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.common.enums.StudentRoleType;
import lk.iit.nextora.module.auth.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends BaseUserRepository<Student> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);

    /**
     * Count students who have a specific role type
     */
    @Query("SELECT COUNT(s) FROM Student s JOIN s.studentRoleTypes rt WHERE rt = :roleType")
    long countByStudentRoleTypesContaining(@Param("roleType") StudentRoleType roleType);
}
