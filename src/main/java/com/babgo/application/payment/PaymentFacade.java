package com.babgo.application.payment;

import com.babgo.application.payment.async.PaymentAsyncExecutor;
import com.babgo.controller.payment.gateway.PaymentGateway;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.payment.Payment;
import com.babgo.domain.payment.PaymentService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.payment.client.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentAsyncExecutor paymentAsyncExecutor;
    private final PaymentGateway paymentGateway;
    private final PaymentUrlResolver urlResolver;

    @Transactional
    public PaymentInfo.ReadyResult ready(PaymentInfo.Ready input) {
        //User id 검증
        //User user = "userService.getUser(info.userId)";
        Long user = 1L;

        Order order = orderService.getOrder(input.getOrderId());
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new CustomException(
                    ErrorCode.ORDER_NOT_PAYABLE,
                    "현재 주문은 결제할 수 없습니다. 동일 구성으로 재주문해 주세요."
            );
        }

        paymentService.getPaymentByOrderId(order.getOrderId()).ifPresent(p -> {
            throw new CustomException(
                    ErrorCode.PAYMENT_ALREADY_IN_PROGRESS,
                    "이미 결제가 진행 중이거나 완료되었습니다."
            );
        });

        // 3) 금액 검증
        Long expected = order.getTotalPrice();
        if (input.getAmount() != null && !expected.equals(input.getAmount())) {
            throw new CustomException(
                    ErrorCode.PAYMENT_AMOUNT_MISMATCH,
                    "결제 금액이 주문 금액과 일치하지 않습니다."
            );
        }

        Payment payment = Payment.of(
                order.getOrderId(),
                user,
                expected,
                input.getPaymentType(),
                input.getCardBrand(),
                input.getCardType()
        );

        try {
            Payment readPayment = paymentService.create(payment);

            PaymentInfo.CreatePg createInfo = PaymentInfo.CreatePg.from(
                    readPayment,
                    urlResolver.successUrl(readPayment),
                    urlResolver.failUrl(readPayment),
                    urlResolver.webhookUrl()
            );

            ClientResponse.create result = paymentGateway.create(createInfo);
            PaymentInfo.ReadyResult readyResult = PaymentInfo.ReadyResult.from(readPayment, result.getUrl());

            return readyResult;
        }catch (DataIntegrityViolationException e){
            throw new CustomException(
                    ErrorCode.PAYMENT_ALREADY_EXISTS,
                    "이미 결제가 생성되었습니다."
            );
        }
    }

    public void pay(String paymentKey, UUID orderId, Long amount){
        // 1) 결제 로드
        Payment payment = paymentService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.NOT_FOUND,
                        "결제 건을 찾을 수 없습니다.")
                );

        // 추후 재시도 또는 취소를 위해 저장해두기
        /*paymentService.attachPaymentKey(payment.getPaymentId(), paymentKey);*/

        //2) 원자적 전이(CAS)
        if (!paymentService.startPayment(payment.getPaymentId())) {
            throw new CustomException(
                    ErrorCode.PAYMENT_ALREADY_IN_PROGRESS,
                    "이미 진행 중이거나 완료된 결제입니다."
            );
        }

        //3) 비동기 실행 시작 (ID만 넘기기)
        paymentAsyncExecutor.requestPgAsync(payment.getPaymentId() , paymentKey, amount);
    }
}
