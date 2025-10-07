package com.babgo.application.store;


import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
                input.getPhoneNumber(),
                input.getMinOrderAmount(),
                input.getOpeningHours(),
                input.getClosingHours(),
                category
        );
        store.markCreateBy("ownerName");
        storeService.create(store);
    }

    @Transactional
    public void updateStore(UUID storeId, StoreInfo.Update input) {
        Store store = storeService.findByStoreId(storeId);

        if (input.getStoreName() != null) {
            store.changeStoreName(input.getStoreName());
        }

        if (input.getAddressLine() != null) {
            store.changeAddressLine(input.getAddressLine());
        }

        if (input.getLatitude() != null || input.getLongitude() != null) {
            double lat = (input.getLatitude()  != null) ? input.getLatitude()  : store.getLatitude();
            double lon = (input.getLongitude() != null) ? input.getLongitude() : store.getLongitude();
            store.changeLocation(lat, lon);
        }

        if (input.getPhoneNumber() != null) {
            store.changePhoneNumber(input.getPhoneNumber());
        }

        if (input.getMinOrderAmount() != null) {
            store.changeMinOrderAmount(input.getMinOrderAmount());
        }

        if (input.getOpeningHours() != null || input.getClosingHours() != null) {
            LocalTime open  = (input.getOpeningHours()  != null) ? input.getOpeningHours()  : store.getOpeningHours();
            LocalTime close = (input.getClosingHours() != null) ? input.getClosingHours() : store.getClosingHours();
            store.changeBusinessHours(open, close);
        }

        if (input.getCategoryId() != null) {
            Category category = categoryService.findByCategoryId(input.getCategoryId());
            store.changeCategory(category);
        }

        store.markUpdatedBy("ownerName");
    }

    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = storeService.findByStoreId(storeId);
        if (store.isDeleted()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR); // 나중에 변경할 예정
        }
        store.markDeletedBy("ownerName");
    }
}
