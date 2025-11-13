package com.babgo.domain.ai.by_menu_recommendation;

public record RecommendedMenuDTO(
        String menuId,
        String menuName,
        String category,
        String reason
) {}