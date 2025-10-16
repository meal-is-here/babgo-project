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

        // 후보 가게 추출 + 유사도 계산
        List<Store> topStores = storeSimilarityService.findSimilarStores(userQuery, topK);

        // AI 추천 이유 생성 + DTO로 변환
        return topStores.stream()
                .map(store -> {
                    String reason = aiRecommendationService.generateRecommendationReason(userQuery, store);

                    // Lazy 로딩 안전하게 필드만 뽑아서 DTO 생성
                    return new RecommendedStoreDTO(
                            store.getStoreId(),
                            store.getStoreName(),
                            store.getCategory() != null ? store.getCategory().getCategoryName() : null,
                            reason
                    );
                })
                .collect(Collectors.toList());
    }
}
