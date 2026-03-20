package lk.iit.nextora.module.club.repository;

import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.club.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Club entity operations
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByClubCode(String clubCode);

    Optional<Club> findByClubCodeAndIsDeletedFalse(String clubCode);

    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.president LEFT JOIN FETCH c.advisor WHERE c.clubCode = :clubCode AND c.isDeleted = false")
    Optional<Club> findByClubCodeWithOfficers(@Param("clubCode") String clubCode);

    Optional<Club> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.president LEFT JOIN FETCH c.advisor WHERE c.id = :id AND c.isDeleted = false")
    Optional<Club> findByIdWithOfficers(@Param("id") Long id);

    boolean existsByClubCodeAndIsDeletedFalse(String clubCode);

    boolean existsByNameAndIsDeletedFalse(String name);

    Page<Club> findByIsDeletedFalseAndIsActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.isDeleted = false AND c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Club> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.faculty = :faculty AND c.isDeleted = false AND c.isActive = true")
    Page<Club> findByFaculty(@Param("faculty") FacultyType faculty, Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.president.id = :studentId AND c.isDeleted = false")
    List<Club> findByPresidentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(c) FROM Club c WHERE c.isDeleted = false AND c.isActive = true")
    long countActiveClubs();

    @Query("SELECT c FROM Club c WHERE c.isRegistrationOpen = true AND c.isDeleted = false AND c.isActive = true")
    Page<Club> findOpenForRegistration(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Club c WHERE c.isRegistrationOpen = true AND c.isDeleted = false AND c.isActive = true")
    long countOpenForRegistration();

    @Query("SELECT c FROM Club c WHERE c.isDeleted = false AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Club> findRecentClubs(Pageable pageable);

    @Query("SELECT c FROM Club c WHERE c.advisor.id = :advisorId AND c.isDeleted = false AND c.isActive = true")
    List<Club> findByAdvisorId(@Param("advisorId") Long advisorId);
}
