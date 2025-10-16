package com.babgo.controller.ai;

import com.babgo.domain.ai.by_search_recommendation.RecommendationForSearchService;
import com.babgo.domain.ai.by_search_recommendation.RecommendedStoreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendationForSearchController {

    private final RecommendationForSearchService recommendationService;

    @GetMapping("/v1/recommendations/ai")
    public List<RecommendedStoreDTO> recommendAI(@RequestParam String query,
                                                 @RequestParam(defaultValue = "5") int topK) {
        return recommendationService.recommendStoresWithReason(query, topK);
    }
}
