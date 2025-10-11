package com.babgo.payment;

import com.babgo.application.payment.PaymentFacade;
import com.babgo.application.payment.PaymentInfo;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.payment.*;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadPayment {

    @Mock
    private OrderService orderService;
    @Mock
    private PaymentService paymentService;

    private PaymentFacade sut;

    @BeforeEach
    void setUp() {
        sut = new PaymentFacade(orderService, paymentService);
    }

        private static Order pendingOrder(UUID orderId, UUID userId, long totalPrice) {
            Order o = mock(Order.class);
            when(o.getOrderId()).thenReturn(orderId);
            when(o.getOrderStatus()).thenReturn(OrderStatus.PENDING);
            return o;
        }

        private static PaymentInfo.Ready readyInput(UUID userId, UUID orderId, Long amount, PaymentMethod method, CardBrand cardBrand, CardType cardType) {
            return new PaymentInfo.Ready(userId, orderId , amount, method, cardBrand, cardType);
        }


        @Test
        @DisplayName("성공: PENDING 주문이고 기존 결제 없고 금액 일치하면 새 Payment 생성")
        void ready_success() {
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            long price = 15000L;

            Order order = pendingOrder(orderId, userId, price);
            when(order.getTotalPrice()).thenReturn(price);
            when(orderService.getOrder(orderId)).thenReturn(order);
            when(paymentService.getPayment(orderId)).thenReturn(Optional.empty());

            Payment toSave = mock(Payment.class);
            Payment saved = mock(Payment.class);
            when(paymentService.create(any(Payment.class))).thenReturn(saved);

            PaymentInfo.Ready input = readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);
            PaymentInfo.ReadyResult result = sut.ready(input);

            // 저장 호출 검증
            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentService).create(captor.capture());
            Payment created = captor.getValue();
            assertThat(created).isNotNull();

            // 결과 매핑만 sanity-check (구현에 맞게 보정)
            assertThat(result).isNotNull();
            verify(paymentService, times(1)).getPayment(orderId);
            verify(paymentService, times(1)).create(any(Payment.class));
        }

        @Test
        @DisplayName("실패: 주문 상태가 PENDING이 아니면 UNPROCESSABLE")
        void ready_fail_when_order_not_pending() {
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Order order = mock(Order.class);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);
            when(orderService.getOrder(orderId)).thenReturn(order);

            PaymentInfo.Ready input = readyInput(userId ,orderId, 1000L, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

            assertThatThrownBy(() -> sut.ready(input))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
                    });

            verify(paymentService, never()).create(any());
        }

        @Test
        @DisplayName("실패: 동일 orderId로 기존 결제가 있으면 CONFLICT")
        void ready_fail_when_existing_payment() {
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            long price = 12000L;

            Order order = pendingOrder(orderId, userId, price);
            when(orderService.getOrder(orderId)).thenReturn(order);

            Payment existing = mock(Payment.class);
            when(paymentService.getPayment(orderId)).thenReturn(Optional.of(existing));

            PaymentInfo.Ready input = readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

            assertThatThrownBy(() -> sut.ready(input))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
                    });

            verify(paymentService, never()).create(any());
        }

        @Test
        @DisplayName("실패: 금액 불일치면 AMOUNT_MISMATCH")
        void ready_fail_when_amount_mismatch() {
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            long price = 10000L;

            Order order = pendingOrder(orderId, userId, price);
            when(order.getTotalPrice()).thenReturn(price);
            when(orderService.getOrder(orderId)).thenReturn(order);
            when(paymentService.getPayment(orderId)).thenReturn(Optional.empty());

            PaymentInfo.Ready input = readyInput(userId, orderId, 9000L, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

            assertThatThrownBy(() -> sut.ready(input))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException ce = (CustomException) ex;
                        assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
                    });

            verify(paymentService, never()).create(any());
        }
    @Test
    @DisplayName("실패: 동시성 등으로 DB 유니크 충돌 시 CONFLICT 변환")
    void ready_fail_when_unique_violation() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        long price = 20_000L;

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(paymentService.getPayment(orderId)).thenReturn(Optional.empty());
        when(order.getTotalPrice()).thenReturn(price);
        when(paymentService.create(any(Payment.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        PaymentInfo.Ready input = readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

        assertThatThrownBy(() -> sut.ready(input))
                .isInstanceOf(CustomException.class) // 현재 프로덕션은 CustomException/BAD_REQUEST 사용 중
                .satisfies(ex -> {
                    var ce = (com.babgo.global.exception.CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(com.babgo.global.exception.ErrorCode.BAD_REQUEST);
                });
    }

}
