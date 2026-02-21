package lk.iit.nextora.module.event.repository;

import lk.iit.nextora.common.enums.EventStatus;
import lk.iit.nextora.common.enums.EventType;
import lk.iit.nextora.module.event.entity.Event;
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
 * Repository for Event entity
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

       // ==================== Basic Queries ====================

       /**
        * Find event by ID (not deleted)
        */
       Optional<Event> findByIdAndIsDeletedFalse(Long id);

       /**
        * Find all events by creator
        */
       Page<Event> findByCreatedByIdAndIsDeletedFalse(Long createdById, Pageable pageable);

       /**
        * Find events by status
        */
       Page<Event> findByStatusAndIsDeletedFalse(EventStatus status, Pageable pageable);

       /**
        * Find all published events (visible to users)
        */
       @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.isDeleted = false")
       Page<Event> findPublishedEvents(Pageable pageable);

       // ==================== Search Queries ====================

       /**
        * Search events by title
        */
       @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> searchByTitle(@Param("keyword") String keyword,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       /**
        * Search events by title or description
        */
       @Query("SELECT e FROM Event e WHERE " +
                     "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                     "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> searchByTitleOrDescription(@Param("keyword") String keyword,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       /**
        * Search events by date range
        */
       @Query("SELECT e FROM Event e WHERE e.startAt BETWEEN :startDate AND :endDate " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> findByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       /**
        * Find upcoming events (start time is in the future)
        */
       @Query("SELECT e FROM Event e WHERE e.startAt > :now " +
                     "AND e.status = 'PUBLISHED' AND e.isDeleted = false ORDER BY e.startAt ASC")
       Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

       /**
        * Find ongoing events (started but not ended)
        */
       @Query("SELECT e FROM Event e WHERE e.startAt <= :now AND e.endAt > :now " +
                     "AND e.status = 'PUBLISHED' AND e.isDeleted = false")
       Page<Event> findOngoingEvents(@Param("now") LocalDateTime now, Pageable pageable);

       /**
        * Find past events (already ended)
        */
       @Query("SELECT e FROM Event e WHERE e.endAt < :now " +
                     "AND e.status IN ('PUBLISHED', 'COMPLETED') AND e.isDeleted = false ORDER BY e.endAt DESC")
       Page<Event> findPastEvents(@Param("now") LocalDateTime now, Pageable pageable);

       /**
        * Find events by event type
        */
       @Query("SELECT e FROM Event e WHERE e.eventType = :eventType " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> findByEventType(@Param("eventType") EventType eventType,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       /**
        * Search events by location
        */
       @Query("SELECT e FROM Event e WHERE LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> searchByLocation(@Param("location") String location,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       /**
        * Search events by creator name
        */
       @Query("SELECT e FROM Event e JOIN e.createdBy c WHERE " +
                     "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :creatorName, '%')) " +
                     "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :creatorName, '%'))) " +
                     "AND e.status IN :statuses AND e.isDeleted = false")
       Page<Event> searchByCreatorName(@Param("creatorName") String creatorName,
                     @Param("statuses") List<EventStatus> statuses,
                     Pageable pageable);

       // ==================== Eager Loading ====================

       /**
        * Find event by ID with creator eagerly loaded
        */
       @Query("SELECT e FROM Event e LEFT JOIN FETCH e.createdBy WHERE e.id = :id AND e.isDeleted = false")
       Optional<Event> findByIdWithCreator(@Param("id") Long id);

       // ==================== Count Queries ====================

       /**
        * Count events by creator
        */
       long countByCreatedByIdAndIsDeletedFalse(Long createdById);

       /**
        * Count events by status
        */
       long countByStatusAndIsDeletedFalse(EventStatus status);

       /**
        * Count all active events
        */
       long countByIsDeletedFalse();

       /**
        * Count events by creator and status
        */
       long countByCreatedByIdAndStatusAndIsDeletedFalse(Long createdById, EventStatus status);

       /**
        * Count upcoming events by creator
        */
       @Query("SELECT COUNT(e) FROM Event e WHERE e.createdBy.id = :createdById " +
                     "AND e.startAt > :now AND e.status = 'PUBLISHED' AND e.isDeleted = false")
       long countUpcomingByCreatedById(@Param("createdById") Long createdById, @Param("now") LocalDateTime now);

       /**
        * Sum view counts by creator
        */
       @Query("SELECT COALESCE(SUM(e.viewCount), 0) FROM Event e WHERE e.createdBy.id = :createdById AND e.isDeleted = false")
       long sumViewCountByCreatedById(@Param("createdById") Long createdById);

       /**
        * Sum all view counts
        */
       @Query("SELECT COALESCE(SUM(e.viewCount), 0) FROM Event e WHERE e.isDeleted = false")
       long sumAllViewCounts();

       /**
        * Find most viewed event by creator
        */
       @Query("SELECT e FROM Event e WHERE e.createdBy.id = :createdById AND e.isDeleted = false ORDER BY e.viewCount DESC")
       List<Event> findMostViewedByCreatedById(@Param("createdById") Long createdById, Pageable pageable);

       /**
        * Count distinct event creators
        */
       @Query("SELECT COUNT(DISTINCT e.createdBy.id) FROM Event e WHERE e.isDeleted = false")
       long countDistinctCreators();

       /**
        * Count events created within date range
        */
       @Query("SELECT COUNT(e) FROM Event e WHERE e.createdAt BETWEEN :start AND :end AND e.isDeleted = false")
       long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

       /**
        * Count all upcoming events (platform-wide)
        */
       @Query("SELECT COUNT(e) FROM Event e WHERE e.startAt > :now AND e.status = 'PUBLISHED' AND e.isDeleted = false")
       long countUpcomingEvents(@Param("now") LocalDateTime now);

       /**
        * Count distinct new creators this month
        */
       @Query("SELECT COUNT(DISTINCT e.createdBy.id) FROM Event e WHERE e.createdAt BETWEEN :start AND :end AND e.isDeleted = false")
       long countNewCreatorsThisMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
