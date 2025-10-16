package com.babgo.domain.review;

import com.babgo.controller.review.dto.ReviewCreateRequest;
import com.babgo.controller.review.dto.ReviewResponse;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.babgo.domain.order.OrderStatus;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Order order;
    private ReviewCreateRequest request;

    @BeforeEach
    void setUp() {
        order = Order.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1L,
                "배달 요청사항 없음",
                "서울시 강남구",
                20000L
        );

        request = new ReviewCreateRequest(
                order.getOrderId(),
                5,
                "배달도 빠르고 맛있어요!"
        );
    }

    @Test
    @DisplayName("리뷰 등록 성공 - 주문 완료 상태")
    void createReview_success() {
        // given
        given(orderRepository.findByOrderId(order.getOrderId()))
                .willReturn(Optional.of(order));
        given(reviewRepository.findByOrderId(order.getOrderId()))
                .willReturn(Optional.empty());

        order.markConfirmed();

        Review savedReview = Review.of(
                5, "맛있어요!", order.getUserId(), order.getStoreId(), order.getOrderId());
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(order.getUserId(), request);

        // then
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getContent()).isEqualTo("맛있어요!");
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 주문이 존재하지 않음")
    void createReview_fail_orderNotFound() {
        // given
        given(orderRepository.findByOrderId(any(UUID.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 주문 미완료 상태")
    void createReview_fail_orderNotCompleted() {
        given(orderRepository.findByOrderId(order.getOrderId()))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_COMPLETED.getMessage());
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 이미 리뷰 존재")
    void createReview_fail_duplicateReview() {
        // given
        given(orderRepository.findByOrderId(order.getOrderId()))
                .willReturn(Optional.of(order));
        given(reviewRepository.findByOrderId(order.getOrderId()))
                .willReturn(Optional.of(new Review()));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage());
    }
}