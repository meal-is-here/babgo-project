package com.babgo.application.payment.processor;

import com.babgo.domain.payment.Payment;

import java.util.UUID;

public record PaymentProcessContext(
        String paymentKey,
        UUID paymentId,
        UUID orderId,
        Long amount

){ public static PaymentProcessContext of(Payment payment, String paymentKey, Long amount){

    return new PaymentProcessContext(
            paymentKey,
            payment.getPaymentId(),
            payment.getOrderId(),
            amount
    );
    }
}
