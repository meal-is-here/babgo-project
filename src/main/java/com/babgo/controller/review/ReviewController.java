package com.babgo.controller.review;

import com.babgo.controller.review.dto.ReviewCreateRequest;
import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.domain.review.ReviewQueryService;
import com.babgo.domain.review.ReviewService;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewQueryService reviewQueryService;

    // create review
    @PostMapping
    public ApiResponse<ReviewResponse> createReview(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        Long userId = 2L;
        ReviewResponse response = reviewService.createReview(userId, request);
        return ApiResponse.success("리뷰 등록 성공", response);
    }

    // read review by store
    @GetMapping("/{storeId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByStore(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        List<ReviewResponse> reviews = reviewQueryService.getReviewsByStore(storeId, sort);
        return ApiResponse.success("리뷰 목록 조회 성공", reviews);
    }
}