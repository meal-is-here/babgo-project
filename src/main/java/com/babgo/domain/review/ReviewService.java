package com.babgo.domain.review;

import com.babgo.controller.review.dto.ReviewCreateRequest;
import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.controller.review.dto.ReviewUpdateRequest;
import com.babgo.domain.ai.review_analysis.ReviewAnalysisService;
import com.babgo.domain.common.ActionType;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ApplicationEventPublisher applicationEventPublisher;

    // create review
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isCompleted()) {
            throw new CustomException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        reviewRepository.findByOrderId(request.getOrderId())
                .ifPresent(r -> {
                    throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
                });

        Review review = Review.of(
                request.getRating(),
                request.getContent(),
                order.getUserId(),
                order.getStoreId(),
                order.getOrderId()
        );
        Review saved = reviewRepository.save(review);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    reviewAnalysisService.analyzeReviewAsync(saved.getReviewId());
                }
            });
        }

        applicationEventPublisher.publishEvent(new ReviewChangedEvent(review.getStoreId(), review.getRating(), 0, ActionType.CREATE));

        return ReviewResponse.from(saved);
    }

    // update review
    public Review updateReview(Long userId, UUID reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.REVIEW_FORBIDDEN);
        }

        // 수정전 평점
        int oldRating = review.getRating();

        // 수정 할 평점
        int newRating = request.getRating();

        review.updateReview(request.getRating(), request.getContent());

        applicationEventPublisher.publishEvent(new ReviewChangedEvent(review.getStoreId(), newRating, oldRating, ActionType.UPDATE));

        return review;
    }

    // delete review
    @Transactional
    public void deleteReview(Long userId, UUID reviewId) {
        Review review = reviewRepository.findByReviewIdAndReviewStatusNot(reviewId, ReviewStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_REVIEW_DELETE);
        }

        if (review.getReviewStatus() == ReviewStatus.DELETED) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_REVIEW);
        }

        int oldRating = review.getRating();

        review.updateStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);

        applicationEventPublisher.publishEvent(new ReviewChangedEvent(review.getStoreId(), 0, oldRating, ActionType.DELETE));
    }
}