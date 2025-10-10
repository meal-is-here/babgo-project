package com.babgo.domain.ai.ReviewAnalysis;

import java.util.List;
import java.util.UUID;

public interface ReviewAnalysisRepository {
    List<ReviewAnalysis> findByReview_StoreId(UUID storeId);
}
