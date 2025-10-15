package com.babgo.domain.ai.review_analysis;

import java.util.List;
import java.util.UUID;

public interface ReviewAnalysisRepository {
    ReviewAnalysis save(ReviewAnalysis reviewAnalysis);
    List<ReviewAnalysis> findByReview_Store_StoreId(UUID storeId);
}
