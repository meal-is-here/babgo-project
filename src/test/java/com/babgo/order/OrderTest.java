package com.babgo.order;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderStatus;
import com.babgo.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Order 엔티티 테스트")
public class OrderTest {

    @Test
    @DisplayName("주문 생성 - 상태는 PENDING")
    void createOrder_shouldHavePendingStatus() {
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Order order = Order.of(orderId, storeId, 1L, "요청사항", "서울시", 10000L);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalPrice()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("PENDING 상태에서만 PAYMENT_IN_PROGRESS로 변경 가능")
    void markPaymentInProgress_onlyFromPending() {
        Order order = Order.of(UUID.randomUUID(), UUID.randomUUID(), 1L, "요청", "주소", 10000L);

        order.markPaymentInProgress();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_IN_PROGRESS);

        assertThatThrownBy(() -> order.markPaymentInProgress())
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("PENDING 상태에서만 취소 가능")
    void markCancel_onlyFromPending() {
        Order order = Order.of(UUID.randomUUID(), UUID.randomUUID(), 1L, "요청", "주소", 10000L);

        order.markCancel();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

        assertThatThrownBy(order::markCancel)
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("CONFIRMED 상태 확인")
    void isCompleted_returnsTrueWhenConfirmed() {
        Order order = Order.of(UUID.randomUUID(), UUID.randomUUID(), 1L, "요청", "주소", 10000L);

        order.markPaymentInProgress();
        order.markConfirmed();

        assertThat(order.isCompleted()).isTrue();
    }
}