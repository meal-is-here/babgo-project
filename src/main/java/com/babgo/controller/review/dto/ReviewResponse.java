package com.babgo.controller.review.dto;

import com.babgo.domain.review.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReviewResponse {

    private UUID reviewId;

    private int rating;

    private String content;

    private Long userId;

    private UUID storeId;

    public ReviewResponse(UUID reviewId, int rating, String content, Long userId, UUID storeId) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.content = content;
        this.userId = userId;
        this.storeId = storeId;
    }

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getRating(),
                review.getContent(),
                review.getUserId(),
                review.getStoreId()
        );
    }
}
