package com.babgo.application.ai;

import com.babgo.controller.ai.RecommendationResponse;
import com.babgo.domain.ai.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RecommendationFacade {

    private final RecommendationService recommendationService;

    @Value("${fastapi.RECOMMENDATION_FASTAPI_URL}")
    private String fastApiBaseUrl;

    public Mono<RecommendationResponse> getRecommendations(String userId) {
        return recommendationService.getPersonalizedRecommendations(userId, fastApiBaseUrl);
    }
}
