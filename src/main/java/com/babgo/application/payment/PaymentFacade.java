package com.babgo.application.payment;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.payment.Payment;
import com.babgo.domain.payment.PaymentService;
import com.babgo.domain.payment.exception.PaymentErrorType;
import com.babgo.domain.payment.exception.PaymentException;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
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

    @Transactional
    public PaymentInfo.ReadyResult ready(PaymentInfo.Ready input) {
        //User id 검증
        //User user = "userService.getUser(info.userId)";
        UUID user = UUID.randomUUID();

        Order order = orderService.getOrder(input.getOrderId());
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "현재 주문은 결제할 수 없습니다. 동일 구성으로 재주문해 주세요."
            );
        }

        paymentService.getPayment(order.getOrderId()).ifPresent(p -> {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "이미 결제가 진행 중이거나 완료되었습니다."
            );
        });

        // 3) 금액 검증
        Long expected = order.getTotalPrice();
        if (input.getAmount() != null && !expected.equals(input.getAmount())) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "결제 금액이 주문 금액과 일치하지 않습니다.");
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
            return PaymentInfo.ReadyResult.from(readPayment);
        }catch (DataIntegrityViolationException e){
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 결제가 생성되었습니다.");
        }
    }


}
