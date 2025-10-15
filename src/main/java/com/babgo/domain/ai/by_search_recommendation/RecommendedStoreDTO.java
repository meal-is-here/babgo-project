package com.babgo.domain.ai.by_search_recommendation;

import com.babgo.domain.store.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendedStoreDTO {
    private Store store;
    private String reason; // AI가 생성한 추천 이유
}