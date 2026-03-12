package lk.iit.nextora.module.club.repository;

import lk.iit.nextora.module.club.entity.ClubAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubAnnouncementRepository extends JpaRepository<ClubAnnouncement, Long> {

    Optional<ClubAnnouncement> findByIdAndIsDeletedFalse(Long id);

    List<ClubAnnouncement> findByClubId(Long clubId);

    void deleteByClubId(Long clubId);

    Page<ClubAnnouncement> findByClubIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Long clubId, Pageable pageable);

    @Query("SELECT a FROM ClubAnnouncement a WHERE a.club.id = :clubId AND a.isMembersOnly = false AND a.isDeleted = false ORDER BY a.isPinned DESC, a.createdAt DESC")
    Page<ClubAnnouncement> findPublicByClubId(@Param("clubId") Long clubId, Pageable pageable);

    @Query("SELECT a FROM ClubAnnouncement a WHERE a.club.id = :clubId AND a.isPinned = true AND a.isDeleted = false ORDER BY a.createdAt DESC")
    Page<ClubAnnouncement> findPinnedByClubId(@Param("clubId") Long clubId, Pageable pageable);

    @Query("SELECT a FROM ClubAnnouncement a WHERE a.club.id = :clubId AND a.priority = :priority AND a.isDeleted = false ORDER BY a.createdAt DESC")
    Page<ClubAnnouncement> findByClubIdAndPriority(@Param("clubId") Long clubId, @Param("priority") ClubAnnouncement.AnnouncementPriority priority, Pageable pageable);

    @Query("SELECT a FROM ClubAnnouncement a WHERE a.isDeleted = false AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ClubAnnouncement> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(a) FROM ClubAnnouncement a WHERE a.club.id = :clubId AND a.isDeleted = false")
    long countByClubId(@Param("clubId") Long clubId);
}

