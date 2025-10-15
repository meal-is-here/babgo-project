package com.babgo.domain.review;

import com.babgo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private UUID reviewId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID orderId;

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
}