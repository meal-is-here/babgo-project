package com.babgo.domain.review;

import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReviewQueryServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewQueryService reviewQueryService;

    @Test
    @DisplayName("음식점별 리뷰 조회 성공")
    void getReviewsByStore_success() {
        UUID storeId = UUID.randomUUID();
        Review review1 = Review.of(5, "최고예요!", 1L, storeId, UUID.randomUUID());
        Review review2 = Review.of(3, "보통이에요", 2L, storeId, UUID.randomUUID());
        given(reviewRepository.existsByStore_StoreId(storeId)).willReturn(true);
        given(reviewRepository.findByStore_StoreId(eq(storeId), any(Sort.class)))
                .willReturn(List.of(review1, review2));

        List<ReviewResponse> result = reviewQueryService.getReviewsByStore(storeId, "latest");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("최고예요!");
    }

    @Test
    @DisplayName("음식점별 리뷰 조회 실패 - 유효하지 않은 정렬 조건")
    void getReviewsByStore_invalidSort() {
        UUID storeId = UUID.randomUUID();
        given(reviewRepository.existsByStore_StoreId(storeId)).willReturn(true);

        assertThatThrownBy(() -> reviewQueryService.getReviewsByStore(storeId, "invalid"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_SORT_TYPE.getMessage());
    }

    @Test
    @DisplayName("음식점별 리뷰 조회 실패 - 존재하지 않는 가게")
    void getReviewsByStore_storeNotFound() {
        UUID storeId = UUID.randomUUID();
        given(reviewRepository.existsByStore_StoreId(storeId)).willReturn(false);

        assertThatThrownBy(() -> reviewQueryService.getReviewsByStore(storeId, "latest"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.STORE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 성공 - 최신순")
    void getReviewsByUser_success_latest() {
        Long userId = 1L;
        Review review1 = Review.of(5, "너무 맛있어요!", userId, UUID.randomUUID(), UUID.randomUUID());
        Review review2 = Review.of(3, "그냥 그래요", userId, UUID.randomUUID(), UUID.randomUUID());

        given(reviewRepository.findAllByUserIdAndDeletedAtIsNull(eq(userId), any(Sort.class)))
                .willReturn(List.of(review1, review2));

        List<ReviewResponse> result = reviewQueryService.getReviewsByUser(userId, "latest");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("너무 맛있어요!");
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 실패 - 잘못된 정렬 조건")
    void getReviewsByUser_fail_invalidSort() {
        Long userId = 1L;

        assertThatThrownBy(() -> reviewQueryService.getReviewsByUser(userId, "wrongSort"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_SORT_TYPE.getMessage());
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 실패 - 비로그인 상태 (userId null)")
    void getReviewsByUser_fail_unauthorized() {
        assertThatThrownBy(() -> reviewQueryService.getReviewsByUser(null, "latest"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.UNAUTHORIZED_USER.getMessage());
    }
}
