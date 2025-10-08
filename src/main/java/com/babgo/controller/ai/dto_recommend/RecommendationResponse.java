package com.babgo.controller.ai.dto_recommend;

import com.babgo.domain.store.Store;

import java.util.List;

public record RecommendationResponse(List<StoreInfo> stores) {

    public static RecommendationResponse from(List<Store> storeEntities) {
        List<StoreInfo> storeInfos = storeEntities.stream()
                .map(store -> new StoreInfo(store.getStoreId().toString(), store.getStoreName()))
                .toList();
        return new RecommendationResponse(storeInfos);
    }

    public record StoreInfo(String storeId, String storeName) {}
}
