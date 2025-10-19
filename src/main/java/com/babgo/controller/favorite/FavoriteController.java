package com.babgo.controller.favorite;

import com.babgo.controller.favorite.dto.FavoriteCreateRequest;
import com.babgo.controller.favorite.dto.FavoriteResponse;
import com.babgo.controller.favorite.dto.FavoriteUpdateRequest;
import com.babgo.domain.favorite.FavoriteService;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @RequestBody FavoriteCreateRequest request) {

        Long userId = 1L;
        FavoriteResponse response = favoriteService.addFavorite(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("즐겨찾기 등록 성공", response));
    }

    // update favorite
    @PatchMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> updateFavorite(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @PathVariable UUID favoriteId,
            @RequestBody FavoriteUpdateRequest request
    ) {
        Long userId = 1L;
        FavoriteResponse response = favoriteService.updateFavorite(userId, favoriteId, request);
        return ResponseEntity.ok(ApiResponse.success("즐겨찾기 수정 성공c", response));

    }

    // delete favorite
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @PathVariable UUID favoriteId
    ) {
        Long userId = 1L;
        favoriteService.deleteFavorite(userId, favoriteId);
        return ResponseEntity.ok(ApiResponse.success("즐겨찾기 해제 성공"));
    }
}