package lk.iit.nextora.module.club.repository;

import lk.iit.nextora.common.enums.ClubMembershipStatus;
import lk.iit.nextora.module.club.entity.ClubMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ClubMembership entity operations
 */
@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    Optional<ClubMembership> findByClubIdAndMemberIdAndIsDeletedFalse(Long clubId, Long memberId);

    Optional<ClubMembership> findByMembershipNumberAndIsDeletedFalse(String membershipNumber);

    boolean existsByClubIdAndMemberIdAndIsDeletedFalse(Long clubId, Long memberId);

    Page<ClubMembership> findByClubIdAndIsDeletedFalse(Long clubId, Pageable pageable);

    Page<ClubMembership> findByMemberIdAndIsDeletedFalse(Long memberId, Pageable pageable);

    Page<ClubMembership> findByClubIdAndStatusAndIsDeletedFalse(Long clubId, ClubMembershipStatus status, Pageable pageable);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.club.id = :clubId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    Page<ClubMembership> findActiveMembers(@Param("clubId") Long clubId,
                                           @Param("currentDate") LocalDate currentDate,
                                           Pageable pageable);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.club.id = :clubId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    List<ClubMembership> findAllActiveMembers(@Param("clubId") Long clubId,
                                              @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(cm) FROM ClubMembership cm WHERE cm.club.id = :clubId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    long countActiveMembers(@Param("clubId") Long clubId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.club.id = :clubId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND cm.joinDate <= :eligibilityDate " +
           "AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    List<ClubMembership> findEligibleForNomination(@Param("clubId") Long clubId,
                                                   @Param("eligibilityDate") LocalDate eligibilityDate,
                                                   @Param("currentDate") LocalDate currentDate);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.member.id = :memberId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    List<ClubMembership> findActiveMembershipsByStudent(@Param("memberId") Long memberId,
                                                        @Param("currentDate") LocalDate currentDate);

    @Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END FROM ClubMembership cm " +
           "WHERE cm.club.id = :clubId AND cm.member.id = :memberId AND cm.status = 'ACTIVE' " +
           "AND cm.isDeleted = false AND (cm.expiryDate IS NULL OR cm.expiryDate > :currentDate)")
    boolean isActiveMember(@Param("clubId") Long clubId,
                          @Param("memberId") Long memberId,
                          @Param("currentDate") LocalDate currentDate);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.club.id = :clubId AND cm.status = 'PENDING' AND cm.isDeleted = false")
    Page<ClubMembership> findPendingApplications(@Param("clubId") Long clubId, Pageable pageable);

    @Query("SELECT cm FROM ClubMembership cm WHERE cm.expiryDate <= :expiryDate AND cm.status = 'ACTIVE' AND cm.isDeleted = false")
    List<ClubMembership> findExpiringMemberships(@Param("expiryDate") LocalDate expiryDate);
}
