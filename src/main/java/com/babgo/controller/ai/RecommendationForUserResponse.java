package com.babgo.controller.ai;

import com.babgo.domain.store.Store;

import java.util.List;

public record RecommendationForUserResponse(List<StoreInfo> stores) {

    public static RecommendationForUserResponse fromStores(List<Store> storeEntities) {
        List<StoreInfo> storeInfos = storeEntities.stream()
                .map(store -> new StoreInfo(store.getStoreId().toString(), store.getStoreName()))
                .toList();
        return new RecommendationForUserResponse(storeInfos);
    }

    public record StoreInfo(String storeId, String storeName) {}
}
