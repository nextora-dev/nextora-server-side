package lk.iit.nextora.module.event.repository;

import lk.iit.nextora.module.event.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.isCancelled = false")
    long countActiveByEventId(@Param("eventId") Long eventId);

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM EventRegistration r " +
            "WHERE r.event.id = :eventId AND r.user.id = :userId AND r.isCancelled = false")
    boolean isUserRegistered(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("SELECT r FROM EventRegistration r JOIN FETCH r.event JOIN FETCH r.user " +
            "WHERE r.user.id = :userId AND r.isCancelled = false")
    Page<EventRegistration> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM EventRegistration r JOIN FETCH r.event JOIN FETCH r.user " +
            "WHERE r.event.id = :eventId AND r.isCancelled = false")
    Page<EventRegistration> findActiveByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.isCancelled = false")
    long countAllActive();
}
