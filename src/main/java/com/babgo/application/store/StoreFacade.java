package com.babgo.application.store;


import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreFacade {

    private final StoreService storeService;
    private final CategoryService categoryService;

    @Transactional
    public void createStore(StoreInfo.Create input) {
        Category category = categoryService.findByCategoryId(input.getCategoryId());
        Store store = Store.of(
                input.getStoreName(),
                input.getAddressLine(),
                input.getLatitude(),
                input.getLongitude(),
                input.getRegionCode(),
                input.getPhoneNumber(),
                input.getMinOrderAmount(),
                input.getOpeningHours(),
                input.getClosingHours(),
                category
        );
        storeService.create(store, "userName");
    }

    @Transactional
    public void updateStore(UUID storeId, StoreInfo.Update input) {
        Store store = storeService.findByStoreId(storeId);

        Map<String, Object> changes = new HashMap<>();
        if (input.getStoreName() != null) changes.put("storeName", input.getStoreName());
        if (input.getAddressLine() != null) changes.put("addressLine", input.getAddressLine());
        if (input.getLatitude() != null) changes.put("latitude", input.getLatitude());
        if (input.getLongitude() != null) changes.put("longitude", input.getLongitude());
        if (input.getRegionCode() != null) changes.put("regionCode", input.getRegionCode());
        if (input.getPhoneNumber() != null) changes.put("phoneNumber", input.getPhoneNumber());
        if (input.getMinOrderAmount() != null) changes.put("minOrderAmount", input.getMinOrderAmount());
        if (input.getOpeningHours() != null) changes.put("openingHours", input.getOpeningHours());
        if (input.getClosingHours() != null) changes.put("closingHours", input.getClosingHours());
        if (input.getCategoryId() != null) changes.put("categoryId", input.getCategoryId());

        storeService.update(store, changes, "userName");
    }

    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = storeService.findByStoreId(storeId);
        storeService.delete(store, "userName");
    }

    // 가게조회
    public StoreInfo.Detail getStoreById(UUID id) {
        Store store = storeService.getStoreById(id)
                .orElseThrow(() -> new RuntimeException("STORE_NOT_FOUND"));
        return StoreInfo.Detail.fromEntity(store);
    }

    // 가게요약
    public StoreInfo.Summary getStoreSummary(UUID id) {
        String summaryText = storeService.getStoreSummary(id);
        if (summaryText == null || summaryText.isBlank()) {
            summaryText = "요약이 존재하지 않습니다.";
        }
        return StoreInfo.Summary.of(summaryText);
    }
}
