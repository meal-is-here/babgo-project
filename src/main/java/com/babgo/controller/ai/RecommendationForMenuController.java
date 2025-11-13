package com.babgo.controller.ai;

import com.babgo.domain.ai.by_menu_recommendation.RecommendationForMenuService;
import com.babgo.domain.ai.by_menu_recommendation.RecommendedMenuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RecommendationForMenuController {

    private final RecommendationForMenuService recommendationForMenuService;

    @GetMapping("/v1/recommendations/menu")
    public List<RecommendedMenuDTO> recommendMenu(
            @RequestParam("userId") Long userId,
            @RequestParam("storeId") UUID storeId,
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK) {

        return recommendationForMenuService.recommendMenus(userId, storeId, query, topK);
    }
}
