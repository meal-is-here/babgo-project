package com.babgo.domain.review;

import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.store.Store;
import com.babgo.domain.user.User;
import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "p_reviews")
@NoArgsConstructor
public class Review extends BaseTimeEntity {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name="deleted_at")
    private LocalDateTime deletedAt = LocalDateTime.now();

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="order_id",nullable = false)
    private UUID orderId;

    @Column(name="store_id",nullable = false)
    private UUID storeId;

    @Column(name="deleted_by")
    private String deletedBy;


    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewAnalysis analysis;

    public Review(int rating, String content, Long userId, UUID storeId, UUID orderId) {
        this.rating = rating;
        this.content = content;
        this.userId = userId;
        this.storeId = storeId;
        this.orderId = orderId;
    }

    public static Review of(int rating, String content, Long userId, UUID storeId, UUID orderId) {
        return new Review(rating, content, userId, storeId, orderId);
    }

    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus;

    public void updateStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}