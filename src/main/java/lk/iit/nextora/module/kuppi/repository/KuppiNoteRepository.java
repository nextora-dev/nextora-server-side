package lk.iit.nextora.module.kuppi.repository;

import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KuppiNoteRepository extends JpaRepository<KuppiNote, Long> {

    // Find by session
    Page<KuppiNote> findBySessionIdAndIsDeletedFalse(Long sessionId, Pageable pageable);

    List<KuppiNote> findBySessionIdAndIsDeletedFalse(Long sessionId);

    // Find by uploader
    Page<KuppiNote> findByUploadedByIdAndIsDeletedFalse(Long uploadedById, Pageable pageable);

    // Find all non-deleted notes
    Page<KuppiNote> findByIsDeletedFalse(Pageable pageable);

    // Find approved notes for a session
    @Query("SELECT n FROM KuppiNote n WHERE n.session.id = :sessionId AND n.isDeleted = false")
    List<KuppiNote> findApprovedNotesBySession(@Param("sessionId") Long sessionId);

    // Search notes by title
    @Query("SELECT n FROM KuppiNote n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND n.isDeleted = false")
    Page<KuppiNote> searchByTitle(@Param("keyword") String keyword,
                                   Pageable pageable);

    // Find by ID with eager loading
    @Query("SELECT n FROM KuppiNote n LEFT JOIN FETCH n.uploadedBy LEFT JOIN FETCH n.session WHERE n.id = :id AND n.isDeleted = false")
    Optional<KuppiNote> findByIdWithDetails(@Param("id") Long id);

    // Count by uploader
    long countByUploadedByIdAndIsDeletedFalse(Long uploadedById);

    // Analytics - total downloads for an uploader
    @Query("SELECT SUM(n.downloadCount) FROM KuppiNote n WHERE n.uploadedBy.id = :uploaderId AND n.isDeleted = false")
    Long getTotalDownloadsByUploader(@Param("uploaderId") Long uploaderId);

    // Analytics - total views for an uploader
    @Query("SELECT SUM(n.viewCount) FROM KuppiNote n WHERE n.uploadedBy.id = :uploaderId AND n.isDeleted = false")
    Long getTotalViewsByUploader(@Param("uploaderId") Long uploaderId);
}