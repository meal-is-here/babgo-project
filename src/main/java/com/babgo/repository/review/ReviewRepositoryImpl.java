package com.babgo.repository.review;

import com.babgo.domain.review.Review;
import com.babgo.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public List<Review> findByStore_StoreId(UUID storeId) {
        // JpaRepository의 기본 메서드 활용
        return reviewJpaRepository.findAll()
                .stream()
                .filter(r -> r.getStore().getStoreId().equals(storeId))
                .toList();
    }

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Review findById(UUID reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
    }
}
