package com.babgo.domain.store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {
    Store save(Store store);

    Optional<Store> findByStoreId(UUID storeId);

    List<Store> saveAll(List<Store> stores);
    List<Store> findByStoreIdIn(List<UUID> storeIds);
}
