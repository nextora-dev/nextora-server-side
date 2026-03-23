package lk.iit.nextora.module.election.repository;

import lk.iit.nextora.common.enums.ElectionStatus;
import lk.iit.nextora.common.enums.ElectionType;
import lk.iit.nextora.module.election.entity.Election;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Election entity operations
 */
@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {

    Optional<Election> findByIdAndIsDeletedFalse(Long id);

    Page<Election> findByClubIdAndIsDeletedFalse(Long clubId, Pageable pageable);

    Page<Election> findByClubIdAndStatusAndIsDeletedFalse(Long clubId, ElectionStatus status, Pageable pageable);

    Page<Election> findByStatusAndIsDeletedFalse(ElectionStatus status, Pageable pageable);

    @Query("SELECT e FROM Election e WHERE e.club.id = :clubId AND e.status IN :statuses AND e.isDeleted = false")
    Page<Election> findByClubIdAndStatusIn(@Param("clubId") Long clubId,
                                           @Param("statuses") List<ElectionStatus> statuses,
                                           Pageable pageable);

    @Query("SELECT e FROM Election e WHERE e.status IN :statuses AND e.isDeleted = false ORDER BY e.votingStartTime ASC")
    Page<Election> findByStatusIn(@Param("statuses") List<ElectionStatus> statuses, Pageable pageable);

    // Find elections with voting currently open
    @Query("SELECT e FROM Election e WHERE e.status = 'VOTING_OPEN' AND e.votingStartTime <= :now " +
           "AND e.votingEndTime > :now AND e.isDeleted = false")
    List<Election> findActiveVotingElections(@Param("now") LocalDateTime now);

    // Find elections with nominations currently open
    @Query("SELECT e FROM Election e WHERE e.status = 'NOMINATION_OPEN' AND e.nominationStartTime <= :now " +
           "AND e.nominationEndTime > :now AND e.isDeleted = false")
    List<Election> findActiveNominationElections(@Param("now") LocalDateTime now);

    // Find upcoming elections for a club
    @Query("SELECT e FROM Election e WHERE e.club.id = :clubId AND e.votingStartTime > :now " +
           "AND e.status NOT IN ('CANCELLED', 'ARCHIVED') AND e.isDeleted = false ORDER BY e.votingStartTime ASC")
    Page<Election> findUpcomingByClub(@Param("clubId") Long clubId, @Param("now") LocalDateTime now, Pageable pageable);

    // Find all upcoming elections
    @Query("SELECT e FROM Election e WHERE e.votingStartTime > :now " +
           "AND e.status NOT IN ('CANCELLED', 'ARCHIVED') AND e.isDeleted = false ORDER BY e.votingStartTime ASC")
    Page<Election> findAllUpcoming(@Param("now") LocalDateTime now, Pageable pageable);

    // Search elections by keyword
    @Query("SELECT e FROM Election e WHERE e.isDeleted = false AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Election> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Find elections by type
    Page<Election> findByElectionTypeAndIsDeletedFalse(ElectionType electionType, Pageable pageable);

    // Find elections created by a user
    Page<Election> findByCreatedByIdAndIsDeletedFalse(Long createdById, Pageable pageable);

    // Find elections needing status update (nomination end time passed but still NOMINATION_OPEN)
    @Query("SELECT e FROM Election e WHERE e.status = 'NOMINATION_OPEN' AND e.nominationEndTime < :now AND e.isDeleted = false")
    List<Election> findElectionsNeedingNominationClose(@Param("now") LocalDateTime now);

    // Find elections needing voting to start
    @Query("SELECT e FROM Election e WHERE e.status = 'NOMINATION_CLOSED' AND e.votingStartTime <= :now AND e.isDeleted = false")
    List<Election> findElectionsNeedingVotingOpen(@Param("now") LocalDateTime now);

    // Find elections needing voting to close
    @Query("SELECT e FROM Election e WHERE e.status = 'VOTING_OPEN' AND e.votingEndTime < :now AND e.isDeleted = false")
    List<Election> findElectionsNeedingVotingClose(@Param("now") LocalDateTime now);

    // Count elections by club and status
    long countByClubIdAndStatusAndIsDeletedFalse(Long clubId, ElectionStatus status);

    // Count all elections by club
    long countByClubIdAndIsDeletedFalse(Long clubId);

    // Find elections where member can vote (active voting in clubs where user is member)
    @Query("SELECT e FROM Election e JOIN ClubMembership cm ON e.club.id = cm.club.id " +
           "WHERE cm.member.id = :memberId AND cm.status = 'ACTIVE' AND cm.isDeleted = false " +
           "AND e.status = 'VOTING_OPEN' AND e.isDeleted = false " +
           "AND (cm.expiryDate IS NULL OR cm.expiryDate > CURRENT_DATE)")
    Page<Election> findVotableElectionsForMember(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT e FROM Election e LEFT JOIN FETCH e.candidates LEFT JOIN FETCH e.club WHERE e.id = :id AND e.isDeleted = false")
    Optional<Election> findByIdWithDetails(@Param("id") Long id);

    // Count elections by status
    long countByStatus(ElectionStatus status);

    // Count elections by statuses
    @Query("SELECT COUNT(e) FROM Election e WHERE e.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<ElectionStatus> statuses);

    // Find elections by club and status
    List<Election> findByClubIdAndStatusAndIsDeletedFalse(Long clubId, ElectionStatus status);

    // Count all non-deleted elections
    @Query("SELECT COUNT(e) FROM Election e WHERE e.isDeleted = false")
    long countAllActive();

    // Count active elections across all clubs (nominations open OR voting open)
    @Query("SELECT COUNT(e) FROM Election e WHERE e.status IN ('NOMINATION_OPEN', 'VOTING_OPEN') AND e.isDeleted = false")
    long countActiveElections();
}
