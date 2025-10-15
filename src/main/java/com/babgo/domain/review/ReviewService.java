package com.babgo.domain.review;

import com.babgo.domain.ai.ReviewAnalysisService;
import com.babgo.domain.store.Store;
import com.babgo.domain.user.User;
import com.babgo.repository.review.ReviewRepositoryImpl;
import com.babgo.repository.store.StoreRepositoryImpl;
import com.babgo.repository.user.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepositoryImpl reviewRepository;
    private final StoreRepositoryImpl storeRepository;
    private final UserRepositoryImpl userRepository;
    private final ReviewAnalysisService reviewAnalysisService;

    @Transactional
    public Review createReview(UUID storeId, Long userId, String content, int rating) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다: " + storeId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        Review review = new Review();
        review.setStore(store);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(content);

        Review saved = reviewRepository.save(review);


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // 비동기로 분석 시작 (쯕시 응답, 분석은 백그라운드)
                reviewAnalysisService.analyzeReviewAsync(saved.getReview_id());
            }
        });

        return saved;
    }


}
