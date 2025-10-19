package com.babgo.domain.ai.by_search_recommendation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedStoreDTO {
    private UUID storeId;
    private String storeName;
    private String categoryName; // Category 이름만 담기
    private String reason;       // AI가 생성한 추천 이유
}
