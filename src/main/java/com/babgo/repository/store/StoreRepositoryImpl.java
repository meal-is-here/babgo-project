package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public Store save(Store store) {
        return storeJpaRepository.save(store);
    }
}
