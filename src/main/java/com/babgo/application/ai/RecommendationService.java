package com.babgo.application.ai;

import com.babgo.application.ai.dto_recommend_with_python.RecomRequest;
import com.babgo.application.ai.dto_recommend_with_python.RecomResponse;
import com.babgo.controller.ai.dto_recommend.RecommendationResponse;
import com.babgo.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final WebClient webClient;
    private final StoreRepository storeRepository;

//    // Python API와 통신하기 위한 내부용 DTO
//    private record recomRequest(String userId) {}
//    private record recomResponse(List<String> storeIds) {}

    public Mono<RecommendationResponse> getPersonalizedRecommendations(String userId) {
        return webClient.post()
                .uri("/recommendations")
                .bodyValue(new RecomRequest(userId))
                .retrieve() // 응답을 받아
                .bodyToMono(RecomResponse.class) // ApiResponse DTO로 변환하고
                .map(response -> { // 받은 ID 목록으로
                    List<UUID> storeUuids = response.getStoreIds().stream()
                            .map(UUID::fromString)
                            .toList();
                    // 우리 DB에서 가게 정보를 조회
                    return storeRepository.findByStoreIdIn(storeUuids);
                })
                .map(RecommendationResponse::from); // 최종 응답 DTO로 변환
    }
}