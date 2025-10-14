package com.babgo.controller.payment;

import com.babgo.application.payment.PaymentInfo;
import com.babgo.domain.payment.PaymentStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResponse {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final UUID paymentId;
        private final PaymentStatus paymentStatus;

        public static Ready from(PaymentInfo.ReadyResult output){
            return new Ready(
                    output.getPaymentId(),
                    output.getPaymentStatus()
            );
        }
    }
}
