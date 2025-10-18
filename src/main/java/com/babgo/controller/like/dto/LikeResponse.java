package com.babgo.controller.like.dto;

import com.babgo.domain.like.Like;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LikeResponse {

    private UUID likeId;

    private UUID storeId;

    private Long userId;

    public static LikeResponse from(Like like) {
        return LikeResponse.builder()
                .likeId(like.getLikeId())
                .storeId(like.getStore().getStoreId())
                .userId(like.getUser().getUserId())
                .build();
    }
}
