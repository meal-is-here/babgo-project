package com.babgo.domain.review;

import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.store.Store;
import com.babgo.domain.user.User;
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
public class Review {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID review_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String content;
    private int rating; // 1~5

    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name="updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name="deleted_at")
    private LocalDateTime deletedAt = LocalDateTime.now();

    @Column(name="deleted_by")
    private String deletedBy;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewAnalysis analysis;
}

