package com.babgo.application.ai;

import com.babgo.controller.ai.RecommendationForUserResponse;
import com.babgo.domain.ai.by_user_recommendation.RecommendationForUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RecommendationFacade {

    private final RecommendationForUserService recommendationForUserService;

    @Value("${fastapi.RECOMMENDATION_FASTAPI_URL}")
    private String fastApiBaseUrl;

    public Mono<RecommendationForUserResponse> getRecommendations(String userId) {
        return recommendationForUserService.getPersonalizedRecommendations(userId, fastApiBaseUrl);
    }
}
