package com.babgo.domain.store;

import com.babgo.domain.ai.store_summary.StoreSummaryService;
import com.babgo.domain.order.Order;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;
    private final StoreSummaryService storeSummaryService;

    public Store create(Store store, String userName) {
        store.markCreateBy(userName);
        return storeRepository.save(store);
    }

    public Store findByStoreId(UUID storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 가게를 찾을 수 없습니다."));
    }

    public void update(Store store, Map<String, Object> changes, String userName) {
        if (changes.containsKey("storeName")) {
            store.changeStoreName((String) changes.get("storeName"));
        }
        if (changes.containsKey("addressLine")) {
            store.changeAddressLine((String) changes.get("addressLine"));
        }

        Double lat = (Double) changes.get("latitude");
        Double lon = (Double) changes.get("longitude");
        if (lat != null || lon != null) {
            double newLat = (lat != null) ? lat : store.getLatitude();
            double newLon = (lon != null) ? lon : store.getLongitude();
            store.changeLocation(newLat, newLon);
        }

        if (changes.containsKey("regionCode")) {
            store.changeRegionCode((String) changes.get("regionCode"));
        }

        if (changes.containsKey("phoneNumber")) {
            store.changePhoneNumber((String) changes.get("phoneNumber"));
        }

        if (changes.containsKey("minOrderAmount")) {
            Integer amount = (Integer) changes.get("minOrderAmount");
            store.changeMinOrderAmount(amount);
        }

        LocalTime open = (LocalTime) changes.get("openingHours");
        LocalTime close = (LocalTime) changes.get("closingHours");
        if (open != null || close != null) {
            LocalTime mergedOpen = (open != null) ? open : store.getOpeningHours();
            LocalTime mergedClose = (close != null) ? close : store.getClosingHours();
            store.changeBusinessHours(mergedOpen, mergedClose);
        }

        if (changes.containsKey("categoryId")) {
            UUID categoryId = (UUID) changes.get("categoryId");
            Category category = categoryService.findByCategoryId(categoryId);
            store.changeCategory(category);
        }

        store.markUpdatedBy(userName);
    }

    public void delete(Store store, String userName) {
        store.markDeletedBy(userName);
    }

    // 세준
    public Optional<Store> getStoreById(UUID id) {
        return storeRepository.findById(id);
    }

    // 세준
    public String getStoreSummary(UUID id) {
        return storeSummaryService.generateSummaryReactive(id).block();
    }

    public void acceptFromConfirmed(Order order) {
        order.acceptFromConfirmed();
    }

    public void prepareFromAccepted(Order order) {
        order.prepareFromAccepted();
    }

    public void pickupFromPrepared(Order order) {
        order.pickupFromPrepared();
    }

    public void deliverFromPickedUp(Order order) {
        order.deliverFromPickedUp();
    }
}
