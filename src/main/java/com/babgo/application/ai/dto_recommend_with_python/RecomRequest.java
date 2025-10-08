package com.babgo.application.ai.dto_recommend_with_python;

import lombok.Getter;

@Getter
public class RecomRequest {
    private String userId;

    public RecomRequest(String userId) {
        this.userId = userId;
    }
}
