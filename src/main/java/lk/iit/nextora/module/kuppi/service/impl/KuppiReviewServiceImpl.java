package lk.iit.nextora.module.kuppi.service.impl;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.common.enums.KuppiSessionStatus;
import lk.iit.nextora.common.exception.custom.BadRequestException;
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.common.exception.custom.UnauthorizedException;
import lk.iit.nextora.config.security.SecurityService;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.StudentRepository;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.request.TutorReviewResponseRequest;
import lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiReviewRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiReviewResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiReview;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import lk.iit.nextora.module.kuppi.repository.KuppiReviewRepository;
import lk.iit.nextora.module.kuppi.repository.KuppiSessionRepository;
import lk.iit.nextora.module.kuppi.service.KuppiReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of KuppiReviewService.
 *
 * Provides enterprise-grade review and rating functionality:
 * - Full CRUD operations with validation
 * - Automatic tutor rating recalculation
 * - Review moderation support
 * - Comprehensive analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KuppiReviewServiceImpl implements KuppiReviewService {

    private final KuppiReviewRepository reviewRepository;
    private final KuppiSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final SecurityService securityService;

    @Override
    @Transactional
    public KuppiReviewResponse createReview(CreateKuppiReviewRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Creating review for session {} by user {}", request.getSessionId(), currentUserId);

        KuppiSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", request.getSessionId()));

        if (session.getIsDeleted()) {
            throw new BadRequestException("Cannot review a deleted session");
        }
        if (session.getStatus() != KuppiSessionStatus.COMPLETED) {
            throw new BadRequestException("Can only review completed sessions");
        }
        if (reviewRepository.existsBySessionIdAndReviewerId(request.getSessionId(), currentUserId)) {
            throw new BadRequestException("You have already reviewed this session");
        }
        if (session.getHost().getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot review your own session");
        }

        Student reviewer = studentRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", currentUserId));

        KuppiReview review = KuppiReview.builder()
                .session(session)
                .reviewer(reviewer)
                .tutor(session.getHost())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        updateTutorRating(session.getHost().getId());

        log.info("Review created: {}", review.getId());
        return mapToResponse(review);
    }

    @Override
    @Transactional
    public KuppiReviewResponse updateReview(Long reviewId, UpdateKuppiReviewRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiReview review = findReviewById(reviewId);

        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        review = reviewRepository.save(review);
        updateTutorRating(review.getTutor().getId());

        log.info("Review updated: {}", reviewId);
        return mapToResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiReview review = findReviewById(reviewId);

        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        Long tutorId = review.getTutor().getId();
        review.softDelete();
        reviewRepository.save(review);
        updateTutorRating(tutorId);

        log.info("Review deleted: {}", reviewId);
    }

    @Override
    public KuppiReviewResponse getReviewById(Long reviewId) {
        return mapToResponse(findReviewById(reviewId));
    }

    @Override
    public PagedResponse<KuppiReviewResponse> getMyReviews(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        return toPagedResponse(reviewRepository.findByReviewerId(currentUserId, pageable));
    }

    @Override
    public PagedResponse<KuppiReviewResponse> getReviewsBySession(Long sessionId, Pageable pageable) {
        return toPagedResponse(reviewRepository.findBySessionId(sessionId, pageable));
    }

    @Override
    public PagedResponse<KuppiReviewResponse> getReviewsByTutor(Long tutorId, Pageable pageable) {
        return toPagedResponse(reviewRepository.findByTutorId(tutorId, pageable));
    }

    @Override
    @Transactional
    public KuppiReviewResponse addTutorResponse(Long reviewId, TutorReviewResponseRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        KuppiReview review = findReviewById(reviewId);

        if (!review.getTutor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the tutor can respond to this review");
        }
        if (review.getTutorResponse() != null && !review.getTutorResponse().isEmpty()) {
            throw new BadRequestException("You have already responded to this review");
        }

        review.setTutorResponse(request.getResponseText());
        review.setTutorResponseAt(LocalDateTime.now());
        review = reviewRepository.save(review);

        log.info("Tutor response added to review: {}", reviewId);
        return mapToResponse(review);
    }

    @Override
    public PagedResponse<KuppiReviewResponse> getReviewsForMyHostedSessions(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        return toPagedResponse(reviewRepository.findByTutorId(currentUserId, pageable));
    }

    @Override
    public PagedResponse<KuppiReviewResponse> getAllReviews(Pageable pageable) {
        return toPagedResponse(reviewRepository.findAllActive(pageable));
    }

    @Override
    @Transactional
    public void adminDeleteReview(Long reviewId) {
        KuppiReview review = findReviewById(reviewId);
        Long tutorId = review.getTutor().getId();
        review.softDelete();
        reviewRepository.save(review);
        updateTutorRating(tutorId);
        log.info("Admin deleted review: {}", reviewId);
    }

    // Private helpers

    private KuppiReview findReviewById(Long reviewId) {
        return reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
    }

    private void updateTutorRating(Long tutorId) {
        Double avgRating = reviewRepository.getAverageRatingByTutorId(tutorId);
        Student tutor = studentRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", "id", tutorId));
        tutor.setKuppiRating(avgRating != null ? avgRating : 0.0);
        studentRepository.save(tutor);
    }

    private PagedResponse<KuppiReviewResponse> toPagedResponse(Page<KuppiReview> page) {
        List<KuppiReviewResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<KuppiReviewResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private KuppiReviewResponse mapToResponse(KuppiReview review) {
        return KuppiReviewResponse.builder()
                .id(review.getId())
                .sessionId(review.getSession().getId())
                .sessionTitle(review.getSession().getTitle())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName())
                .tutorId(review.getTutor().getId())
                .tutorName(review.getTutor().getFirstName() + " " + review.getTutor().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .tutorResponse(review.getTutorResponse())
                .tutorResponseAt(review.getTutorResponseAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
