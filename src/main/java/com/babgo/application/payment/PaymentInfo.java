package com.babgo.application.payment;

import com.babgo.controller.payment.PaymentRequest;
import com.babgo.domain.payment.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInfo {

    /**
     * Input
     */
    @Getter
    @RequiredArgsConstructor
    public static class Ready {
        private final UUID userId;
        private final UUID orderId;
        private final Long amount;
        private final PaymentMethod paymentType;
        private final CardBrand cardBrand;
        private final CardType cardType;
        public static Ready from(
                UUID userId,
                PaymentRequest.Ready request
        ){
            return new Ready(
                    userId,
                    request.getOrderId(),
                    request.getAmount(),
                    request.getPaymentType(),
                    request.getCardBrand(),
                    request.getCardType()
            );
        }
    }
    /**
     * Output
     */
    @Getter
    @RequiredArgsConstructor
    public static class ReadyResult {
        private final UUID paymentId;
        private final PaymentStatus paymentStatus;

        public static ReadyResult from(Payment payment){
            return new ReadyResult(
              payment.getPaymentId(),
              payment.getPaymentStatus()
            );
        }
    }
}
