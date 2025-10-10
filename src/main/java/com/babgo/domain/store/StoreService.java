package com.babgo.domain.store;

import com.babgo.domain.ai.StoreSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreSummaryService storeSummaryService;

    public Store create(Store store) {
        return storeRepository.save(store);
    }

    // 세준
    public Optional<Store> getStoreById(UUID id) {
        return storeRepository.findById(id);
    }

    // 세준
    public String getStoreSummary(UUID id) {
        return String.valueOf(storeSummaryService.generateSummaryReactive(id));
    }
}
