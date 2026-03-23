package lk.iit.nextora.module.intranet.repository;

import lk.iit.nextora.module.intranet.entity.AcademicCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicCalendarRepository extends JpaRepository<AcademicCalendar, Long> {

    List<AcademicCalendar> findAllByIsDeletedFalseAndIsActiveTrueOrderByUniversityNameAsc();

    @Query("SELECT DISTINCT ac FROM AcademicCalendar ac LEFT JOIN FETCH ac.events WHERE ac.universitySlug = :slug AND ac.isDeleted = false")
    Optional<AcademicCalendar> findByUniversitySlugWithEvents(String slug);

    boolean existsByUniversitySlugAndIsDeletedFalse(String universitySlug);
}

