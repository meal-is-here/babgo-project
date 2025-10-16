package com.babgo.payment;

import com.babgo.application.payment.PaymentFacade;
import com.babgo.application.payment.PaymentInfo;
import com.babgo.application.payment.async.PaymentAsyncExecutor;
import com.babgo.controller.payment.gateway.PaymentGateway;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.payment.*;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.payment.client.ClientResponse;
import com.babgo.repository.payment.resolver.PaymentUrlResolver;
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

    @Mock private OrderService orderService;
    @Mock private PaymentService paymentService;
    @Mock private PaymentAsyncExecutor paymentAsyncExecutor;
    @Mock private PaymentUrlResolver urlResolver;
    @Mock private PaymentGateway paymentGateway;

    private PaymentFacade sut;

    @BeforeEach
    void setUp() {
        sut = new PaymentFacade(orderService, paymentService, paymentAsyncExecutor, paymentGateway, urlResolver);
    }

    /** 공통으로 항상 쓰는 부분만 스텁(상태/ID) — 금액은 각 테스트에서 필요할 때만 스텁 */
    private static Order pendingOrder(UUID orderId) {
        Order o = mock(Order.class);
        when(o.getOrderId()).thenReturn(orderId);
        when(o.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        return o;
    }

    private static PaymentInfo.Ready readyInput(
            Long userId, UUID orderId, Long amount, PaymentMethod method, CardBrand cardBrand, CardType cardType
    ) {
        return new PaymentInfo.Ready(userId, orderId, amount, method, cardBrand, cardType);
    }

    @Test
    @DisplayName("성공: PENDING 주문이고 기존 결제 없고 금액 일치하면 새 Payment 생성")
    void ready_success() {
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        long price = 15_000L;

        Order order = pendingOrder(orderId);
        when(orderService.getOrder(orderId)).thenReturn(order);
        // 이 테스트에서만 금액 스텁
        when(order.getTotalPrice()).thenReturn(price);

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.empty());

        Payment saved = mock(Payment.class);
        when(saved.getOrderId()).thenReturn(orderId);
        when(paymentService.create(any(Payment.class))).thenReturn(saved);

        // URL/PG 스텁 (이 테스트에서 실제로 사용됨)
        when(urlResolver.approveUrl(saved.getOrderId())).thenReturn("http://localhost:8080/pay/success?orderId=" + orderId);
        when(urlResolver.failUrl(saved.getOrderId())).thenReturn("http://localhost:8080/pay/fail?orderId=" + orderId);
        when(urlResolver.webhookUrl()).thenReturn("http://localhost:8080/v1/payments/webhook");

        ClientResponse.create pgResp = mock(ClientResponse.create.class);
        when(pgResp.getUrl()).thenReturn("http://localhost:8080/pg-sim/checkout?o=" + orderId);
        when(paymentGateway.create(any())).thenReturn(pgResp);

        PaymentInfo.Ready input = readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);
        PaymentInfo.ReadyResult result = sut.ready(input);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentService).create(captor.capture());
        assertThat(captor.getValue()).isNotNull();

        assertThat(result).isNotNull();
        verify(paymentService, times(1)).getPaymentByOrderId(orderId);
        verify(paymentService, times(1)).create(any(Payment.class));
        verify(paymentGateway, times(1)).create(any());
    }

    @Test
    @DisplayName("실패: 주문 상태가 PENDING이 아니면 ORDER_NOT_PAYABLE")
    void ready_fail_when_order_not_pending() {
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;

        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELED);
        when(orderService.getOrder(orderId)).thenReturn(order);

        PaymentInfo.Ready input =
                readyInput(userId, orderId, 1_000L, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

        assertThatThrownBy(() -> sut.ready(input))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_NOT_PAYABLE));

        verify(paymentService, never()).create(any());
        verify(paymentGateway, never()).create(any());
    }

    @Test
    @DisplayName("실패: 동일 orderId로 기존 결제가 있으면 PAYMENT_ALREADY_IN_PROGRESS")
    void ready_fail_when_existing_payment() {
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        long price = 12_000L;

        Order order = pendingOrder(orderId);
        when(orderService.getOrder(orderId)).thenReturn(order);
        // 이 테스트 흐름에서는 totalPrice 비교가 호출되지 않으므로 스텁 생략 (불필요 스텁 방지)

        Payment existing = mock(Payment.class);
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.of(existing));

        PaymentInfo.Ready input =
                readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

        assertThatThrownBy(() -> sut.ready(input))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS));

        verify(paymentService, never()).create(any());
        verify(paymentGateway, never()).create(any());
    }

    @Test
    @DisplayName("실패: 금액 불일치면 PAYMENT_AMOUNT_MISMATCH")
    void ready_fail_when_amount_mismatch() {
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        long price = 10_000L;

        Order order = pendingOrder(orderId);
        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getTotalPrice()).thenReturn(price); // 이 테스트에서만 필요

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.empty());

        PaymentInfo.Ready input =
                readyInput(userId, orderId, 9_000L, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

        assertThatThrownBy(() -> sut.ready(input))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH));

        verify(paymentService, never()).create(any());
        verify(paymentGateway, never()).create(any());
    }

    @Test
    @DisplayName("실패: 동시성 등으로 DB 유니크 충돌 시 PAYMENT_ALREADY_EXISTS로 변환")
    void ready_fail_when_unique_violation() {
        UUID orderId = UUID.randomUUID();
        Long userId = 1L;
        long price = 20_000L;

        Order order = pendingOrder(orderId);
        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getTotalPrice()).thenReturn(price); // 이 테스트에서만 필요
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentService.create(any(Payment.class)))
                .thenThrow(new DataIntegrityViolationException("unique violation"));

        PaymentInfo.Ready input =
                readyInput(userId, orderId, price, PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CHECK);

        assertThatThrownBy(() -> sut.ready(input))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS));

        verify(paymentGateway, never()).create(any());
    }
}
