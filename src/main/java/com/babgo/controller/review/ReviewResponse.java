package com.babgo.controller.review;


import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewResponse {
    private UUID reviewId;
    private UUID storeId;
    private Long userId;
    private String content;
    private int rating;
    private LocalDateTime createdAt;

    public ReviewResponse(UUID reviewId, UUID storeId, Long userId, String content, int rating, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.storeId = storeId;
        this.userId = userId;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public UUID getReviewId() { return reviewId; }
    public UUID getStoreId() { return storeId; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}