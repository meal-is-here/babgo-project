package com.babgo.controller.favorite;

import com.babgo.controller.favorite.dto.FavoriteRequest;
import com.babgo.controller.favorite.dto.FavoriteResponse;
import com.babgo.domain.favorite.FavoriteService;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // add favorite
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @RequestBody FavoriteRequest request) {

        Long userId = 1L;
        FavoriteResponse response = favoriteService.addFavorite(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("즐겨찾기가 등록되었습니다.", response));
    }
}
