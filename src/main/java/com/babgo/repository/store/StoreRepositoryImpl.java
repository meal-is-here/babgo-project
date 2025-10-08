package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public List<Store> saveAll(List<Store> stores) {
        return storeJpaRepository.saveAll(stores);
    }

    @Override
    public List<Store> findByStoreIdIn(List<UUID> storeIds) {
        return storeJpaRepository.findByStoreIdIn(storeIds);
    }

    @Override
    public Store findByStoreId(UUID storeId) {
        return storeJpaRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }
}
