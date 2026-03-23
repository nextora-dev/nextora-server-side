package lk.iit.nextora.module.election.repository;

import lk.iit.nextora.module.election.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Vote entity operations
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Check if user has already voted in an election
    boolean existsByElectionIdAndVoterIdAndIsDeletedFalse(Long electionId, Long voterId);

    // Check by vote hash (for anonymous voting verification)
    boolean existsByElectionIdAndVoteHashAndIsDeletedFalse(Long electionId, String voteHash);

    // Find vote by verification token
    Optional<Vote> findByVerificationTokenAndIsDeletedFalse(String verificationToken);

    // Find votes by election
    Page<Vote> findByElectionIdAndIsDeletedFalse(Long electionId, Pageable pageable);

    // Find votes by candidate
    Page<Vote> findByCandidateIdAndIsDeletedFalse(Long candidateId, Pageable pageable);

    // Find votes by voter (for non-anonymous elections or audit)
    Page<Vote> findByVoterIdAndIsDeletedFalse(Long voterId, Pageable pageable);

    // Count votes by election
    long countByElectionIdAndIsDeletedFalse(Long electionId);

    // Count votes by candidate
    long countByCandidateIdAndIsDeletedFalse(Long candidateId);

    // Count votes by election and candidate
    @Query("SELECT c.id, c.student.firstName, c.student.lastName, COUNT(v) as voteCount " +
           "FROM Vote v JOIN v.candidate c WHERE v.election.id = :electionId AND v.isDeleted = false " +
           "GROUP BY c.id, c.student.firstName, c.student.lastName ORDER BY voteCount DESC")
    List<Object[]> getVoteCountsByCandidate(@Param("electionId") Long electionId);

    // Get voting statistics by time intervals (PostgreSQL compatible)
    @Query(value = "SELECT DATE(voted_at) as vote_date, COUNT(*) as vote_count " +
           "FROM votes WHERE election_id = :electionId AND is_deleted = false " +
           "GROUP BY DATE(voted_at) ORDER BY vote_date", nativeQuery = true)
    List<Object[]> getVotingStatsByDate(@Param("electionId") Long electionId);

    // Get hourly voting distribution (PostgreSQL compatible)
    @Query(value = "SELECT EXTRACT(HOUR FROM voted_at) as vote_hour, COUNT(*) as vote_count " +
           "FROM votes WHERE election_id = :electionId AND is_deleted = false " +
           "GROUP BY EXTRACT(HOUR FROM voted_at) ORDER BY vote_hour", nativeQuery = true)
    List<Object[]> getVotingStatsByHour(@Param("electionId") Long electionId);

    // Find votes in time range
    @Query("SELECT v FROM Vote v WHERE v.election.id = :electionId " +
           "AND v.votedAt BETWEEN :startTime AND :endTime AND v.isDeleted = false")
    List<Vote> findVotesInTimeRange(@Param("electionId") Long electionId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    // Get recent votes for real-time updates
    @Query("SELECT v FROM Vote v WHERE v.election.id = :electionId " +
           "AND v.votedAt > :since AND v.isDeleted = false ORDER BY v.votedAt DESC")
    List<Vote> findRecentVotes(@Param("electionId") Long electionId, @Param("since") LocalDateTime since);

    // Verify if vote exists (for voter receipt verification)
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vote v " +
           "WHERE v.verificationToken = :token AND v.election.id = :electionId AND v.isDeleted = false")
    boolean verifyVote(@Param("electionId") Long electionId, @Param("token") String token);

    // Get participation rate (requires total eligible voters count separately)
    @Query("SELECT COUNT(DISTINCT v.voter.id) FROM Vote v WHERE v.election.id = :electionId AND v.isDeleted = false")
    long countUniqueVoters(@Param("electionId") Long electionId);

    // Find duplicate votes (should always be empty in production)
    @Query("SELECT v.voter.id, COUNT(v) FROM Vote v WHERE v.election.id = :electionId AND v.isDeleted = false " +
           "GROUP BY v.voter.id HAVING COUNT(v) > 1")
    List<Object[]> findDuplicateVotes(@Param("electionId") Long electionId);

    // Delete all votes by election (for admin reset)
    @Modifying
    @Query("DELETE FROM Vote v WHERE v.election.id = :electionId")
    void deleteByElectionId(@Param("electionId") Long electionId);

    // Count votes by club (through election relationship)
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.election.club.id = :clubId AND v.isDeleted = false")
    long countByElection_ClubIdAndIsDeletedFalse(@Param("clubId") Long clubId);
}
