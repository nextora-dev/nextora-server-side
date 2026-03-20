package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    List<Program> findAllByProgramLevelAndIsDeletedFalseAndIsActiveTrueOrderByProgramNameAsc(String programLevel);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.modules WHERE p.programSlug = :slug AND p.programLevel = :level AND p.isDeleted = false")
    Optional<Program> findByProgramSlugAndProgramLevelWithModules(String slug, String level);

    boolean existsByProgramSlugAndProgramLevelAndIsDeletedFalse(String programSlug, String programLevel);
}