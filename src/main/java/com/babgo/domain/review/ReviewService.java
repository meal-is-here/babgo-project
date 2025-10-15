package com.babgo.domain.review;

import com.babgo.controller.review.dto.ReviewCreateRequest;
import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.domain.ai.ReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewAnalysisService reviewAnalysisService;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    // create review
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isCompleted()) {
            throw new CustomException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        // 중복 리뷰 방지
        reviewRepository.findByOrderId(request.getOrderId())
                .ifPresent(r -> { throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS); });

        Review review = Review.of(
                request.getRating(),
                request.getContent(),
                order.getUserId(),
                order.getStoreId(),
                order.getOrderId()
        );
        Review saved = reviewRepository.save(review);


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // 비동기로 분석 시작 (쯕시 응답, 분석은 백그라운드)
                reviewAnalysisService.analyzeReviewAsync(saved.getReviewId());
            }
        });

        return ReviewResponse.from(saved);
    }
}
