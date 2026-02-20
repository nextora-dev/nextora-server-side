package lk.iit.nextora.module.kuppi.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.request.TutorReviewResponseRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiReviewResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Kuppi Review operations.
 */
public interface KuppiReviewService {

    // Student CRUD operations
    KuppiReviewResponse createReview(CreateKuppiReviewRequest request);
    KuppiReviewResponse updateReview(Long reviewId, UpdateKuppiReviewRequest request);
    void deleteReview(Long reviewId);
    KuppiReviewResponse getReviewById(Long reviewId);
    PagedResponse<KuppiReviewResponse> getMyReviews(Pageable pageable);

    // Session reviews
    PagedResponse<KuppiReviewResponse> getReviewsBySession(Long sessionId, Pageable pageable);

    // Tutor reviews
    PagedResponse<KuppiReviewResponse> getReviewsByTutor(Long tutorId, Pageable pageable);

    // Tutor response
    KuppiReviewResponse addTutorResponse(Long reviewId, TutorReviewResponseRequest request);
    PagedResponse<KuppiReviewResponse> getReviewsForMyHostedSessions(Pageable pageable);

    // Admin operations
    PagedResponse<KuppiReviewResponse> getAllReviews(Pageable pageable);
    void adminDeleteReview(Long reviewId);
}
