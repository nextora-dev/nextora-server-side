package lk.iit.nextora.module.club.repository;

import lk.iit.nextora.module.club.entity.ClubActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClubActivityLogRepository extends JpaRepository<ClubActivityLog, Long> {

    Page<ClubActivityLog> findByClubIdOrderByCreatedAtDesc(Long clubId, Pageable pageable);

    @Query("SELECT a FROM ClubActivityLog a WHERE a.club.id = :clubId AND a.activityType = :type ORDER BY a.createdAt DESC")
    Page<ClubActivityLog> findByClubIdAndType(@Param("clubId") Long clubId, @Param("type") ClubActivityLog.ActivityType type, Pageable pageable);

    @Query("SELECT a FROM ClubActivityLog a WHERE a.club.id = :clubId AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt DESC")
    List<ClubActivityLog> findByClubIdAndDateRange(@Param("clubId") Long clubId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM ClubActivityLog a WHERE a.performedByUserId = :userId ORDER BY a.createdAt DESC")
    Page<ClubActivityLog> findByPerformedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM ClubActivityLog a ORDER BY a.createdAt DESC")
    List<ClubActivityLog> findRecentActivity(Pageable pageable);

    void deleteByClubId(Long clubId);
}

