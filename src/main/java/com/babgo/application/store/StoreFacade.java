package com.babgo.application.store;


import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        store.markOwnerName("ownerName");
        storeService.create(store);
    }
}
