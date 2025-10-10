package com.babgo.application.review;

import com.babgo.domain.review.Review;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ReviewInfo {
    private UUID reviewId;
    private UUID storeId;
    private Long userId;
    private String content;
    private int rating;
    private LocalDateTime createdAt;

    private ReviewInfo(UUID reviewId, UUID storeId, Long userId, String content, int rating, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.storeId = storeId;
        this.userId = userId;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static ReviewInfo from(Review review) {
        return new ReviewInfo(
                review.getReview_id(),
                review.getStore().getStoreId(),
                review.getUser().getUserId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
}
