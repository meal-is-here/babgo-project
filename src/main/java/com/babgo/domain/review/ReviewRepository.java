package com.babgo.domain.ai.review_analysis;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository {
    List<Review> findByStoreId(UUID storeId);
}
