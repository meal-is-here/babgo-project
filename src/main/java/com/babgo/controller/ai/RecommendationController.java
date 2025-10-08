package com.babgo.controller.ai;

import com.babgo.application.ai.RecommendationService;
import com.babgo.controller.ai.dto_recommend.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/v1/recommendations/{userId}")
    public Mono<RecommendationResponse> getRecommendations(@PathVariable String userId) {
        return recommendationService.getPersonalizedRecommendations(userId);
    }
}