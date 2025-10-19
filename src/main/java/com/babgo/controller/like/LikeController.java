package com.babgo.controller.like;

import com.babgo.controller.like.dto.LikeResponse;
import com.babgo.domain.like.Like;
import com.babgo.domain.like.LikeService;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/likes")
public class LikeController {

    private final LikeService likeService;

    // register like
    @PostMapping("/{storeId}")
    public ApiResponse<LikeResponse> registerLike(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @PathVariable UUID storeId
    ) {
        Long userId = 1L;
        Like like = likeService.registerLike(userId, storeId);
        return ApiResponse.success("좋아요가 등록되었습니다.", LikeResponse.from(like));
    }

    // unlike store
    @DeleteMapping("/{storeId}")
    public ApiResponse<String> unlikeStore(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @PathVariable UUID storeId
    ) {

        Long userId = 1L;
        likeService.unlikeStore(userId, storeId);
        return ApiResponse.success("좋아요가 해제되었습니다.");
    }
}
