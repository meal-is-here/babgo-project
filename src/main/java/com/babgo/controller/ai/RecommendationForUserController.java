package com.babgo.controller.ai;

import com.babgo.application.ai.RecommendationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RecommendationForUserController {

    private final RecommendationFacade recommendationFacade;

    @GetMapping("/v1/recommendations/{userId}")
    public Mono<RecommendationForUserResponse> getRecommendations(@PathVariable Long userId) {
        return recommendationFacade.getRecommendations(userId);
    }
}
