package com.babgo.application.review;

import com.babgo.domain.review.Review;
import com.babgo.domain.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewFacade {

    private final ReviewService reviewService;

    @Transactional
    public ReviewInfo createReview(UUID storeId, Long userId, String content, int rating) {
        Review review = reviewService.createReview(storeId, userId, content, rating);
        return ReviewInfo.from(review);
    }
}
