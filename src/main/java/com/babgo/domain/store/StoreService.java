package com.babgo.domain.store;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public Store create(Store store) {
        return storeRepository.save(store);
    }

    public Store findByStoreId(UUID storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 가게를 찾을 수 없습니다."));
    }
}
