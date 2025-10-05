package com.babgo.domain.store;

import java.util.UUID;

public interface StoreRepository {
    Store save(Store store);

    Store findByStoreId(UUID storeId);
}
