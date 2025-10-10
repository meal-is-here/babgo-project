package com.babgo.domain.ai.recommendation.dto_recommendationService_with_python;

import lombok.Getter;

@Getter
public class RecomRequest {
    private String userId;

    public RecomRequest(String userId) {
        this.userId = userId;
    }
}
