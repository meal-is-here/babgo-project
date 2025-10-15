package com.babgo.repository.ai.review_analysis;

import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.ai.review_analysis.ReviewAnalysisRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewAnalysisJpaRepository extends JpaRepository<ReviewAnalysis, UUID> {
    List<ReviewAnalysis> findByReview_Store_StoreId(UUID storeId);
}
