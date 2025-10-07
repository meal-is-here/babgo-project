package com.babgo.domain.store;

import java.util.List;
import java.util.UUID;

public interface StoreRepository {
    Store save(Store store);

    List<Store> saveAll(List<Store> stores);
    List<Store> findByStoreIdIn(List<UUID> storeIds);
}
