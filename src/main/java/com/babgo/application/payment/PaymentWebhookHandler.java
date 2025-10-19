package com.babgo.application.payment;

import com.babgo.application.payment.event.ApprovedEvent;
import com.babgo.controller.payment.PaymentRequest;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.payment.Payment;
import com.babgo.domain.payment.PaymentService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWebhookHandler {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * PG에서 결제 상태 변경이벤트 수신 시 처리
     * - 결제 승인(APPROVED)
     * - 결제 실패(DECLINED)
     * - 결제 취소(CANCELLED)
     */
    @Transactional
    public void handleStatusChanged(PaymentRequest.WebhookPayload payload) {
        // 주문, 결제
        UUID orderId = UUID.fromString(payload.getOrderId());
        Payment payment = paymentService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        // 상태 전이
        switch (payload.getStatus()){
            case "APPROVED" ->
                    {
                        paymentService.updateApproved(payment, payload.getTransactionKey());
                        orderService.updateConfirmed(orderId);
                        eventPublisher.publishEvent(new ApprovedEvent(orderId));
                    }
            case "DECLINED" -> payment.markFailed();
        }

        // 상태 업데이트 진행
    }

    public void handleCancelled(PaymentRequest.WebhookPayload payload) {
    }
    /**
     * 결제 실패 처리 (fail URL 콜백)
     */
    @Transactional
    public void handlePaymentFailure(UUID orderId, String code, String message) {
        Payment payment = paymentService.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        // 결제 상태를 FAILED로 변경
        payment.markFailed();

        // 주문 상태도 실패로 변경
        orderService.updateFailed(orderId);

        log.info("[Payment Failure] 결제 실패 처리 완료 - orderId: {}, code: {}, message: {}",
                orderId, code, message);
    }


    public void handleRefund(PaymentRequest.WebhookPayload payload) {
    }
}
