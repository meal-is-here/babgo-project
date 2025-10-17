package com.babgo.application.store;

import com.babgo.domain.store.Store;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreCreatedEvent(
    UUID storeId,
    String storeName,
    UUID categoryId,
    String categoryName,
    String regionCode,
    double minOrderAmount,   // 임시 평점 역할
    String storeStatus,
    double latitude,
    double longitude,
    LocalDateTime createdAt
) {

    public static StoreCreatedEvent from(Store store) {
        return new StoreCreatedEvent(
            store.getStoreId(),
            store.getStoreName(),
            store.getCategory().getCategoryId(),
            store.getCategory().getCategoryName(),
            store.getRegionCode(),
            store.getMinOrderAmount(), // 임시 평점
            store.getStoreStatus().toString(),
            store.getLatitude(),
            store.getLongitude(),
            store.getCreatedAt()
        );
    }
}