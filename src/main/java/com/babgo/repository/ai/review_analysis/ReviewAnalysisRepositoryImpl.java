package com.babgo.repository.ai.review_analysis;

import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.ai.review_analysis.ReviewAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAnalysisRepositoryImpl implements ReviewAnalysisRepository {

    private final ReviewAnalysisJpaRepository reviewAnalysisJpaRepository;

    @Override
    @Transactional
    public ReviewAnalysis save(ReviewAnalysis reviewAnalysis) {
        return reviewAnalysisJpaRepository.save(reviewAnalysis);
    }

    @Override
    public List<ReviewAnalysis> findByReview_Store_StoreId(UUID storeId) {
        return reviewAnalysisJpaRepository.findByReview_Store_StoreId(storeId);
    }
}
