package com.babgo.domain.ai.by_search_recommendation;

import com.babgo.domain.store.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationForSearchService {

    private final StoreSimilarityService storeSimilarityService;
    private final AIRecommendationService aiRecommendationService;

    public List<RecommendedStoreDTO> recommendStoresWithReason(String userQuery, int topK) {

        // 1️⃣ 후보 가게 추출 + 유사도 계산
        List<Store> topStores = storeSimilarityService.findSimilarStores(userQuery, topK);

        // 2️⃣ AI 추천 이유 생성
        return topStores.stream()
                .map(store -> {
                    String reason = aiRecommendationService.generateRecommendationReason(userQuery, store);
                    return new RecommendedStoreDTO(store, reason);
                })
                .collect(Collectors.toList());
    }
}
