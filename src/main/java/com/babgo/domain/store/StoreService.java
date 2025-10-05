package com.babgo.domain.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public Store create(Store store) {
        return storeRepository.save(store);
    }
}
