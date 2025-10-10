package com.babgo.domain.review;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository {
    List<Review> findByStore_StoreId(UUID storeId);

    Review save(Review review);
    Review findById(UUID id);
}
