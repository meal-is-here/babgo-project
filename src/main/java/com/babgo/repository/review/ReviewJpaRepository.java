package com.babgo.repository.review;

import com.babgo.domain.review.Review;
import com.babgo.domain.review.ReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
}
