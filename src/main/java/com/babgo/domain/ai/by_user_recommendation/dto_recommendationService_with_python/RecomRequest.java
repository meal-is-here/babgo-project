package com.babgo.domain.ai.by_user_recommendation.dto_recommendationService_with_python;

import lombok.Getter;

@Getter
public class RecomRequest {
    private Long userId;

    public RecomRequest(Long userId) {
        this.userId = userId;
    }
}
