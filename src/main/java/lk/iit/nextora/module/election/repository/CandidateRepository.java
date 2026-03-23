package lk.iit.nextora.module.election.repository;

import lk.iit.nextora.common.enums.CandidateStatus;
import lk.iit.nextora.module.election.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Candidate entity operations
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByIdAndIsDeletedFalse(Long id);

    Optional<Candidate> findByElectionIdAndStudentIdAndIsDeletedFalse(Long electionId, Long studentId);

    boolean existsByElectionIdAndStudentIdAndIsDeletedFalse(Long electionId, Long studentId);

    Page<Candidate> findByElectionIdAndIsDeletedFalse(Long electionId, Pageable pageable);

    Page<Candidate> findByElectionIdAndStatusAndIsDeletedFalse(Long electionId, CandidateStatus status, Pageable pageable);

    List<Candidate> findByElectionIdAndStatusAndIsDeletedFalseOrderByDisplayOrderAsc(Long electionId, CandidateStatus status);

    @Query("SELECT c FROM Candidate c WHERE c.election.id = :electionId AND c.status = 'APPROVED' " +
           "AND c.isDeleted = false ORDER BY c.voteCount DESC")
    List<Candidate> findApprovedCandidatesOrderByVotes(@Param("electionId") Long electionId);

    @Query("SELECT c FROM Candidate c WHERE c.election.id = :electionId AND c.status = 'APPROVED' " +
           "AND c.isDeleted = false ORDER BY c.voteCount DESC")
    Page<Candidate> findApprovedCandidatesOrderByVotes(@Param("electionId") Long electionId, Pageable pageable);

    // Find candidates by student
    Page<Candidate> findByStudentIdAndIsDeletedFalse(Long studentId, Pageable pageable);

    // Find pending candidates for review
    @Query("SELECT c FROM Candidate c WHERE c.election.id = :electionId AND c.status = 'PENDING' AND c.isDeleted = false")
    Page<Candidate> findPendingCandidates(@Param("electionId") Long electionId, Pageable pageable);

    // Count candidates by election and status
    long countByElectionIdAndStatusAndIsDeletedFalse(Long electionId, CandidateStatus status);

    // Count all candidates by election
    long countByElectionIdAndIsDeletedFalse(Long electionId);

    // Get top N candidates by vote count
    @Query("SELECT c FROM Candidate c WHERE c.election.id = :electionId AND c.status = 'APPROVED' " +
           "AND c.isDeleted = false ORDER BY c.voteCount DESC")
    List<Candidate> findTopCandidates(@Param("electionId") Long electionId, Pageable pageable);

    // Get winners (top N candidates based on winnersCount)
    @Query(value = "SELECT c.* FROM candidates c WHERE c.election_id = :electionId AND c.status = 'APPROVED' " +
           "AND c.is_deleted = false ORDER BY c.vote_count DESC LIMIT :limit", nativeQuery = true)
    List<Candidate> findWinners(@Param("electionId") Long electionId, @Param("limit") int limit);

    // Increment vote count
    @Modifying
    @Query("UPDATE Candidate c SET c.voteCount = c.voteCount + 1 WHERE c.id = :candidateId")
    void incrementVoteCount(@Param("candidateId") Long candidateId);

    // Get candidate with election details
    @Query("SELECT c FROM Candidate c LEFT JOIN FETCH c.election LEFT JOIN FETCH c.student WHERE c.id = :id AND c.isDeleted = false")
    Optional<Candidate> findByIdWithDetails(@Param("id") Long id);

    // Get all candidates with student details for an election
    @Query("SELECT c FROM Candidate c LEFT JOIN FETCH c.student WHERE c.election.id = :electionId " +
           "AND c.status = 'APPROVED' AND c.isDeleted = false ORDER BY c.displayOrder ASC")
    List<Candidate> findApprovedWithStudentDetails(@Param("electionId") Long electionId);

    // Find all candidates for an election (including deleted for admin view)
    Page<Candidate> findByElectionId(Long electionId, Pageable pageable);

    // Find all candidates for an election (not deleted)
    List<Candidate> findByElectionIdAndIsDeletedFalse(Long electionId);

    // Delete all candidates by election
    @Modifying
    @Query("DELETE FROM Candidate c WHERE c.election.id = :electionId")
    void deleteByElectionId(@Param("electionId") Long electionId);

    // Count by status
    long countByStatus(CandidateStatus status);
}
