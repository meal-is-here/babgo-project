package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public Store save(Store store) {
        return storeJpaRepository.save(store);
    }

    @Override
    public Optional<Store> findById(UUID storeId) {
        return storeJpaRepository.findById(storeId);
    }

    @Override
    public List<Store> saveAll(List<Store> stores) {
        return storeJpaRepository.saveAll(stores);
    }

    @Override
    public List<Store> findByStoreIdIn(List<UUID> storeIds) {
        return storeJpaRepository.findByStoreIdIn(storeIds);
    }

    @Override
    public List<Store> findAll() {
        return storeJpaRepository.findAll();
    }

    @Override
    public Optional<Store> findByStoreId(UUID storeId) {
        return storeJpaRepository.findById(storeId);
    }
}
