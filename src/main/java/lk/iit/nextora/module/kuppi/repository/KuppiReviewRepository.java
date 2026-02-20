package lk.iit.nextora.module.kuppi.repository;

import lk.iit.nextora.module.kuppi.entity.KuppiReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for KuppiReview entity.
 */
@Repository
public interface KuppiReviewRepository extends JpaRepository<KuppiReview, Long> {

    @Query("SELECT r FROM KuppiReview r " +
           "LEFT JOIN FETCH r.session " +
           "LEFT JOIN FETCH r.reviewer " +
           "LEFT JOIN FETCH r.tutor " +
           "WHERE r.id = :id AND r.isDeleted = false")
    Optional<KuppiReview> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM KuppiReview r WHERE r.session.id = :sessionId AND r.isDeleted = false")
    Page<KuppiReview> findBySessionId(@Param("sessionId") Long sessionId, Pageable pageable);

    @Query("SELECT r FROM KuppiReview r WHERE r.reviewer.id = :reviewerId AND r.isDeleted = false")
    Page<KuppiReview> findByReviewerId(@Param("reviewerId") Long reviewerId, Pageable pageable);

    @Query("SELECT r FROM KuppiReview r WHERE r.tutor.id = :tutorId AND r.isDeleted = false")
    Page<KuppiReview> findByTutorId(@Param("tutorId") Long tutorId, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM KuppiReview r " +
           "WHERE r.session.id = :sessionId AND r.reviewer.id = :reviewerId AND r.isDeleted = false")
    boolean existsBySessionIdAndReviewerId(@Param("sessionId") Long sessionId, @Param("reviewerId") Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM KuppiReview r WHERE r.tutor.id = :tutorId AND r.isDeleted = false")
    Double getAverageRatingByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT COUNT(r) FROM KuppiReview r WHERE r.tutor.id = :tutorId AND r.isDeleted = false")
    Long countByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT r FROM KuppiReview r WHERE r.isDeleted = false ORDER BY r.createdAt DESC")
    Page<KuppiReview> findAllActive(Pageable pageable);
}
